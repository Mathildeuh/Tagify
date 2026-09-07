package fr.mathildeuh.tagify.internal.metrics;

import fr.mathildeuh.tagify.internal.TagifyCore;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;

import java.util.logging.Level;

/**
 * Statistiques anonymes bStats (activées par {@code advanced.metrics}).
 * Le package {@code org.bstats} est relocalisé au build pour éviter les conflits.
 *
 * <p>Deux projets bStats distincts selon la distribution : Free (33907) et Premium (33906).
 * L'ID est choisi d'après la présence du module Premium dans le jar, pas d'après l'état de la licence.
 */
public final class TagifyMetrics {

    private static final int FREE_PLUGIN_ID = 33907;
    private static final int PREMIUM_PLUGIN_ID = 33906;

    private Metrics metrics;

    public void start(TagifyCore core) {
        try {
            boolean premiumJar = core.modules() != null && core.modules().isPremiumPresent();
            int pluginId = premiumJar ? PREMIUM_PLUGIN_ID : FREE_PLUGIN_ID;
            this.metrics = new Metrics(core.plugin(), pluginId);

            metrics.addCustomChart(new SimplePie("storage_backend",
                    () -> core.currentDataStore() == null ? "unknown" : core.currentDataStore().id()));
            metrics.addCustomChart(new SimplePie("edition",
                    () -> core.modules() != null && core.modules().isPremiumActive() ? "Premium" : "Free"));
            metrics.addCustomChart(new SimplePie("luckperms",
                    () -> core.integrations().activeIntegrations().contains("LuckPerms") ? "yes" : "no"));
            metrics.addCustomChart(new SimplePie("placeholderapi",
                    () -> core.integrations().activeIntegrations().contains("PlaceholderAPI") ? "yes" : "no"));
            metrics.addCustomChart(new SimplePie("folia",
                    () -> core.platform().isFolia() ? "yes" : "no"));
            metrics.addCustomChart(new SimplePie("group_count_bucket",
                    () -> bucket(core.groups().getGroups().size())));
        } catch (Throwable t) {
            core.plugin().getLogger().log(Level.FINE, "bStats not initialised", t);
        }
    }

    public void stop() {
        if (metrics != null) {
            try {
                metrics.shutdown();
            } catch (Throwable ignored) {
                // certaines versions n'exposent pas shutdown()
            }
            metrics = null;
        }
    }

    private static String bucket(int count) {
        if (count == 0) {
            return "0";
        }
        if (count <= 5) {
            return "1-5";
        }
        if (count <= 15) {
            return "6-15";
        }
        if (count <= 50) {
            return "16-50";
        }
        return "50+";
    }
}
