package fr.mathildeuh.tagify.internal.storage;

import fr.mathildeuh.tagify.api.model.Tag;
import fr.mathildeuh.tagify.api.model.TagGroup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Copie l'intégralité des données (groupes + tags individuels) d'un backend vers un autre.
 * La source n'est jamais modifiée. Utilisé par {@code /tagify convert <source> <cible>}.
 */
public final class StorageMigrator {

    /**
     * @param groups  groupes copiés avec succès
     * @param players tags individuels copiés avec succès
     * @param errors  erreurs non bloquantes rencontrées (élément par élément)
     */
    public record Result(int groups, int players, List<String> errors) {
        public boolean hasErrors() {
            return !errors.isEmpty();
        }
    }

    /**
     * L'orchestration s'exécute sur le pool commun (bloque sur {@code join()}), tandis que les
     * opérations de chaque backend utilisent leur propre exécuteur — pas de risque d'interblocage.
     */
    public CompletableFuture<Result> migrate(DataStore source, DataStore target) {
        return CompletableFuture.supplyAsync(() -> {
            List<String> errors = new ArrayList<>();

            source.init().join();
            target.init().join();

            int groupCount = 0;
            Collection<TagGroup> groups = source.loadGroups().join();
            for (TagGroup group : groups) {
                try {
                    target.saveGroup(group).join();
                    groupCount++;
                } catch (RuntimeException e) {
                    errors.add("Group '" + group.name() + "': " + rootMessage(e));
                }
            }

            int playerCount = 0;
            Map<UUID, Tag> players = source.loadAllPlayers().join();
            for (Map.Entry<UUID, Tag> entry : players.entrySet()) {
                try {
                    target.savePlayer(entry.getKey(), entry.getValue()).join();
                    playerCount++;
                } catch (RuntimeException e) {
                    errors.add("Player '" + entry.getKey() + "': " + rootMessage(e));
                }
            }

            return new Result(groupCount, playerCount, errors);
        });
    }

    private static String rootMessage(Throwable t) {
        Throwable cursor = t;
        while (cursor.getCause() != null && cursor.getCause() != cursor) {
            cursor = cursor.getCause();
        }
        String message = cursor.getMessage();
        return message != null ? message : cursor.getClass().getSimpleName();
    }
}
