package fr.mathildeuh.tagify.internal.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Locale;

/**
 * Immutable snapshot of {@code config.yml}. Rebuilt on every reload; consumers go through
 * {@link ConfigManager#get()} (or a {@code Supplier}).
 */
public final class TagifyConfig {

    public enum TextFormat { AUTO, MINIMESSAGE, LEGACY }

    /** Tab-list ordering strategy. {@code WEIGHT} and {@code AUTOMATIC} require the Premium module. */
    public enum SortMode {
        PRIORITY, WEIGHT, AUTOMATIC, DISABLED;

        public boolean requiresPremium() {
            return this == WEIGHT || this == AUTOMATIC;
        }

        public static SortMode parse(String raw) {
            if (raw == null) {
                return PRIORITY;
            }
            try {
                return valueOf(raw.trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                return PRIORITY;
            }
        }
    }

    public record Sql(String host, int port, String database, String username, String password,
                      int poolSize, String extraProperties) {
    }

    public record Mongo(String uri, String database) {
    }

    public record Storage(String type, Sql mysql, Sql postgresql, Mongo mongodb) {
    }

    public record Redis(boolean enabled, String host, int port, String password, String channel) {
    }

    public record Refresh(boolean onPermissionChange, boolean onWorldChange,
                          int intervalSeconds, int debounceTicks) {
    }

    public record TablistSort(SortMode mode, int defaultWeight, String automaticTiebreaker, boolean reverse) {
    }

    public record Display(boolean applyToTablist, boolean applyToNametag, boolean applyToChat,
                          String chatFormat, int maxPrefixLength, int maxSuffixLength,
                          TextFormat format, TablistSort tablistSort, String nameTagVisibility,
                          String collisionRule, boolean dedicatedScoreboard, boolean colorFromPrefix) {
    }

    public record Groups(boolean mapLuckPermsGroups, String luckPermsGroupFilter,
                         int defaultPriority) {
    }

    public record Integrations(boolean luckperms, boolean placeholderapi, boolean vault,
                               boolean protocollib, boolean sanitizeThirdPartyIcons,
                               boolean resolveIconPlaceholders) {
    }

    public record Premium(String verifyMode, String builtByBitToken, int graceDays) {
    }

    public record Advanced(int asyncPoolSize, boolean metrics) {
    }

    private final Storage storage;
    private final Redis redis;
    private final Refresh refresh;
    private final Display display;
    private final Groups groups;
    private final Integrations integrations;
    private final Premium premium;
    private final Advanced advanced;
    private final boolean guiEnabled;
    private final String language;
    private final boolean debug;

    private TagifyConfig(FileConfiguration c) {
        ConfigurationSection root = c;

        this.storage = new Storage(
                str(root, "storage.type", "flatfile").toLowerCase(Locale.ROOT),
                sql(root, "storage.mysql", 3306),
                sql(root, "storage.postgresql", 5432),
                new Mongo(str(root, "storage.mongodb.uri", "mongodb://localhost:27017"),
                        str(root, "storage.mongodb.database", "tagify")));

        this.redis = new Redis(
                bool(root, "cache.redis.enabled", false),
                str(root, "cache.redis.host", "localhost"),
                (int) num(root, "cache.redis.port", 6379),
                str(root, "cache.redis.password", ""),
                str(root, "cache.redis.channel", "tagify:sync"));

        this.refresh = new Refresh(
                bool(root, "refresh.on-permission-change", true),
                bool(root, "refresh.on-world-change", true),
                (int) num(root, "refresh.interval-seconds", 0),
                Math.max(1, (int) num(root, "refresh.debounce-ticks", 10)));

        TablistSort tablistSort = new TablistSort(
                SortMode.parse(str(root, "display.tablist-sort.mode", "priority")),
                (int) num(root, "display.tablist-sort.default-weight", 0),
                str(root, "display.tablist-sort.automatic-tiebreaker", "name").toLowerCase(Locale.ROOT),
                bool(root, "display.tablist-sort.reverse", false));

        this.display = new Display(
                bool(root, "display.apply-to-tablist", true),
                bool(root, "display.apply-to-nametag", true),
                bool(root, "display.apply-to-chat", false),
                str(root, "display.chat-format", "<prefix><name><suffix> <dark_gray>»</dark_gray> <message>"),
                (int) num(root, "display.max-prefix-length", 32),
                (int) num(root, "display.max-suffix-length", 32),
                textFormat(str(root, "display.format", "auto")),
                tablistSort,
                str(root, "display.nametag-visibility", "ALWAYS"),
                str(root, "display.collision-rule", "NEVER"),
                !"main".equalsIgnoreCase(str(root, "display.scoreboard", "dedicated")),
                bool(root, "display.color-from-prefix", true));

        this.groups = new Groups(
                bool(root, "groups.map-luckperms-groups", true),
                str(root, "groups.luckperms-group-filter", ""),
                (int) num(root, "groups.default-priority", 0));

        this.integrations = new Integrations(
                bool(root, "integrations.luckperms", true),
                bool(root, "integrations.placeholderapi", true),
                bool(root, "integrations.vault", false),
                bool(root, "integrations.protocollib", false),
                bool(root, "integrations.sanitize-third-party-icons", true),
                bool(root, "integrations.resolve-icon-placeholders", true));

        this.premium = new Premium(
                str(root, "premium.verify-mode", "builtbybit").toLowerCase(Locale.ROOT),
                str(root, "premium.builtbybit-token", ""),
                Math.max(0, (int) num(root, "premium.grace-days", 7)));

        this.advanced = new Advanced(
                Math.max(1, (int) num(root, "advanced.async-pool-size", 4)),
                bool(root, "advanced.metrics", true));

        this.guiEnabled = bool(root, "gui.enabled", true);
        this.language = str(root, "language", "en").toLowerCase(Locale.ROOT);
        this.debug = bool(root, "debug", false);
    }

    static TagifyConfig from(FileConfiguration configuration) {
        return new TagifyConfig(configuration);
    }

    private static Sql sql(ConfigurationSection root, String path, int defaultPort) {
        return new Sql(
                str(root, path + ".host", "localhost"),
                (int) num(root, path + ".port", defaultPort),
                str(root, path + ".database", "tagify"),
                str(root, path + ".username", "tagify"),
                str(root, path + ".password", ""),
                Math.max(1, (int) num(root, path + ".pool-size", 10)),
                str(root, path + ".properties", ""));
    }

    private static TextFormat textFormat(String raw) {
        try {
            return TextFormat.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return TextFormat.AUTO;
        }
    }

    private static String str(ConfigurationSection s, String path, String def) {
        return s.getString(path, def);
    }

    private static boolean bool(ConfigurationSection s, String path, boolean def) {
        return s.getBoolean(path, def);
    }

    private static double num(ConfigurationSection s, String path, double def) {
        return s.getDouble(path, def);
    }

    public Storage storage() {
        return storage;
    }

    public Redis redis() {
        return redis;
    }

    public Refresh refresh() {
        return refresh;
    }

    public Display display() {
        return display;
    }

    public Groups groups() {
        return groups;
    }

    public Integrations integrations() {
        return integrations;
    }

    public Premium premium() {
        return premium;
    }

    public Advanced advanced() {
        return advanced;
    }

    public boolean guiEnabled() {
        return guiEnabled;
    }

    public String language() {
        return language;
    }

    public boolean debug() {
        return debug;
    }
}
