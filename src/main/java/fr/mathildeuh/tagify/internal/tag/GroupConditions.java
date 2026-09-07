package fr.mathildeuh.tagify.internal.tag;

import fr.mathildeuh.tagify.api.model.TagGroup;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

/**
 * Per-player evaluation of a group: does it apply right now, and with what effective priority?
 *
 * <p>The Free edition uses {@link #NONE} (every group always applies at its declared priority).
 * The Premium module registers an implementation that reads conditions from
 * {@link TagGroup#metadata()} (world restrictions, permission gates, priority boosts, ...) via
 * {@code ModuleContext#setGroupConditions}.
 */
@FunctionalInterface
public interface GroupConditions {

    GroupConditions NONE = (player, group) -> new Decision(true, group.priority());

    /**
     * @param player the player (may be {@code null} for offline resolution)
     * @param group  the candidate group
     */
    Decision evaluate(@Nullable Player player, TagGroup group);

    /**
     * @param applies            whether the group applies to this player right now
     * @param effectivePriority  priority to use for ordering (defaults to the group's priority)
     */
    record Decision(boolean applies, int effectivePriority) {
    }
}
