package fr.mathildeuh.tagify.internal.text;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.Nullable;

/**
 * Pont vers un moteur de placeholders externe (PlaceholderAPI). Résout les {@code %...%}
 * <em>avant</em> l'analyse MiniMessage / legacy.
 */
public interface PlaceholderBridge {

    PlaceholderBridge NONE = new PlaceholderBridge() {
        @Override
        public boolean hasPlaceholders(String text) {
            return false;
        }

        @Override
        public String apply(@Nullable OfflinePlayer player, String text) {
            return text;
        }
    };

    boolean hasPlaceholders(String text);

    String apply(@Nullable OfflinePlayer player, String text);
}
