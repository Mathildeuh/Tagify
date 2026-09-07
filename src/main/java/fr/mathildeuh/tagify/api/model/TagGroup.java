package fr.mathildeuh.tagify.api.model;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * A tag group: a {@link Tag} applied to a set of players, with a priority used to break ties
 * when a player belongs to more than one group.
 *
 * <p>A player is a member of a group when they are:
 * <ul>
 *   <li>listed explicitly in {@link #members()};</li>
 *   <li>holding the {@code tagify.group.<name>} permission;</li>
 *   <li>a member of the LuckPerms group of the same name (when {@code groups.map-luckperms-groups} is on).</li>
 * </ul>
 *
 * <p>{@link #weight()} is a separate ordering value used only by the Premium tab-list sorting
 * modes; the Free edition orders the tab list by {@link #priority()}.
 *
 * <p>Immutable — use {@link #toBuilder()} to produce a modified copy.
 */
public final class TagGroup {

    private final String name;
    private final String key;
    private final Tag tag;
    private final int priority;
    private final int weight;
    private final @Unmodifiable Set<UUID> members;
    private final @Unmodifiable Map<String, String> metadata;

    private TagGroup(Builder builder) {
        this.name = Objects.requireNonNull(builder.name, "name");
        this.key = builder.name.toLowerCase(Locale.ROOT);
        this.tag = builder.tag;
        this.priority = builder.priority;
        this.weight = builder.weight;
        this.members = Collections.unmodifiableSet(new LinkedHashSet<>(builder.members));
        this.metadata = Collections.unmodifiableMap(new LinkedHashMap<>(builder.metadata));
    }

    public static Builder builder(String name) {
        return new Builder(name);
    }

    /** Display name (original casing). */
    public String name() {
        return name;
    }

    /** Normalised key (lower-case) used for indexing and storage. */
    public String key() {
        return key;
    }

    public Tag tag() {
        return tag;
    }

    public int priority() {
        return priority;
    }

    /** Explicit ordering weight (Premium tab-list sorting only; 0 by default). */
    public int weight() {
        return weight;
    }

    /** Explicit members (beyond permission / LuckPerms membership). */
    public @Unmodifiable Set<UUID> members() {
        return members;
    }

    /** Free-form metadata (used by the Premium module: conditions, expressions, ...). */
    public @Unmodifiable Map<String, String> metadata() {
        return metadata;
    }

    public boolean hasExplicitMember(UUID playerId) {
        return members.contains(playerId);
    }

    @Contract(pure = true)
    public Builder toBuilder() {
        return new Builder(name)
                .tag(tag)
                .priority(priority)
                .weight(weight)
                .members(members)
                .metadata(metadata);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof TagGroup other && key.equals(other.key);
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }

    @Override
    public String toString() {
        return "TagGroup{name=" + name + ", priority=" + priority + ", weight=" + weight + ", tag=" + tag + '}';
    }

    public static final class Builder {
        private final String name;
        private Tag tag = Tag.EMPTY;
        private int priority = 0;
        private int weight = 0;
        private final Set<UUID> members = new LinkedHashSet<>();
        private final Map<String, String> metadata = new LinkedHashMap<>();

        private Builder(String name) {
            this.name = Objects.requireNonNull(name, "name").trim();
            if (this.name.isEmpty()) {
                throw new IllegalArgumentException("A group name cannot be empty.");
            }
        }

        public Builder tag(Tag tag) {
            this.tag = tag == null ? Tag.EMPTY : tag;
            return this;
        }

        public Builder prefix(String prefix) {
            this.tag = this.tag.withPrefix(prefix);
            return this;
        }

        public Builder suffix(String suffix) {
            this.tag = this.tag.withSuffix(suffix);
            return this;
        }

        public Builder priority(int priority) {
            this.priority = priority;
            return this;
        }

        public Builder weight(int weight) {
            this.weight = weight;
            return this;
        }

        public Builder members(Set<UUID> members) {
            this.members.clear();
            if (members != null) {
                this.members.addAll(members);
            }
            return this;
        }

        public Builder addMember(UUID playerId) {
            this.members.add(playerId);
            return this;
        }

        public Builder removeMember(UUID playerId) {
            this.members.remove(playerId);
            return this;
        }

        public Builder metadata(Map<String, String> metadata) {
            this.metadata.clear();
            if (metadata != null) {
                this.metadata.putAll(metadata);
            }
            return this;
        }

        public Builder putMetadata(String key, String value) {
            this.metadata.put(key, value);
            return this;
        }

        public TagGroup build() {
            return new TagGroup(this);
        }
    }
}
