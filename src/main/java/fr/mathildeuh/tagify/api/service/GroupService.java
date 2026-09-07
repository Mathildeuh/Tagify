package fr.mathildeuh.tagify.api.service;

import fr.mathildeuh.tagify.api.model.Tag;
import fr.mathildeuh.tagify.api.model.TagGroup;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Tag group management.
 *
 * <p>Reads ({@link #getGroup(String)}, {@link #getGroups()}) are served from the in-memory
 * cache and are synchronous. Writes are asynchronous and persisted.
 */
public interface GroupService {

    Optional<TagGroup> getGroup(String name);

    @Unmodifiable List<TagGroup> getGroups();

    boolean groupExists(String name);

    /** Creates an empty group. Fails if a group with the same name already exists. */
    CompletableFuture<TagGroup> createGroup(String name);

    /**
     * Deletes a group. Online players that inherited from it are refreshed, and explicit
     * memberships are cleaned up.
     */
    CompletableFuture<Void> deleteGroup(String name);

    /** Replaces a group's whole definition. */
    CompletableFuture<Void> saveGroup(TagGroup group);

    CompletableFuture<Void> setGroupTag(String name, Tag tag);

    CompletableFuture<Void> setGroupPriority(String name, int priority);

    /** Sets the group ordering weight (only affects the tab list under the Premium sorting modes). */
    CompletableFuture<Void> setGroupWeight(String name, int weight);

    CompletableFuture<Void> addMember(String name, UUID playerId);

    CompletableFuture<Void> removeMember(String name, UUID playerId);

    /**
     * Returns every group a player belongs to (explicit membership, {@code tagify.group.<name>}
     * permission, or LuckPerms group), sorted by descending priority then ascending name.
     */
    List<TagGroup> getGroupsOf(UUID playerId);
}
