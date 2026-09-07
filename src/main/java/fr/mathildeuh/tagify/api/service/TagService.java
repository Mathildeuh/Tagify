package fr.mathildeuh.tagify.api.service;

import fr.mathildeuh.tagify.api.model.PlayerTags;
import fr.mathildeuh.tagify.api.model.Tag;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Point d'entrée pour lire et modifier les tags des joueurs.
 *
 * <p>Toutes les méthodes renvoyant un {@link CompletableFuture} effectuent leurs I/O hors
 * du thread principal ; le future se complète sur un thread arbitraire. Les méthodes
 * {@code *Cached} sont synchrones et non bloquantes (retour immédiat, éventuellement vide).
 */
public interface TagService {

    /**
     * Résout les tags effectifs d'un joueur (individuel &gt; groupe par priorité), en
     * chargeant les données depuis le stockage si nécessaire.
     */
    CompletableFuture<PlayerTags> resolve(UUID playerId);

    /**
     * Renvoie la dernière résolution connue pour ce joueur sans toucher au stockage.
     * Vide si le joueur n'est pas en cache (hors-ligne et jamais chargé).
     */
    Optional<PlayerTags> resolveCached(UUID playerId);

    /** Définit (ou retire si {@code null}) le préfixe individuel persisté d'un joueur. */
    CompletableFuture<Void> setIndividualPrefix(UUID playerId, @Nullable String prefix);

    /** Définit (ou retire si {@code null}) le suffixe individuel persisté d'un joueur. */
    CompletableFuture<Void> setIndividualSuffix(UUID playerId, @Nullable String suffix);

    /** Définit le tag individuel complet d'un joueur (préfixe + suffixe). */
    CompletableFuture<Void> setIndividualTag(UUID playerId, Tag tag);

    /** Retire entièrement le tag individuel d'un joueur. */
    CompletableFuture<Void> clearIndividual(UUID playerId);

    /**
     * Pose un tag temporaire (non persisté, prioritaire sur tout le reste), retiré à la
     * déconnexion. {@code null} le retire.
     */
    void setTemporaryTag(UUID playerId, @Nullable Tag tag);

    /** Programme un rafraîchissement (débouncé) de l'affichage du joueur. */
    void refresh(UUID playerId);

    /** Programme un rafraîchissement de tous les joueurs connectés. */
    void refreshAll();

    /** Préfixe effectif rendu, pour intégration directe (chat, hologrammes, etc.). */
    Optional<Component> renderPrefix(UUID playerId);

    /** Suffixe effectif rendu. */
    Optional<Component> renderSuffix(UUID playerId);
}
