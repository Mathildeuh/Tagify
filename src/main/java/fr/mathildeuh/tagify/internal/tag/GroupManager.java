package fr.mathildeuh.tagify.internal.tag;

import fr.mathildeuh.tagify.api.event.TagGroupUpdateEvent;
import fr.mathildeuh.tagify.api.model.Tag;
import fr.mathildeuh.tagify.api.model.TagGroup;
import fr.mathildeuh.tagify.api.service.GroupService;
import fr.mathildeuh.tagify.internal.config.TagifyConfig;
import fr.mathildeuh.tagify.internal.integration.permission.GroupProvider;
import fr.mathildeuh.tagify.internal.module.SyncService;
import fr.mathildeuh.tagify.internal.platform.Platform;
import fr.mathildeuh.tagify.internal.storage.DataStore;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * In-memory group cache + persistent CRUD + resolution of a player's membership
 * (explicit members ∪ {@code tagify.group.<key>} permission ∪ mapped permission groups),
 * filtered / re-prioritised by {@link GroupConditions} (Premium).
 */
public final class GroupManager implements GroupService {

    private static final Comparator<TagGroup> ORDER =
            Comparator.comparingInt(TagGroup::priority).reversed()
                    .thenComparing(TagGroup::key);

    private final Platform platform;
    private final Supplier<DataStore> store;
    private final Supplier<GroupProvider> groupProvider;
    private final Supplier<TagifyConfig> config;
    private final Supplier<SyncService> sync;
    private final Supplier<GroupConditions> conditions;
    private final RefreshService refresh;

    private final Map<String, TagGroup> cache = new ConcurrentHashMap<>();

    public GroupManager(Platform platform, Supplier<DataStore> store,
                        Supplier<GroupProvider> groupProvider, Supplier<TagifyConfig> config,
                        Supplier<SyncService> sync, Supplier<GroupConditions> conditions,
                        RefreshService refresh) {
        this.platform = platform;
        this.store = store;
        this.groupProvider = groupProvider;
        this.config = config;
        this.sync = sync;
        this.conditions = conditions;
        this.refresh = refresh;
    }

    public CompletableFuture<Void> loadAll() {
        return store.get().loadGroups().thenAccept(groups -> {
            cache.clear();
            for (TagGroup group : groups) {
                cache.put(group.key(), group);
            }
        });
    }

    // ------------------------------------------------------------------ reads

    @Override
    public Optional<TagGroup> getGroup(String name) {
        return name == null ? Optional.empty()
                : Optional.ofNullable(cache.get(name.toLowerCase(Locale.ROOT)));
    }

    @Override
    public @Unmodifiable List<TagGroup> getGroups() {
        List<TagGroup> list = new ArrayList<>(cache.values());
        list.sort(ORDER);
        return List.copyOf(list);
    }

    @Override
    public boolean groupExists(String name) {
        return name != null && cache.containsKey(name.toLowerCase(Locale.ROOT));
    }

    @Override
    public List<TagGroup> getGroupsOf(UUID playerId) {
        Map<String, TagGroup> matched = new LinkedHashMap<>();

        for (TagGroup group : cache.values()) {
            if (group.hasExplicitMember(playerId)) {
                matched.put(group.key(), group);
            }
        }

        Player online = Bukkit.getPlayer(playerId);
        if (online != null) {
            for (Map.Entry<String, TagGroup> entry : cache.entrySet()) {
                if (online.hasPermission("tagify.group." + entry.getKey())) {
                    matched.put(entry.getKey(), entry.getValue());
                }
            }
        }

        TagifyConfig.Groups settings = config.get().groups();
        if (settings.mapLuckPermsGroups()) {
            String filter = settings.luckPermsGroupFilter().toLowerCase(Locale.ROOT);
            for (String raw : groupProvider.get().groupsOf(playerId)) {
                String key = raw.toLowerCase(Locale.ROOT);
                if (!filter.isEmpty() && !key.startsWith(filter)) {
                    continue;
                }
                TagGroup group = cache.get(key);
                if (group != null) {
                    matched.put(key, group);
                }
            }
        }

        GroupConditions gc = conditions.get();
        List<TagGroup> result = new ArrayList<>();
        for (TagGroup group : matched.values()) {
            GroupConditions.Decision decision = gc.evaluate(online, group);
            if (!decision.applies()) {
                continue;
            }
            result.add(decision.effectivePriority() == group.priority()
                    ? group
                    : group.toBuilder().priority(decision.effectivePriority()).build());
        }
        result.sort(ORDER);
        return result;
    }

    // ------------------------------------------------------------------ writes

    @Override
    public CompletableFuture<TagGroup> createGroup(String name) {
        String key = name.toLowerCase(Locale.ROOT);
        if (cache.containsKey(key)) {
            return failed(new IllegalStateException("Group '" + name + "' already exists."));
        }
        TagGroup group = TagGroup.builder(name)
                .priority(config.get().groups().defaultPriority())
                .build();
        cache.put(key, group);
        return store.get().saveGroup(group)
                .thenApply(v -> {
                    notifyChange(TagGroupUpdateEvent.Action.CREATE, group.name(), group);
                    return group;
                });
    }

    @Override
    public CompletableFuture<Void> deleteGroup(String name) {
        String key = name.toLowerCase(Locale.ROOT);
        TagGroup removed = cache.remove(key);
        if (removed == null) {
            return failed(new IllegalStateException("Group '" + name + "' does not exist."));
        }
        return store.get().deleteGroup(key)
                .thenRun(() -> notifyChange(TagGroupUpdateEvent.Action.DELETE, removed.name(), null));
    }

    @Override
    public CompletableFuture<Void> saveGroup(TagGroup group) {
        cache.put(group.key(), group);
        return store.get().saveGroup(group)
                .thenRun(() -> notifyChange(TagGroupUpdateEvent.Action.MODIFY, group.name(), group));
    }

    @Override
    public CompletableFuture<Void> setGroupTag(String name, Tag tag) {
        return mutate(name, builder -> builder.tag(tag));
    }

    @Override
    public CompletableFuture<Void> setGroupPriority(String name, int priority) {
        return mutate(name, builder -> builder.priority(priority));
    }

    @Override
    public CompletableFuture<Void> setGroupWeight(String name, int weight) {
        return mutate(name, builder -> builder.weight(weight));
    }

    @Override
    public CompletableFuture<Void> addMember(String name, UUID playerId) {
        return mutate(name, builder -> builder.addMember(playerId));
    }

    @Override
    public CompletableFuture<Void> removeMember(String name, UUID playerId) {
        return mutate(name, builder -> builder.removeMember(playerId));
    }

    /** Sets a raw metadata key on a group (used for Premium conditions). */
    public CompletableFuture<Void> setGroupMetadata(String name, String key, String value) {
        return mutate(name, builder -> {
            if (value == null || value.isEmpty()) {
                Map<String, String> current = new LinkedHashMap<>(
                        getGroup(name).map(TagGroup::metadata).orElse(Map.of()));
                current.remove(key);
                builder.metadata(current);
            } else {
                builder.putMetadata(key, value);
            }
        });
    }

    private CompletableFuture<Void> mutate(String name, Consumer<TagGroup.Builder> edit) {
        TagGroup existing = cache.get(name.toLowerCase(Locale.ROOT));
        if (existing == null) {
            return failed(new IllegalStateException("Group '" + name + "' does not exist."));
        }
        TagGroup.Builder builder = existing.toBuilder();
        edit.accept(builder);
        return saveGroup(builder.build());
    }

    // ------------------------------------------------------------------ internal

    /** Reloads the cache from storage (triggered by an update received from another server). */
    public void onRemoteUpdate() {
        loadAll().thenRun(() -> platform.scheduler().global(() -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                refresh.request(player.getUniqueId());
            }
        }));
    }

    private void notifyChange(TagGroupUpdateEvent.Action action, String name, TagGroup group) {
        sync.get().publishGroupUpdate(name.toLowerCase(Locale.ROOT));
        platform.scheduler().global(() -> {
            Bukkit.getPluginManager().callEvent(new TagGroupUpdateEvent(name, action, group));
            List<UUID> online = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                online.add(player.getUniqueId());
            }
            refresh.requestAll(online);
        });
    }

    private static <T> CompletableFuture<T> failed(Throwable t) {
        CompletableFuture<T> future = new CompletableFuture<>();
        future.completeExceptionally(t);
        return future;
    }
}
