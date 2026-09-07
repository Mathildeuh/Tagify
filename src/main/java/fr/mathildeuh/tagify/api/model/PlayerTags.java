package fr.mathildeuh.tagify.api.model;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Vue résolue des tags d'un joueur à un instant donné : préfixe / suffixe effectifs,
 * leur origine, le tag individuel éventuel et la liste des groupes du joueur.
 *
 * <p>Instance immuable — obtenue via {@link fr.mathildeuh.tagify.api.service.TagService#resolve(UUID)}
 * ou {@link fr.mathildeuh.tagify.api.service.TagService#resolveCached(UUID)}.
 */
public interface PlayerTags {

    UUID playerId();

    /** Préfixe effectif brut (format d'origine), s'il existe. */
    Optional<String> prefix();

    /** Suffixe effectif brut (format d'origine), s'il existe. */
    Optional<String> suffix();

    /** Préfixe effectif rendu en composant Adventure (placeholders résolus, formatage appliqué). */
    Optional<Component> renderedPrefix();

    /** Suffixe effectif rendu en composant Adventure. */
    Optional<Component> renderedSuffix();

    TagSource prefixSource();

    TagSource suffixSource();

    /** Groupe d'où provient le préfixe, si {@link #prefixSource()} vaut {@link TagSource#GROUP}. */
    Optional<TagGroup> prefixGroup();

    /** Groupe d'où provient le suffixe, si {@link #suffixSource()} vaut {@link TagSource#GROUP}. */
    Optional<TagGroup> suffixGroup();

    /** Tag défini individuellement pour ce joueur (persisté), s'il existe. */
    Optional<Tag> individualTag();

    /** Tag temporaire posé via l'API (non persisté), s'il existe. */
    Optional<Tag> temporaryTag();

    /** Tous les groupes du joueur, triés par priorité décroissante puis nom croissant. */
    @Unmodifiable List<TagGroup> groups();

    /** Groupe de plus haute priorité du joueur, s'il en a. */
    Optional<TagGroup> primaryGroup();

    /** {@code true} si le joueur a au moins un préfixe ou suffixe effectif. */
    default boolean hasAnyTag() {
        return prefix().isPresent() || suffix().isPresent();
    }
}
