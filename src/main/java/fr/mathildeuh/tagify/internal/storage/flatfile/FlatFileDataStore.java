package fr.mathildeuh.tagify.internal.storage.flatfile;

import fr.mathildeuh.tagify.api.model.Tag;
import fr.mathildeuh.tagify.api.model.TagGroup;
import fr.mathildeuh.tagify.internal.storage.DataStore;
import fr.mathildeuh.tagify.internal.storage.GroupCodec;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * Backend FlatFile — deux fichiers YAML dans {@code plugins/Tagify/data/} :
 * {@code players.yml} (tags individuels) et {@code groups.yml}.
 *
 * <p>Toutes les opérations sont sérialisées sur un exécuteur mono-thread <b>dédié</b> (et non
 * le pool asynchrone de Bukkit) : les écritures ne sont donc jamais annulées par l'arrêt du
 * plugin, et {@link #close()} les draine avant de rendre la main.
 */
public final class FlatFileDataStore implements DataStore {

    private final Plugin plugin;
    private final ExecutorService io = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "tagify-flatfile-io");
        thread.setDaemon(true);
        return thread;
    });

    private final File playersFile;
    private final File groupsFile;

    private volatile YamlConfiguration players;
    private volatile YamlConfiguration groups;

    public FlatFileDataStore(Plugin plugin) {
        this.plugin = plugin;
        File dir = new File(plugin.getDataFolder(), "data");
        this.playersFile = new File(dir, "players.yml");
        this.groupsFile = new File(dir, "groups.yml");
    }

    @Override
    public String id() {
        return "flatfile";
    }

    @Override
    public CompletableFuture<Void> init() {
        return CompletableFuture.runAsync(() -> {
            File dir = playersFile.getParentFile();
            if (!dir.exists() && !dir.mkdirs()) {
                plugin.getLogger().warning("Could not create " + dir);
            }
            this.players = YamlConfiguration.loadConfiguration(playersFile);
            this.groups = YamlConfiguration.loadConfiguration(groupsFile);
        }, io);
    }

    @Override
    public CompletableFuture<Optional<Tag>> loadPlayer(UUID playerId) {
        return CompletableFuture.supplyAsync(() -> {
            ConfigurationSection section = players.getConfigurationSection(playerId.toString());
            if (section == null) {
                return Optional.empty();
            }
            Tag tag = Tag.of(section.getString("prefix"), section.getString("suffix"));
            return tag.isEmpty() ? Optional.empty() : Optional.of(tag);
        }, io);
    }

    @Override
    public CompletableFuture<Void> savePlayer(UUID playerId, @Nullable Tag tag) {
        return CompletableFuture.runAsync(() -> {
            String key = playerId.toString();
            if (tag == null || tag.isEmpty()) {
                players.set(key, null);
            } else {
                ConfigurationSection section = players.createSection(key);
                section.set("prefix", tag.prefix());
                section.set("suffix", tag.suffix());
            }
            save(players, playersFile);
        }, io);
    }

    @Override
    public CompletableFuture<Map<UUID, Tag>> loadAllPlayers() {
        return CompletableFuture.supplyAsync(() -> {
            Map<UUID, Tag> result = new LinkedHashMap<>();
            for (String key : players.getKeys(false)) {
                ConfigurationSection section = players.getConfigurationSection(key);
                if (section == null) {
                    continue;
                }
                try {
                    Tag tag = Tag.of(section.getString("prefix"), section.getString("suffix"));
                    if (!tag.isEmpty()) {
                        result.put(UUID.fromString(key), tag);
                    }
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid UUID in players.yml: " + key);
                }
            }
            return result;
        }, io);
    }

    @Override
    public CompletableFuture<Collection<TagGroup>> loadGroups() {
        return CompletableFuture.supplyAsync(() -> {
            Collection<TagGroup> result = new ArrayList<>();
            ConfigurationSection root = groups.getConfigurationSection("groups");
            if (root != null) {
                for (String key : root.getKeys(false)) {
                    ConfigurationSection section = root.getConfigurationSection(key);
                    if (section != null) {
                        result.add(GroupCodec.read(section, key, plugin.getLogger()));
                    }
                }
            }
            return result;
        }, io);
    }

    @Override
    public CompletableFuture<Void> saveGroup(TagGroup group) {
        return CompletableFuture.runAsync(() -> {
            ConfigurationSection section = groups.createSection("groups." + group.key());
            GroupCodec.write(section, group);
            save(groups, groupsFile);
        }, io);
    }

    @Override
    public CompletableFuture<Void> deleteGroup(String groupKey) {
        return CompletableFuture.runAsync(() -> {
            groups.set("groups." + groupKey, null);
            save(groups, groupsFile);
        }, io);
    }

    @Override
    public CompletableFuture<Void> close() {
        // Draine toutes les écritures en attente, effectue une sauvegarde finale, puis arrête.
        io.execute(() -> {
            if (players != null) {
                save(players, playersFile);
            }
            if (groups != null) {
                save(groups, groupsFile);
            }
        });
        io.shutdown();
        try {
            if (!io.awaitTermination(5, TimeUnit.SECONDS)) {
                plugin.getLogger().warning("FlatFile write did not finish within the allotted time.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return CompletableFuture.completedFuture(null);
    }

    private void save(YamlConfiguration configuration, File file) {
        try {
            configuration.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to write " + file.getName(), e);
        }
    }
}
