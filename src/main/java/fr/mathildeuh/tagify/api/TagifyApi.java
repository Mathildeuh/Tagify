package fr.mathildeuh.tagify.api;

import fr.mathildeuh.tagify.api.service.GroupService;
import fr.mathildeuh.tagify.api.service.TagService;

/**
 * Façade de l'API Tagify, enregistrée dans le {@link org.bukkit.plugin.ServicesManager} de Bukkit.
 *
 * <p>Récupération recommandée :
 * <pre>{@code
 * TagifyApi tagify = Tagify.get();
 * tagify.tags().resolve(uuid).thenAccept(tags ->
 *         tags.renderedPrefix().ifPresent(System.out::println));
 * }</pre>
 *
 * @see Tagify
 */
public interface TagifyApi {

    TagService tags();

    GroupService groups();

    /** Version complète du plugin, ex. {@code 1.4.2+build.87}. */
    String version();

    /** {@code true} si le module Premium est chargé et sous licence valide. */
    boolean isPremium();

    /** Nom du backend de stockage actif ({@code flatfile}, {@code mysql}, ...). */
    String storageBackend();
}
