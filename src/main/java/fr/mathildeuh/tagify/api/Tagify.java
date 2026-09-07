package fr.mathildeuh.tagify.api;

import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Accès statique à l'{@link TagifyApi}.
 *
 * <p>Les plugins tiers doivent déclarer {@code Tagify} en {@code softdepend} ou {@code depend}
 * dans leur {@code plugin.yml} et compiler contre le jar de Tagify en {@code compileOnly}.
 */
public final class Tagify {

    private Tagify() {
    }

    /**
     * @return l'API Tagify
     * @throws IllegalStateException si Tagify n'est pas encore chargé
     */
    public static @NotNull TagifyApi get() {
        RegisteredServiceProvider<TagifyApi> rsp =
                Bukkit.getServicesManager().getRegistration(TagifyApi.class);
        if (rsp == null) {
            throw new IllegalStateException(
                    "The Tagify API is not available - is the Tagify plugin installed and loaded?");
        }
        return rsp.getProvider();
    }

    /** Variante non levée d'exception. */
    public static @NotNull Optional<TagifyApi> getIfLoaded() {
        RegisteredServiceProvider<TagifyApi> rsp =
                Bukkit.getServicesManager().getRegistration(TagifyApi.class);
        return Optional.ofNullable(rsp).map(RegisteredServiceProvider::getProvider);
    }

    public static boolean isAvailable() {
        return Bukkit.getServicesManager().getRegistration(TagifyApi.class) != null;
    }
}
