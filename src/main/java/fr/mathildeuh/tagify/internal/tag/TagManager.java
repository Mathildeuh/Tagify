package fr.mathildeuh.tagify.internal.tag;

import fr.mathildeuh.tagify.api.event.PlayerTagUpdateEvent;
import fr.mathildeuh.tagify.api.model.PlayerTags;
import fr.mathildeuh.tagify.api.model.Tag;
import fr.mathildeuh.tagify.api.model.TagGroup;
import fr.mathildeuh.tagify.api.service.TagService;
import fr.mathildeuh.tagify.internal.config.TagifyConfig;
import fr.mathildeuh.tagify.internal.module.SyncService;
import fr.mathildeuh.tagify.internal.platform.Platform;
import fr.mathildeuh.tagify.internal.platform.nametag.NametagRenderer;
import fr.mathildeuh.tagify.internal.storage.DataStore;
import fr.mathildeuh.tagify.internal.text.TextPipeline;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Orchestrateur central : cache des tags individuels et résolus, calcul de l'effectif,
 * rendu et application à l'affichage, cycle de vie des joueurs.
 */
public final class TagManager implements TagService {

    private final Platform platform;
    private final Supplier<DataStore> store;
    private final TextPipeline text;
    private final NametagRenderer renderer;
    private final Supplier<TagifyConfig> config;
    private final Supplier<SyncService> sync;
    private final TagComputer computer = new TagComputer();
    private final RefreshService refresh;

    private final Map<UUID, Tag> individualCache = new ConcurrentHashMap<>();
    private final Map<UUID, Tag> temporary = new ConcurrentHashMap<>();
    private final Map<UUID, PlayerTagsImpl> resolved = new ConcurrentHashMap<>();

    private GroupManager groups;

    public TagManager(Platform platform, Supplier<DataStore> store, TextPipeline text,
                      NametagRenderer renderer, Supplier<TagifyConfig> config, Supplier<SyncService> sync) {
        this.platform = platform;
        this.store = store;
        this.text = text;
        this.renderer = renderer;
        this.config = config;
        this.sync = sync;
        this.refresh = new RefreshService(platform.scheduler(), this::applyNow,
                () -> config.get().refresh().debounceTicks());
    }

    public void setGroupManager(GroupManager groups) {
        this.groups = groups;
    }

    public RefreshService refreshService() {
        return refresh;
    }

    // ------------------------------------------------------------- cycle de vie

    public void onJoin(Player player) {
        UUID id = player.getUniqueId();
        renderer.register(player);
        store.get().loadPlayer(id).whenComplete((opt, error) -> {
            individualCache.put(id, error == null && opt != null ? opt.orElse(Tag.EMPTY) : Tag.EMPTY);
            refresh.requestImmediate(id);
        });
    }

    public void onQuit(Player player) {
        UUID id = player.getUniqueId();
        resolved.remove(id);
        temporary.remove(id);
        individualCache.remove(id);
        renderer.remove(player);
    }

    public void reloadAndReapply() {
        renderer.reset();
        for (Player player : Bukkit.getOnlinePlayers()) {
            refresh.request(player.getUniqueId());
        }
    }

    public void shutdown() {
        refresh.shutdown();
        renderer.reset();
    }

    // ------------------------------------------------------------- application

    /** Recalcule et applique le tag d'un joueur. Exécuté sur le thread global. */
    void applyNow(UUID playerId) {
        Player player = Bukkit.getPlayer(playerId);
        if (player == null || !player.isOnline() || groups == null) {
            return;
        }

        Tag individual = individualCache.get(playerId);
        if (individual == null) {
            store.get().loadPlayer(playerId).whenComplete((opt, error) -> {
                individualCache.put(playerId, error == null && opt != null ? opt.orElse(Tag.EMPTY) : Tag.EMPTY);
                refresh.requestImmediate(playerId);
            });
            return;
        }

        Tag temp = temporary.getOrDefault(playerId, Tag.EMPTY);
        List<TagGroup> playerGroups = groups.getGroupsOf(playerId);
        TagComputer.Resolution resolution = computer.compute(individual, temp, playerGroups);

        PlayerTagsImpl tags = new PlayerTagsImpl(playerId, resolution, individual, temp, playerGroups, text);
        resolved.put(playerId, tags);

        TagifyConfig.Display display = config.get().display();
        boolean touchScoreboard = display.applyToTablist() || display.applyToNametag();

        if (touchScoreboard) {
            Component prefix = tags.renderPrefixComponent();
            Component suffix = tags.renderSuffixComponent();
            renderer.apply(player, prefix, suffix, tags);
        } else {
            renderer.remove(player);
        }

        Bukkit.getPluginManager().callEvent(new PlayerTagUpdateEvent(player, tags));
    }

    // ------------------------------------------------------------- TagService

    @Override
    public CompletableFuture<PlayerTags> resolve(UUID playerId) {
        return currentIndividual(playerId).thenApply(individual -> {
            Tag temp = temporary.getOrDefault(playerId, Tag.EMPTY);
            List<TagGroup> playerGroups = groups == null ? List.of() : groups.getGroupsOf(playerId);
            TagComputer.Resolution resolution = computer.compute(individual, temp, playerGroups);
            PlayerTagsImpl tags = new PlayerTagsImpl(playerId, resolution, individual, temp, playerGroups, text);
            resolved.put(playerId, tags);
            return tags;
        });
    }

    @Override
    public Optional<PlayerTags> resolveCached(UUID playerId) {
        return Optional.ofNullable(resolved.get(playerId));
    }

    @Override
    public CompletableFuture<Void> setIndividualPrefix(UUID playerId, @Nullable String prefix) {
        return currentIndividual(playerId)
                .thenCompose(current -> setIndividualTag(playerId, current.withPrefix(prefix)));
    }

    @Override
    public CompletableFuture<Void> setIndividualSuffix(UUID playerId, @Nullable String suffix) {
        return currentIndividual(playerId)
                .thenCompose(current -> setIndividualTag(playerId, current.withSuffix(suffix)));
    }

    @Override
    public CompletableFuture<Void> setIndividualTag(UUID playerId, Tag tag) {
        Tag normalized = tag == null ? Tag.EMPTY : tag;
        individualCache.put(playerId, normalized);
        return store.get().savePlayer(playerId, normalized.isEmpty() ? null : normalized)
                .thenRun(() -> {
                    refresh.requestImmediate(playerId);
                    sync.get().publishPlayerUpdate(playerId);
                });
    }

    /** Applique une mise à jour reçue d'un autre serveur (via {@link SyncService}). */
    public void onRemoteUpdate(UUID playerId) {
        individualCache.remove(playerId);
        if (Bukkit.getPlayer(playerId) != null) {
            refresh.request(playerId);
        }
    }

    @Override
    public CompletableFuture<Void> clearIndividual(UUID playerId) {
        return setIndividualTag(playerId, Tag.EMPTY);
    }

    @Override
    public void setTemporaryTag(UUID playerId, @Nullable Tag tag) {
        if (tag == null || tag.isEmpty()) {
            temporary.remove(playerId);
        } else {
            temporary.put(playerId, tag);
        }
        refresh.requestImmediate(playerId);
    }

    @Override
    public void refresh(UUID playerId) {
        refresh.request(playerId);
    }

    @Override
    public void refreshAll() {
        List<UUID> online = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            online.add(player.getUniqueId());
        }
        refresh.requestAll(online);
    }

    @Override
    public Optional<Component> renderPrefix(UUID playerId) {
        return resolveCached(playerId).flatMap(PlayerTags::renderedPrefix);
    }

    @Override
    public Optional<Component> renderSuffix(UUID playerId) {
        return resolveCached(playerId).flatMap(PlayerTags::renderedSuffix);
    }

    // ------------------------------------------------------------- interne

    private CompletableFuture<Tag> currentIndividual(UUID playerId) {
        Tag cached = individualCache.get(playerId);
        if (cached != null) {
            return CompletableFuture.completedFuture(cached);
        }
        return store.get().loadPlayer(playerId)
                .thenApply(opt -> {
                    Tag tag = opt.orElse(Tag.EMPTY);
                    individualCache.put(playerId, tag);
                    return tag;
                });
    }
}
