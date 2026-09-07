package fr.mathildeuh.tagify.internal.integration.placeholderapi;

import fr.mathildeuh.tagify.api.model.PlayerTags;
import fr.mathildeuh.tagify.api.service.TagService;
import fr.mathildeuh.tagify.internal.BuildConstants;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Optional;

/**
 * Tagify's PlaceholderAPI expansion. Identifier: {@code tagify}.
 *
 * <p>Placeholders: {@code %tagify_prefix%}, {@code %tagify_prefix_mini%}, {@code %tagify_prefix_plain%},
 * likewise {@code suffix}, {@code %tagify_group%}, {@code %tagify_group_priority%},
 * {@code %tagify_group_count%}, {@code %tagify_has_prefix%}, {@code %tagify_has_suffix%},
 * {@code %tagify_has_tag%}.
 *
 * <p>The Premium module subclasses this to add extended placeholders.
 */
public class TagifyExpansion extends PlaceholderExpansion {

    protected static final LegacyComponentSerializer LEGACY =
            LegacyComponentSerializer.builder().character('§').hexColors().build();

    protected final TagService tags;

    public TagifyExpansion(TagService tags) {
        this.tags = tags;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "tagify";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Mathildeuh";
    }

    @Override
    public @NotNull String getVersion() {
        return BuildConstants.fullVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) {
            return "";
        }

        Optional<PlayerTags> cached = tags.resolveCached(player.getUniqueId());
        if (cached.isEmpty()) {
            tags.resolve(player.getUniqueId());
            return "";
        }
        PlayerTags resolved = cached.get();

        String base = baseRequest(resolved, params.toLowerCase(Locale.ROOT));
        if (base != null) {
            return base;
        }
        return extendedRequest(player, resolved, params.toLowerCase(Locale.ROOT));
    }

    protected @Nullable String baseRequest(PlayerTags resolved, String params) {
        return switch (params) {
            case "prefix" -> legacy(resolved.renderedPrefix());
            case "prefix_mini" -> mini(resolved.renderedPrefix());
            case "prefix_plain", "prefix_stripped" -> plain(resolved.renderedPrefix());
            case "suffix" -> legacy(resolved.renderedSuffix());
            case "suffix_mini" -> mini(resolved.renderedSuffix());
            case "suffix_plain", "suffix_stripped" -> plain(resolved.renderedSuffix());
            case "group" -> resolved.primaryGroup().map(g -> g.name()).orElse("");
            case "group_priority" -> String.valueOf(resolved.primaryGroup().map(g -> g.priority()).orElse(0));
            case "group_count" -> String.valueOf(resolved.groups().size());
            case "has_prefix" -> String.valueOf(resolved.prefix().isPresent());
            case "has_suffix" -> String.valueOf(resolved.suffix().isPresent());
            case "has_tag" -> String.valueOf(resolved.hasAnyTag());
            default -> null;
        };
    }

    /** Extension point for the Premium module. */
    protected @Nullable String extendedRequest(OfflinePlayer player, PlayerTags resolved, String params) {
        return null;
    }

    protected static String legacy(Optional<Component> component) {
        return component.map(LEGACY::serialize).orElse("");
    }

    protected static String mini(Optional<Component> component) {
        return component.map(c -> MiniMessage.miniMessage().serialize(c)).orElse("");
    }

    protected static String plain(Optional<Component> component) {
        return component.map(c -> PlainTextComponentSerializer.plainText().serialize(c)).orElse("");
    }
}
