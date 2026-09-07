package fr.mathildeuh.tagify.internal.importer;

import fr.mathildeuh.tagify.api.model.Tag;
import fr.mathildeuh.tagify.api.model.TagGroup;
import fr.mathildeuh.tagify.internal.storage.DataStore;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Imports the configuration of an old NametagEdit install ({@code plugins/NametagEdit/groups.yml}
 * + {@code players.yml}) into the active Tagify backend.
 *
 * <p>NametagEdit sorts priorities in ascending order (lower = stronger); Tagify does the
 * opposite. The conversion applies {@code tagify_priority = 1000 - nte_priority} (clamped).
 */
public final class NametagEditImporter {

    public record Result(int groups, int players, List<String> warnings) {
    }

    public CompletableFuture<Result> importFrom(File nametagEditFolder, DataStore target) {
        return CompletableFuture.supplyAsync(() -> {
            List<String> warnings = new ArrayList<>();
            if (nametagEditFolder == null || !nametagEditFolder.isDirectory()) {
                warnings.add("plugins/NametagEdit folder not found.");
                return new Result(0, 0, warnings);
            }

            int groupCount = importGroups(new File(nametagEditFolder, "groups.yml"), target, warnings);
            int playerCount = importPlayers(new File(nametagEditFolder, "players.yml"), target, warnings);
            return new Result(groupCount, playerCount, warnings);
        });
    }

    private int importGroups(File file, DataStore target, List<String> warnings) {
        if (!file.isFile()) {
            warnings.add("groups.yml missing - no group imported.");
            return 0;
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        int count = 0;

        List<Map<?, ?>> asList = yaml.getMapList("groups");
        if (!asList.isEmpty()) {
            for (Map<?, ?> raw : asList) {
                String name = str(raw.get("group"), raw.get("name"));
                if (name == null) {
                    warnings.add("Skipped a NametagEdit group with no name.");
                    continue;
                }
                int ntePriority = intOf(raw.get("priority"), intOf(raw.get("order"), 0));
                TagGroup group = TagGroup.builder(name)
                        .tag(Tag.of(str(raw.get("prefix")), str(raw.get("suffix"))))
                        .priority(clampPriority(1000 - ntePriority))
                        .build();
                target.saveGroup(group).join();
                count++;
            }
            return count;
        }

        ConfigurationSection section = yaml.getConfigurationSection("groups");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                ConfigurationSection group = section.getConfigurationSection(key);
                if (group == null) {
                    continue;
                }
                int ntePriority = group.getInt("priority", group.getInt("order", 0));
                TagGroup converted = TagGroup.builder(key)
                        .tag(Tag.of(group.getString("prefix"), group.getString("suffix")))
                        .priority(clampPriority(1000 - ntePriority))
                        .build();
                target.saveGroup(converted).join();
                count++;
            }
        }
        return count;
    }

    private int importPlayers(File file, DataStore target, List<String> warnings) {
        if (!file.isFile()) {
            warnings.add("players.yml missing - no individual tag imported.");
            return 0;
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection root = yaml.getConfigurationSection("players");
        if (root == null) {
            return 0;
        }
        int count = 0;
        for (String key : root.getKeys(false)) {
            ConfigurationSection entry = root.getConfigurationSection(key);
            if (entry == null) {
                continue;
            }
            try {
                UUID uuid = UUID.fromString(key);
                Tag tag = Tag.of(entry.getString("prefix"), entry.getString("suffix"));
                if (!tag.isEmpty()) {
                    target.savePlayer(uuid, tag).join();
                    count++;
                }
            } catch (IllegalArgumentException e) {
                warnings.add("Skipped an invalid player UUID: " + key);
            }
        }
        return count;
    }

    private static int clampPriority(int value) {
        return Math.max(-10_000, Math.min(10_000, value));
    }

    private static String str(Object... candidates) {
        for (Object candidate : candidates) {
            if (candidate instanceof String s && !s.isEmpty()) {
                return s;
            }
        }
        return null;
    }

    private static int intOf(Object value, int fallback) {
        if (value instanceof Number n) {
            return n.intValue();
        }
        if (value instanceof String s) {
            try {
                return Integer.parseInt(s.trim());
            } catch (NumberFormatException ignored) {
                return fallback;
            }
        }
        return fallback;
    }
}
