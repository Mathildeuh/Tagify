package fr.mathildeuh.tagify.internal.platform;

import io.papermc.paper.ServerBuildInfo;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

/**
 * Détecte l'environnement serveur et fournit l'implémentation de scheduler adaptée.
 * Unique point du plugin qui « connaît » la plateforme (Bukkit / Paper / Folia).
 */
public final class Platform {

    private final Plugin plugin;
    private final boolean folia;
    private final boolean paper;
    private final PlatformScheduler scheduler;

    private Platform(Plugin plugin, boolean folia, boolean paper) {
        this.plugin = plugin;
        this.folia = folia;
        this.paper = paper;
        this.scheduler = folia ? new FoliaPlatformScheduler(plugin) : new BukkitPlatformScheduler(plugin);
    }

    public static Platform create(Plugin plugin) {
        return new Platform(plugin, detectFolia(), detectPaper());
    }

    private static boolean detectFolia() {
        try {
            return ServerBuildInfo.buildInfo().isBrandCompatible(Key.key("papermc", "folia"));
        } catch (Throwable ignoredModernApiMissing) {
            // Repli : classe interne présente uniquement sous Folia.
            return classPresent("io.papermc.paper.threadedregions.RegionizedServer");
        }
    }

    private static boolean detectPaper() {
        return classPresent("com.destroystokyo.paper.PaperConfig")
                || classPresent("io.papermc.paper.configuration.Configuration");
    }

    private static boolean classPresent(String name) {
        try {
            Class.forName(name);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public Plugin plugin() {
        return plugin;
    }

    public PlatformScheduler scheduler() {
        return scheduler;
    }

    public boolean isFolia() {
        return folia;
    }

    public boolean isPaper() {
        return paper;
    }

    public String describe() {
        String base = Bukkit.getName() + " " + Bukkit.getBukkitVersion();
        return folia ? base + " (Folia)" : base;
    }
}
