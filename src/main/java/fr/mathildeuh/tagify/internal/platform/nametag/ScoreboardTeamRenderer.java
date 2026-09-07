package fr.mathildeuh.tagify.internal.platform.nametag;

import fr.mathildeuh.tagify.api.event.PreTagApplyEvent;
import fr.mathildeuh.tagify.api.model.PlayerTags;
import fr.mathildeuh.tagify.internal.platform.Platform;
import fr.mathildeuh.tagify.internal.util.Components;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.logging.Level;

/**
 * Nametag rendering through Bukkit's Scoreboard Team API — one team per player, whose name
 * encodes an ordering token (from {@link TablistSort}) to control tab-list order.
 *
 * <p>Every scoreboard mutation goes through {@code platform.scheduler().global(...)}
 * (required on Folia, harmless on Bukkit).
 */
public final class ScoreboardTeamRenderer implements NametagRenderer {

    private static final String TEAM_PREFIX = "tf_";

    private final Platform platform;
    private final Supplier<NametagOptions> options;
    private final Supplier<TablistSort> sort;
    private final Map<UUID, String> teamNames = new ConcurrentHashMap<>();

    private Scoreboard scoreboard;

    public ScoreboardTeamRenderer(Platform platform, Supplier<NametagOptions> options,
                                  Supplier<TablistSort> sort) {
        this.platform = platform;
        this.options = options;
        this.sort = sort;
        this.scoreboard = resolveScoreboard(options.get());
    }

    private Scoreboard resolveScoreboard(NametagOptions opts) {
        var manager = Bukkit.getScoreboardManager();
        return opts.dedicatedScoreboard() ? manager.getNewScoreboard() : manager.getMainScoreboard();
    }

    @Override
    public void register(Player player) {
        platform.scheduler().global(() -> {
            try {
                player.setScoreboard(scoreboard);
            } catch (Throwable t) {
                platform.plugin().getLogger().log(Level.FINE, "setScoreboard failed for " + player.getName(), t);
            }
        });
    }

    @Override
    public void apply(Player player, @Nullable Component prefix, @Nullable Component suffix, PlayerTags tags) {
        platform.scheduler().global(() -> applySync(player, prefix, suffix, tags));
    }

    private void applySync(Player player, @Nullable Component prefix, @Nullable Component suffix, PlayerTags tags) {
        if (!player.isOnline()) {
            return;
        }

        PreTagApplyEvent event = new PreTagApplyEvent(player, prefix, suffix);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }
        Component finalPrefix = event.getPrefix();
        Component finalSuffix = event.getSuffix();

        NametagOptions opts = options.get();
        String desired = teamName(player, tags, opts);
        String current = teamNames.get(player.getUniqueId());

        if (current != null && !current.equals(desired)) {
            detach(player, current);
        }

        try {
            Team team = scoreboard.getTeam(desired);
            if (team == null) {
                team = scoreboard.registerNewTeam(desired);
            }

            team.prefix(finalPrefix == null ? Component.empty() : finalPrefix);
            team.suffix(finalSuffix == null ? Component.empty() : finalSuffix);

            if (opts.colorFromPrefix()) {
                NamedTextColor color = Components.trailingColor(finalPrefix);
                if (color != null) {
                    team.color(color);
                }
            }

            team.setOption(Team.Option.NAME_TAG_VISIBILITY, opts.nameTagVisibility());
            team.setOption(Team.Option.COLLISION_RULE, opts.collisionRule());

            if (!team.hasEntry(player.getName())) {
                team.addEntry(player.getName());
            }
            teamNames.put(player.getUniqueId(), desired);
        } catch (IllegalStateException unregistered) {
            teamNames.remove(player.getUniqueId());
        } catch (Throwable t) {
            platform.plugin().getLogger().log(Level.WARNING,
                    "Could not apply the tag of " + player.getName(), t);
        }
    }

    @Override
    public void remove(Player player) {
        String name = teamNames.remove(player.getUniqueId());
        if (name == null) {
            return;
        }
        platform.scheduler().global(() -> detach(player, name));
    }

    private void detach(Player player, String teamName) {
        try {
            Team team = scoreboard.getTeam(teamName);
            if (team != null) {
                team.removeEntry(player.getName());
                if (team.getEntries().isEmpty()) {
                    team.unregister();
                }
            }
        } catch (IllegalStateException ignored) {
            // already unregistered
        }
    }

    @Override
    public void reset() {
        platform.scheduler().global(() -> {
            for (Team team : scoreboard.getTeams()) {
                if (team.getName().startsWith(TEAM_PREFIX)) {
                    try {
                        team.unregister();
                    } catch (IllegalStateException ignored) {
                        // ignore
                    }
                }
            }
            teamNames.clear();
            this.scoreboard = resolveScoreboard(options.get());
            for (Player online : Bukkit.getOnlinePlayers()) {
                online.setScoreboard(scoreboard);
            }
        });
    }

    /**
     * Team name: {@code tf_} + ordering token (from {@link TablistSort}) + player-name fragment,
     * bounded to 16 chars. On the unlikely collision, falls back to the player's UUID fragment.
     */
    private String teamName(Player player, PlayerTags tags, NametagOptions opts) {
        String uuidFragment = player.getUniqueId().toString().replace("-", "");

        if (!opts.sortEnabled()) {
            String candidate = TEAM_PREFIX + trim(sanitize(player.getName()), 13);
            return ensureUnique(candidate, player, () -> TEAM_PREFIX + trim(uuidFragment, 13));
        }

        String token = trim(sanitize(sort.get().orderToken(player, tags)), 9);
        String base = TEAM_PREFIX + token;
        int remaining = Math.max(0, 16 - base.length());
        String candidate = base + trim(sanitize(player.getName()), remaining);
        return ensureUnique(candidate, player, () -> base + trim(uuidFragment, remaining));
    }

    private String ensureUnique(String candidate, Player player, Supplier<String> fallback) {
        Team existing = scoreboard.getTeam(candidate);
        if (existing == null || existing.hasEntry(player.getName()) || existing.getEntries().isEmpty()) {
            return candidate;
        }
        return fallback.get();
    }

    private static String sanitize(String name) {
        return name.replaceAll("[^A-Za-z0-9_]", "");
    }

    private static String trim(String value, int max) {
        return value.length() <= max ? value : value.substring(0, max);
    }
}
