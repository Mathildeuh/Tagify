package fr.mathildeuh.tagify.internal.listener;

import fr.mathildeuh.tagify.internal.TagifyCore;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/** Charge / applique les tags à la connexion, nettoie à la déconnexion, rafraîchit au changement de monde. */
public final class PlayerConnectionListener implements Listener {

    private final TagifyCore core;

    public PlayerConnectionListener(TagifyCore core) {
        this.core = core;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onJoin(PlayerJoinEvent event) {
        core.tags().onJoin(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        core.tags().onQuit(event.getPlayer());
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        if (core.config().refresh().onWorldChange()) {
            core.tags().refresh(event.getPlayer().getUniqueId());
        }
    }
}
