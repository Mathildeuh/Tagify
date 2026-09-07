package fr.mathildeuh.tagify.internal.integration;

import fr.mathildeuh.tagify.api.service.TagService;
import fr.mathildeuh.tagify.internal.config.TagifyConfig;
import fr.mathildeuh.tagify.internal.integration.luckperms.LuckPermsHook;
import fr.mathildeuh.tagify.internal.integration.permission.GroupProvider;
import fr.mathildeuh.tagify.internal.integration.placeholderapi.PapiPlaceholderBridge;
import fr.mathildeuh.tagify.internal.integration.placeholderapi.TagifyExpansion;
import fr.mathildeuh.tagify.internal.integration.vault.VaultHook;
import fr.mathildeuh.tagify.internal.tag.RefreshService;
import fr.mathildeuh.tagify.internal.text.PlaceholderBridge;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.logging.Level;

/**
 * Detects and wires the optional plugins (LuckPerms, PlaceholderAPI, Vault).
 * Each integration is guarded: a missing plugin or an error never blocks startup.
 */
public final class Integrations {

    private final Plugin plugin;
    private final Supplier<TagifyConfig> config;

    private LuckPermsHook luckPerms;
    private VaultHook vault;
    private TagifyExpansion expansion;

    private volatile GroupProvider groupProvider = GroupProvider.NONE;
    private volatile PlaceholderBridge placeholderBridge = PlaceholderBridge.NONE;

    public Integrations(Plugin plugin, Supplier<TagifyConfig> config) {
        this.plugin = plugin;
        this.config = config;
    }

    public void wire(RefreshService refresh, TagService tagService) {
        PluginManager pm = Bukkit.getPluginManager();
        TagifyConfig cfg = config.get();

        if (cfg.integrations().luckperms() && pm.isPluginEnabled("LuckPerms")) {
            try {
                luckPerms = LuckPermsHook.tryHook(plugin, refresh,
                        () -> config.get().refresh().onPermissionChange());
            } catch (Throwable t) {
                warn("LuckPerms", t);
            }
        }

        if (cfg.integrations().vault() && pm.isPluginEnabled("Vault")) {
            try {
                vault = VaultHook.tryHook(plugin);
            } catch (Throwable t) {
                warn("Vault", t);
            }
        }

        groupProvider = luckPerms != null ? luckPerms
                : (vault != null ? vault : GroupProvider.NONE);

        if (cfg.integrations().placeholderapi() && pm.isPluginEnabled("PlaceholderAPI")) {
            try {
                placeholderBridge = new PapiPlaceholderBridge();
                expansion = new TagifyExpansion(tagService);
                expansion.register();
            } catch (Throwable t) {
                warn("PlaceholderAPI", t);
                placeholderBridge = PlaceholderBridge.NONE;
            }
        }

        List<String> active = activeIntegrations();
        plugin.getLogger().info(active.isEmpty()
                ? "No third-party integration detected."
                : "Active integrations: " + String.join(", ", active) + ".");
    }

    public void shutdown() {
        if (luckPerms != null) {
            try {
                luckPerms.close();
            } catch (Throwable ignored) {
                // ignore
            }
            luckPerms = null;
        }
        if (expansion != null) {
            try {
                expansion.unregister();
            } catch (Throwable ignored) {
                // ignore
            }
            expansion = null;
        }
        groupProvider = GroupProvider.NONE;
        placeholderBridge = PlaceholderBridge.NONE;
    }

    public GroupProvider groupProvider() {
        return groupProvider;
    }

    public PlaceholderBridge placeholderBridge() {
        return placeholderBridge;
    }

    public List<String> activeIntegrations() {
        List<String> list = new ArrayList<>();
        if (luckPerms != null) {
            list.add("LuckPerms");
        }
        if (vault != null) {
            list.add("Vault");
        }
        if (expansion != null) {
            list.add("PlaceholderAPI");
        }
        return list;
    }

    private void warn(String name, Throwable t) {
        plugin.getLogger().log(Level.WARNING,
                "Integration " + name + " unavailable: " + t.getMessage());
        plugin.getLogger().log(Level.FINE, "Details", t);
    }
}
