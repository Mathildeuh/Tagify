package fr.mathildeuh.tagify.internal.util;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

/** Résolution de joueurs par nom sans appel bloquant à l'API Mojang. */
public final class Players {

    private Players() {
    }

    /**
     * Résout un nom en UUID : joueur connecté d'abord, sinon cache local du serveur.
     * Vide si le joueur n'a jamais rejoint ce serveur.
     */
    public static Optional<UUID> resolve(String name) {
        Player online = Bukkit.getPlayerExact(name);
        if (online != null) {
            return Optional.of(online.getUniqueId());
        }
        OfflinePlayer cached = Bukkit.getOfflinePlayerIfCached(name);
        return cached != null ? Optional.of(cached.getUniqueId()) : Optional.empty();
    }

    public static String nameOf(UUID playerId) {
        Player online = Bukkit.getPlayer(playerId);
        if (online != null) {
            return online.getName();
        }
        String cached = Bukkit.getOfflinePlayer(playerId).getName();
        return cached != null ? cached : playerId.toString().substring(0, 8);
    }

    public static @Nullable Player online(UUID playerId) {
        return Bukkit.getPlayer(playerId);
    }
}
