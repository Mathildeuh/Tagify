package fr.mathildeuh.tagify.internal.integration.placeholderapi;

import fr.mathildeuh.tagify.internal.text.PlaceholderBridge;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.Nullable;

/** Résout les {@code %...%} via PlaceholderAPI avant l'analyse MiniMessage / legacy. */
public final class PapiPlaceholderBridge implements PlaceholderBridge {

    @Override
    public boolean hasPlaceholders(String text) {
        return text != null && text.indexOf('%') >= 0;
    }

    @Override
    public String apply(@Nullable OfflinePlayer player, String text) {
        return PlaceholderAPI.setPlaceholders(player, text);
    }
}
