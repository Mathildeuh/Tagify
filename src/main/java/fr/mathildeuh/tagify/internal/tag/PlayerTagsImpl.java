package fr.mathildeuh.tagify.internal.tag;

import fr.mathildeuh.tagify.api.model.PlayerTags;
import fr.mathildeuh.tagify.api.model.Tag;
import fr.mathildeuh.tagify.api.model.TagGroup;
import fr.mathildeuh.tagify.api.model.TagSource;
import fr.mathildeuh.tagify.internal.text.TextPipeline;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Implémentation immuable de {@link PlayerTags} ; le rendu est paresseux. */
public final class PlayerTagsImpl implements PlayerTags {

    private final UUID playerId;
    private final TagComputer.Resolution resolution;
    private final Tag individual;
    private final Tag temporary;
    private final List<TagGroup> groups;
    private final TextPipeline text;

    public PlayerTagsImpl(UUID playerId, TagComputer.Resolution resolution, Tag individual,
                          Tag temporary, List<TagGroup> groups, TextPipeline text) {
        this.playerId = playerId;
        this.resolution = resolution;
        this.individual = individual;
        this.temporary = temporary;
        this.groups = List.copyOf(groups);
        this.text = text;
    }

    public TagComputer.Resolution resolution() {
        return resolution;
    }

    private OfflinePlayer offlinePlayer() {
        return Bukkit.getOfflinePlayer(playerId);
    }

    @Override
    public UUID playerId() {
        return playerId;
    }

    @Override
    public Optional<String> prefix() {
        return Optional.ofNullable(resolution.prefix());
    }

    @Override
    public Optional<String> suffix() {
        return Optional.ofNullable(resolution.suffix());
    }

    @Override
    public Optional<Component> renderedPrefix() {
        return prefix().map(raw -> text.render(offlinePlayer(), raw));
    }

    @Override
    public Optional<Component> renderedSuffix() {
        return suffix().map(raw -> text.render(offlinePlayer(), raw));
    }

    @Override
    public TagSource prefixSource() {
        return resolution.prefixSource();
    }

    @Override
    public TagSource suffixSource() {
        return resolution.suffixSource();
    }

    @Override
    public Optional<TagGroup> prefixGroup() {
        return Optional.ofNullable(resolution.prefixGroup());
    }

    @Override
    public Optional<TagGroup> suffixGroup() {
        return Optional.ofNullable(resolution.suffixGroup());
    }

    @Override
    public Optional<Tag> individualTag() {
        return individual.isEmpty() ? Optional.empty() : Optional.of(individual);
    }

    @Override
    public Optional<Tag> temporaryTag() {
        return temporary.isEmpty() ? Optional.empty() : Optional.of(temporary);
    }

    @Override
    public @Unmodifiable List<TagGroup> groups() {
        return groups;
    }

    @Override
    public Optional<TagGroup> primaryGroup() {
        return groups.isEmpty() ? Optional.empty() : Optional.of(groups.get(0));
    }

    public @Nullable Component renderPrefixComponent() {
        return prefix().map(raw -> text.render(offlinePlayer(), raw)).orElse(null);
    }

    public @Nullable Component renderSuffixComponent() {
        return suffix().map(raw -> text.render(offlinePlayer(), raw)).orElse(null);
    }
}
