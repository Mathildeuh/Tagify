package fr.mathildeuh.tagify.internal.storage;

import fr.mathildeuh.tagify.api.model.Tag;
import fr.mathildeuh.tagify.api.model.TagGroup;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Contrat unique de persistance. Une implémentation par backend (FlatFile en Core ;
 * MySQL / PostgreSQL / MongoDB dans le module Premium).
 *
 * <p>Toutes les opérations sont asynchrones : aucune I/O sur le thread principal.
 */
public interface DataStore {

    String id();

    /** Prépare le backend (fichiers, schéma, pool de connexions). */
    CompletableFuture<Void> init();

    /** Tag individuel persisté d'un joueur, s'il en a un. */
    CompletableFuture<Optional<Tag>> loadPlayer(UUID playerId);

    /** Écrit (ou supprime si {@code tag} est {@code null} ou vide) le tag individuel d'un joueur. */
    CompletableFuture<Void> savePlayer(UUID playerId, @Nullable Tag tag);

    /** Tous les tags individuels (pour la migration et les rapports). */
    CompletableFuture<Map<UUID, Tag>> loadAllPlayers();

    /** Tous les groupes définis. */
    CompletableFuture<Collection<TagGroup>> loadGroups();

    CompletableFuture<Void> saveGroup(TagGroup group);

    CompletableFuture<Void> deleteGroup(String groupKey);

    /** Libère les ressources (connexions, tâches). */
    CompletableFuture<Void> close();
}
