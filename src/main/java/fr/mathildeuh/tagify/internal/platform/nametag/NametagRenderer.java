package fr.mathildeuh.tagify.internal.platform.nametag;

import fr.mathildeuh.tagify.api.model.PlayerTags;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

/**
 * Applies the rendered prefix / suffix to a player's display (tab list + above the head).
 * Chat is handled separately (placeholders / chat listener).
 */
public interface NametagRenderer {

    /** Prepares the display for a player who just joined. */
    void register(Player player);

    /**
     * Applies the rendered tag. A {@code null} prefix / suffix is treated as empty.
     * The tab-list ordering token is derived from {@code tags} via the active {@link TablistSort}.
     */
    void apply(Player player, @Nullable Component prefix, @Nullable Component suffix, PlayerTags tags);

    /** Removes a player's display (disconnect). */
    void remove(Player player);

    /** Resets every team (reload / shutdown). */
    void reset();
}
