package fr.mathildeuh.tagify.api.event;

import fr.mathildeuh.tagify.api.model.PlayerTags;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Émis après qu'un changement de tag effectif d'un joueur a été calculé et appliqué à
 * l'affichage (tab list, nametag, chat). Purement informatif — non annulable.
 */
public class PlayerTagUpdateEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final PlayerTags tags;

    public PlayerTagUpdateEvent(@NotNull Player player, @NotNull PlayerTags tags) {
        this.player = player;
        this.tags = tags;
    }

    public @NotNull Player getPlayer() {
        return player;
    }

    public @NotNull PlayerTags getTags() {
        return tags;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS;
    }
}
