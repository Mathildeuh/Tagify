package fr.mathildeuh.tagify.internal.text;

import fr.mathildeuh.tagify.internal.config.TagifyConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * Transforme une chaîne brute (MiniMessage ou legacy {@code &}/{@code §}) en {@link Component},
 * en résolvant d'abord les placeholders externes puis en assainissant le résultat.
 *
 * <p>Analyse défensive : une chaîne MiniMessage invalide retombe sur l'analyse legacy, puis
 * sur du texte brut — jamais d'exception propagée à l'appelant.
 */
public final class TextPipeline {

    private static final Pattern MINIMESSAGE_TAG = Pattern.compile("<[a-zA-Z#!/][^<>]*>");

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.builder()
            .character('&')
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();

    private static final MiniMessage MINI = MiniMessage.miniMessage();

    private final Supplier<TagifyConfig> config;
    private final Supplier<PlaceholderBridge> placeholders;
    private final IconSanitizer sanitizer = new IconSanitizer();

    public TextPipeline(Supplier<TagifyConfig> config, Supplier<PlaceholderBridge> placeholders) {
        this.config = config;
        this.placeholders = placeholders;
    }

    /** Rend une chaîne pour un joueur donné (placeholders résolus dans son contexte). */
    public Component render(@Nullable OfflinePlayer viewer, @Nullable String raw, TagResolver... extra) {
        if (raw == null || raw.isEmpty()) {
            return Component.empty();
        }

        String text = raw;
        PlaceholderBridge bridge = placeholders.get();
        if (bridge != null && bridge.hasPlaceholders(text)) {
            try {
                text = bridge.apply(viewer, text);
            } catch (Throwable ignored) {
                text = raw;
            }
        }

        Component component = parse(text, resolveFormat(), extra);

        if (config.get().integrations().sanitizeThirdPartyIcons()) {
            component = sanitizer.sanitize(component);
        }
        return component;
    }

    public Component renderPlain(@Nullable String raw, TagResolver... extra) {
        return render(null, raw, extra);
    }

    private TagifyConfig.TextFormat resolveFormat() {
        return config.get().display().format();
    }

    private Component parse(String text, TagifyConfig.TextFormat format, TagResolver... extra) {
        TagifyConfig.TextFormat effective =
                format == TagifyConfig.TextFormat.AUTO ? detect(text) : format;

        if (effective == TagifyConfig.TextFormat.LEGACY) {
            return LEGACY.deserialize(text);
        }
        try {
            return MINI.deserialize(text, TagResolver.resolver(extra));
        } catch (RuntimeException miniFailed) {
            try {
                return LEGACY.deserialize(text);
            } catch (RuntimeException legacyFailed) {
                return Component.text(text);
            }
        }
    }

    private static TagifyConfig.TextFormat detect(String text) {
        if (MINIMESSAGE_TAG.matcher(text).find()) {
            return TagifyConfig.TextFormat.MINIMESSAGE;
        }
        if (text.indexOf('&') >= 0 || text.indexOf('§') >= 0) {
            return TagifyConfig.TextFormat.LEGACY;
        }
        return TagifyConfig.TextFormat.MINIMESSAGE;
    }
}
