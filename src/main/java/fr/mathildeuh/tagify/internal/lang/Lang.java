package fr.mathildeuh.tagify.internal.lang;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Localised messages in MiniMessage. Every render automatically injects the {@code <prefix>}
 * tag (plugin prefix defined by the {@code prefix} key of the language file).
 *
 * <p>The bundled {@code lang/en.yml} and {@code lang/fr.yml} files are copied into the plugin
 * folder on first start; missing keys fall back to the bundled English file.
 */
public final class Lang {

    private static final String DEFAULT_LANGUAGE = "en";

    private final Plugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    private YamlConfiguration active = new YamlConfiguration();
    private YamlConfiguration embedded = new YamlConfiguration();
    private String prefix = "";

    public Lang(Plugin plugin) {
        this.plugin = plugin;
    }

    public void load(String language) {
        File dir = new File(plugin.getDataFolder(), "lang");
        if (!dir.exists() && !dir.mkdirs()) {
            plugin.getLogger().warning("Could not create the lang/ folder");
        }
        saveBundled("lang/en.yml");
        saveBundled("lang/fr.yml");

        String lang = language == null ? DEFAULT_LANGUAGE : language.toLowerCase(Locale.ROOT);
        File target = new File(dir, lang + ".yml");
        if (!target.exists()) {
            plugin.getLogger().warning("Language '" + lang + "' not found - falling back to " + DEFAULT_LANGUAGE + ".");
            target = new File(dir, DEFAULT_LANGUAGE + ".yml");
        }
        this.active = YamlConfiguration.loadConfiguration(target);

        try (InputStream in = plugin.getResource("lang/" + DEFAULT_LANGUAGE + ".yml")) {
            if (in != null) {
                this.embedded = YamlConfiguration.loadConfiguration(
                        new InputStreamReader(in, StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            plugin.getLogger().warning("Could not read the bundled language file: " + e.getMessage());
        }

        this.prefix = rawOrNull("prefix") != null ? rawOrNull("prefix") : "";
    }

    private void saveBundled(String resourcePath) {
        if (plugin.getResource(resourcePath) == null) {
            return;
        }
        File out = new File(plugin.getDataFolder(), resourcePath);
        if (!out.exists()) {
            plugin.saveResource(resourcePath, false);
        }
    }

    private @Nullable String rawOrNull(String key) {
        String value = active.getString(key);
        if (value == null) {
            value = embedded.getString(key);
        }
        return value;
    }

    /** Raw text of a key (unparsed MiniMessage). Returns {@code [key]} if missing. */
    public String raw(String key) {
        String value = rawOrNull(key);
        return value != null ? value : "[" + key + "]";
    }

    public List<String> rawList(String key) {
        List<String> list = active.getStringList(key);
        if (list.isEmpty()) {
            list = embedded.getStringList(key);
        }
        return list;
    }

    /** Parses an arbitrary MiniMessage string with the {@code <prefix>} tag available. */
    public Component parse(String raw, TagResolver... resolvers) {
        TagResolver[] all = Arrays.copyOf(resolvers, resolvers.length + 1);
        all[resolvers.length] = Placeholder.parsed("prefix", prefix);
        return miniMessage.deserialize(raw, all);
    }

    public Component render(String key, TagResolver... resolvers) {
        return parse(raw(key), resolvers);
    }

    public List<Component> renderList(String key, TagResolver... resolvers) {
        List<Component> out = new ArrayList<>();
        for (String line : rawList(key)) {
            out.add(parse(line, resolvers));
        }
        return out;
    }

    public void send(CommandSender sender, String key, TagResolver... resolvers) {
        List<String> lines = rawList(key);
        if (!lines.isEmpty()) {
            for (String line : lines) {
                sender.sendMessage(parse(line, resolvers));
            }
            return;
        }
        String raw = rawOrNull(key);
        if (raw == null || raw.isEmpty()) {
            return;
        }
        sender.sendMessage(parse(raw, resolvers));
    }

    public Component prefixComponent() {
        return miniMessage.deserialize(prefix);
    }
}
