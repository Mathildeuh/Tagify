package fr.mathildeuh.tagify.internal.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

/**
 * Charge, expose et recharge {@code config.yml}. Conserve un instantané {@link TagifyConfig}
 * courant, remplacé atomiquement au rechargement.
 */
public final class ConfigManager {

    private final Plugin plugin;
    private final File file;
    private volatile TagifyConfig current;

    public ConfigManager(Plugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "config.yml");
    }

    public void load() {
        if (!file.exists()) {
            plugin.saveResource("config.yml", false);
        }
        this.current = parse();
    }

    public void reload() {
        this.current = parse();
    }

    public TagifyConfig get() {
        TagifyConfig snapshot = current;
        if (snapshot == null) {
            load();
            snapshot = current;
        }
        return snapshot;
    }

    private TagifyConfig parse() {
        FileConfiguration configuration = YamlConfiguration.loadConfiguration(file);
        applyDefaults(configuration);
        return TagifyConfig.from(configuration);
    }

    /** Complète les clés manquantes à partir du {@code config.yml} embarqué (montées de version en douceur). */
    private void applyDefaults(FileConfiguration configuration) {
        try (InputStream in = plugin.getResource("config.yml")) {
            if (in == null) {
                return;
            }
            FileConfiguration defaults = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(in, StandardCharsets.UTF_8));
            configuration.setDefaults(defaults);
            configuration.options().copyDefaults(true);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Could not read the bundled config.yml", e);
        }
    }
}
