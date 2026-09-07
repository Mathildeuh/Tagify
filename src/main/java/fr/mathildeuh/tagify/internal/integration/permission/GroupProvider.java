package fr.mathildeuh.tagify.internal.integration.permission;

import java.util.Set;
import java.util.UUID;

/**
 * Source des groupes d'un joueur telle que vue par le plugin de permissions
 * (LuckPerms ou Vault). Sert au mappage « groupe de permissions → groupe de tags ».
 *
 * <p>Indépendant de la permission {@code tagify.group.<nom>}, qui est vérifiée
 * directement via {@code Player#hasPermission} et fonctionne avec n'importe quel backend.
 */
@FunctionalInterface
public interface GroupProvider {

    GroupProvider NONE = playerId -> Set.of();

    /** Noms bruts des groupes du joueur (peut être vide, jamais {@code null}). */
    Set<String> groupsOf(UUID playerId);
}
