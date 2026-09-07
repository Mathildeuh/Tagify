package fr.mathildeuh.tagify.internal.platform.nametag;

import fr.mathildeuh.tagify.api.model.PlayerTags;
import fr.mathildeuh.tagify.api.model.TagGroup;
import org.bukkit.entity.Player;

import java.util.Locale;

/**
 * Produces the ordering token embedded in a player's scoreboard team name to control
 * tab-list order. A lexicographically <em>lower</em> token sorts <em>higher</em> in the list.
 *
 * <p>The Free edition only ships {@link #BY_PRIORITY}. The Premium module registers advanced
 * strategies (explicit weight, automatic mode) via {@code ModuleContext#setTablistSort}.
 */
@FunctionalInterface
public interface TablistSort {

    /** Ordering token — digits/letters, kept short (≤ 8 chars) so the team name fits 16 chars. */
    String orderToken(Player player, PlayerTags tags);

    /** Default: order by the player's primary (highest) group priority, descending. */
    TablistSort BY_PRIORITY = (player, tags) -> {
        int priority = tags.primaryGroup().map(TagGroup::priority).orElse(0);
        long value = 100_000L - clamp(priority);
        return String.format(Locale.ROOT, "%06d", value);
    };

    static int clamp(int value) {
        return Math.max(-99_999, Math.min(99_999, value));
    }
}
