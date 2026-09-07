package fr.mathildeuh.tagify.internal.platform.nametag;

import org.bukkit.scoreboard.Team;

import java.util.Locale;

/**
 * Scoreboard team display options derived from the {@code display} config section.
 *
 * @param sortEnabled         embed an ordering token in the team name to control tab-list order
 * @param nameTagVisibility   name-tag visibility above the head
 * @param collisionRule       entity collision rule
 * @param dedicatedScoreboard use an isolated scoreboard (vs. the main one)
 * @param colorFromPrefix     colour the player name with the trailing colour of the prefix
 */
public record NametagOptions(
        boolean sortEnabled,
        Team.OptionStatus nameTagVisibility,
        Team.OptionStatus collisionRule,
        boolean dedicatedScoreboard,
        boolean colorFromPrefix
) {

    public static NametagOptions defaults() {
        return new NametagOptions(true, Team.OptionStatus.ALWAYS, Team.OptionStatus.NEVER, true, true);
    }

    public static Team.OptionStatus parseStatus(String raw, Team.OptionStatus fallback) {
        if (raw == null) {
            return fallback;
        }
        try {
            return Team.OptionStatus.valueOf(raw.trim().toUpperCase(Locale.ROOT).replace('-', '_'));
        } catch (IllegalArgumentException e) {
            return fallback;
        }
    }
}
