package fr.mathildeuh.tagify.api.event;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Émis juste avant l'application du préfixe / suffixe rendu à l'affichage du joueur.
 *
 * <p>Les autres plugins peuvent :
 * <ul>
 *   <li>modifier le préfixe / suffixe final via {@link #setPrefix(Component)} / {@link #setSuffix(Component)} ;</li>
 *   <li>annuler entièrement l'application via {@link #setCancelled(boolean)}.</li>
 * </ul>
 *
 * <p>Émis sur le thread qui applique le tag (thread principal, ou thread de région sous Folia).
 */
public class PreTagApplyEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private @Nullable Component prefix;
    private @Nullable Component suffix;
    private boolean cancelled;

    public PreTagApplyEvent(@NotNull Player player, @Nullable Component prefix, @Nullable Component suffix) {
        this.player = player;
        this.prefix = prefix;
        this.suffix = suffix;
    }

    public @NotNull Player getPlayer() {
        return player;
    }

    public @Nullable Component getPrefix() {
        return prefix;
    }

    public void setPrefix(@Nullable Component prefix) {
        this.prefix = prefix;
    }

    public @Nullable Component getSuffix() {
        return suffix;
    }

    public void setSuffix(@Nullable Component suffix) {
        this.suffix = suffix;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS;
    }
}
