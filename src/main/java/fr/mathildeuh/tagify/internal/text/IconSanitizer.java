package fr.mathildeuh.tagify.internal.text;

import fr.mathildeuh.tagify.internal.util.Components;
import net.kyori.adventure.text.Component;

import java.util.regex.Pattern;

/**
 * Nettoie un composant avant application à l'affichage, afin d'éviter les plantages client
 * provoqués par des icônes / polices de plugins tiers (ItemsAdder, Oraxen, Nexo) mal formées :
 * retrait des caractères de contrôle et non-caractères, des sauts de ligne, et plafonnement
 * de longueur de sécurité.
 */
public final class IconSanitizer {

    private static final Pattern FORBIDDEN = Pattern.compile(
            "[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F\\x7F\\u0080-\\u009F\\uFFF9-\\uFFFB\\uFFFC\\uFFFE\\uFFFF\\r\\n]");

    private static final int HARD_LIMIT = 512;

    public Component sanitize(Component input) {
        if (input == null) {
            return Component.empty();
        }
        Component cleaned = input.replaceText(builder -> builder.match(FORBIDDEN).replacement(""));
        if (Components.plainLength(cleaned) > HARD_LIMIT) {
            String plain = Components.plain(cleaned);
            return Component.text(plain.substring(0, HARD_LIMIT));
        }
        return cleaned;
    }
}
