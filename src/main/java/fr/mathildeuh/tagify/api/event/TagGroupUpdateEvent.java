package fr.mathildeuh.tagify.api.event;

import fr.mathildeuh.tagify.api.model.TagGroup;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Émis après la création, la modification ou la suppression d'un groupe de tags.
 */
public class TagGroupUpdateEvent extends Event {

    /** Nature du changement. */
    public enum Action {
        CREATE,
        MODIFY,
        DELETE
    }

    private static final HandlerList HANDLERS = new HandlerList();

    private final String groupName;
    private final Action action;
    private final @Nullable TagGroup group;

    public TagGroupUpdateEvent(@NotNull String groupName, @NotNull Action action, @Nullable TagGroup group) {
        this.groupName = groupName;
        this.action = action;
        this.group = group;
    }

    public @NotNull String getGroupName() {
        return groupName;
    }

    public @NotNull Action getAction() {
        return action;
    }

    /** Nouvel état du groupe, ou {@code null} en cas de {@link Action#DELETE}. */
    public @Nullable TagGroup getGroup() {
        return group;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS;
    }
}
