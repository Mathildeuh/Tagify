package fr.mathildeuh.tagify.internal.command.sub;

import fr.mathildeuh.tagify.api.model.Tag;
import fr.mathildeuh.tagify.api.model.TagGroup;
import fr.mathildeuh.tagify.internal.command.CommandContext;
import fr.mathildeuh.tagify.internal.command.CommandTab;
import fr.mathildeuh.tagify.internal.command.SubCommand;
import fr.mathildeuh.tagify.internal.util.Players;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

/** {@code /tagify group <create|delete|list|set|priority|addplayer|removeplayer> ...}. */
public final class GroupCommand implements SubCommand {

    private static final List<String> FREE_ACTIONS =
            List.of("create", "delete", "list", "set", "priority", "addplayer", "removeplayer");

    /** {@code weight} and {@code condition} only take effect with the Premium module active. */
    private static final List<String> PREMIUM_ACTIONS = List.of("weight", "condition");

    private static final List<String> CONDITION_KEYS =
            List.of("worlds", "permission", "priority-boost-permission", "priority-boost");

    @Override
    public String name() {
        return "group";
    }

    @Override
    public String permission() {
        return "tagify.admin.group";
    }

    @Override
    public String usageArgs() {
        return "<create|delete|list|set|priority|weight|condition|addplayer|removeplayer> ...";
    }

    @Override
    public String descriptionKey() {
        return "command.group.description";
    }

    @Override
    public void run(CommandContext ctx) {
        if (ctx.argCount() < 1) {
            ctx.msg("command.group.usage");
            return;
        }
        String action = ctx.arg(0).toLowerCase(Locale.ROOT);
        switch (action) {
            case "list" -> list(ctx);
            case "create" -> create(ctx);
            case "delete" -> delete(ctx);
            case "set" -> set(ctx);
            case "priority" -> priority(ctx);
            case "weight" -> {
                if (requirePremium(ctx)) {
                    weight(ctx);
                }
            }
            case "condition" -> {
                if (requirePremium(ctx)) {
                    condition(ctx);
                }
            }
            case "addplayer" -> member(ctx, true);
            case "removeplayer" -> member(ctx, false);
            default -> ctx.msg("command.group.usage");
        }
    }

    /** {@code weight} and {@code condition} only take effect with the Premium module active. */
    private static boolean requirePremium(CommandContext ctx) {
        if (isPremiumActive(ctx)) {
            return true;
        }
        ctx.msg("command.group.premium-only");
        return false;
    }

    private static boolean isPremiumActive(CommandContext ctx) {
        return ctx.core().modules() != null && ctx.core().modules().isPremiumActive();
    }

    /**
     * One compact line per group (just its name) to avoid flooding the chat; priority, prefix,
     * suffix, member count — and, when set, weight and active conditions — are on hover.
     * Clicking a line suggests {@code /tagify group set <name> }.
     */
    private void list(CommandContext ctx) {
        List<TagGroup> groups = ctx.core().groups().getGroups();
        if (groups.isEmpty()) {
            ctx.msg("command.group.list-empty");
            return;
        }
        ctx.msg("command.group.list-header");
        for (TagGroup group : groups) {
            List<String> hoverLines = new ArrayList<>();
            hoverLines.add(ctx.lang().raw("command.group.list-hover.priority"));
            if (group.weight() != 0) {
                hoverLines.add(ctx.lang().raw("command.group.list-hover.weight"));
            }
            hoverLines.add(ctx.lang().raw("command.group.list-hover.prefix"));
            hoverLines.add(ctx.lang().raw("command.group.list-hover.suffix"));
            hoverLines.add(ctx.lang().raw("command.group.list-hover.members"));
            long conditionCount = group.metadata().keySet().stream()
                    .filter(key -> key.startsWith("condition.")).count();
            if (conditionCount > 0) {
                hoverLines.add(ctx.lang().raw("command.group.list-hover.conditions"));
            }

            Component hover = ctx.lang().parse(
                    String.join("<newline>", hoverLines),
                    Placeholder.unparsed("priority", String.valueOf(group.priority())),
                    Placeholder.unparsed("weight", String.valueOf(group.weight())),
                    Placeholder.parsed("gprefix", nullSafe(group.tag().prefix())),
                    Placeholder.parsed("gsuffix", nullSafe(group.tag().suffix())),
                    Placeholder.unparsed("members", String.valueOf(group.members().size())),
                    Placeholder.unparsed("count", String.valueOf(conditionCount)));

            Component line = ctx.lang()
                    .render("command.group.list-entry", Placeholder.unparsed("group", group.name()))
                    .hoverEvent(HoverEvent.showText(hover))
                    .clickEvent(ClickEvent.suggestCommand("/tagify group set " + group.name() + " "));

            ctx.sender().sendMessage(line);
        }
    }

    private void create(CommandContext ctx) {
        if (ctx.argCount() < 2) {
            ctx.msg("command.group.create-usage");
            return;
        }
        String name = ctx.arg(1);
        ctx.core().groups().createGroup(name).whenComplete((group, error) ->
                ctx.core().platform().scheduler().global(() -> {
                    if (error != null) {
                        ctx.msg("group.create-failed", Placeholder.unparsed("group", name));
                    } else {
                        ctx.msg("group.created", Placeholder.unparsed("group", name));
                    }
                }));
    }

    private void delete(CommandContext ctx) {
        if (ctx.argCount() < 2) {
            ctx.msg("command.group.delete-usage");
            return;
        }
        String name = ctx.arg(1);
        ctx.core().groups().deleteGroup(name).whenComplete((v, error) ->
                ctx.core().platform().scheduler().global(() -> {
                    if (error != null) {
                        ctx.msg("group.not-found", Placeholder.unparsed("group", name));
                    } else {
                        ctx.msg("group.deleted", Placeholder.unparsed("group", name));
                    }
                }));
    }

    private void set(CommandContext ctx) {
        if (ctx.argCount() < 4) {
            ctx.msg("command.group.set-usage");
            return;
        }
        String name = ctx.arg(1);
        String field = ctx.arg(2).toLowerCase(Locale.ROOT);
        if (!field.equals("prefix") && !field.equals("suffix")) {
            ctx.msg("command.group.set-usage");
            return;
        }
        Optional<TagGroup> group = ctx.core().groups().getGroup(name);
        if (group.isEmpty()) {
            ctx.msg("group.not-found", Placeholder.unparsed("group", name));
            return;
        }
        String value = ctx.rest(3);
        Tag current = group.get().tag();
        Tag updated = field.equals("prefix") ? current.withPrefix(value) : current.withSuffix(value);

        ctx.core().groups().setGroupTag(name, updated).whenComplete((v, error) ->
                ctx.core().platform().scheduler().global(() -> {
                    if (error != null) {
                        ctx.msg("command.error");
                    } else {
                        ctx.msg("command.group.set-success",
                                Placeholder.unparsed("group", name),
                                Placeholder.unparsed("field", field),
                                Placeholder.parsed("value", value));
                    }
                }));
    }

    private void priority(CommandContext ctx) {
        if (ctx.argCount() < 3) {
            ctx.msg("command.group.priority-usage");
            return;
        }
        String name = ctx.arg(1);
        int value;
        try {
            value = Integer.parseInt(ctx.arg(2));
        } catch (NumberFormatException e) {
            ctx.msg("command.group.priority-usage");
            return;
        }
        if (!ctx.core().groups().groupExists(name)) {
            ctx.msg("group.not-found", Placeholder.unparsed("group", name));
            return;
        }
        ctx.core().groups().setGroupPriority(name, value).whenComplete((v, error) ->
                ctx.core().platform().scheduler().global(() -> {
                    if (error != null) {
                        ctx.msg("command.error");
                    } else {
                        ctx.msg("command.group.priority-success",
                                Placeholder.unparsed("group", name),
                                Placeholder.unparsed("priority", String.valueOf(value)));
                    }
                }));
    }

    private void weight(CommandContext ctx) {
        if (ctx.argCount() < 3) {
            ctx.msg("command.group.weight-usage");
            return;
        }
        String name = ctx.arg(1);
        int value;
        try {
            value = Integer.parseInt(ctx.arg(2));
        } catch (NumberFormatException e) {
            ctx.msg("command.group.weight-usage");
            return;
        }
        if (!ctx.core().groups().groupExists(name)) {
            ctx.msg("group.not-found", Placeholder.unparsed("group", name));
            return;
        }
        ctx.core().groups().setGroupWeight(name, value).whenComplete((v, error) ->
                ctx.core().platform().scheduler().global(() -> {
                    if (error != null) {
                        ctx.msg("command.error");
                    } else {
                        ctx.msg("command.group.weight-success",
                                Placeholder.unparsed("group", name),
                                Placeholder.unparsed("weight", String.valueOf(value)));
                    }
                }));
    }

    private void condition(CommandContext ctx) {
        if (ctx.argCount() < 3) {
            ctx.msg("command.group.condition-usage");
            return;
        }
        String name = ctx.arg(1);
        String key = ctx.arg(2).toLowerCase(Locale.ROOT);
        if (!CONDITION_KEYS.contains(key)) {
            ctx.msg("command.group.condition-usage");
            return;
        }
        if (!ctx.core().groups().groupExists(name)) {
            ctx.msg("group.not-found", Placeholder.unparsed("group", name));
            return;
        }
        String value = ctx.rest(3);
        ctx.core().groups().setGroupMetadata(name, "condition." + key, value)
                .whenComplete((v, error) -> ctx.core().platform().scheduler().global(() -> {
                    if (error != null) {
                        ctx.msg("command.error");
                        return;
                    }
                    ctx.msg("command.group.condition-success",
                            Placeholder.unparsed("group", name),
                            Placeholder.unparsed("key", key),
                            Placeholder.parsed("value", value.isEmpty() ? "-" : value));
                }));
    }

    private void member(CommandContext ctx, boolean add) {
        if (ctx.argCount() < 3) {
            ctx.msg(add ? "command.group.addplayer-usage" : "command.group.removeplayer-usage");
            return;
        }
        String name = ctx.arg(1);
        String playerName = ctx.arg(2);
        if (!ctx.core().groups().groupExists(name)) {
            ctx.msg("group.not-found", Placeholder.unparsed("group", name));
            return;
        }
        Optional<UUID> target = Players.resolve(playerName);
        if (target.isEmpty()) {
            ctx.msg("command.player-unknown", Placeholder.unparsed("player", playerName));
            return;
        }
        UUID uuid = target.get();
        var future = add ? ctx.core().groups().addMember(name, uuid)
                : ctx.core().groups().removeMember(name, uuid);
        future.whenComplete((v, error) -> ctx.core().platform().scheduler().global(() -> {
            if (error != null) {
                ctx.msg("command.error");
                return;
            }
            ctx.core().tags().refresh(uuid);
            ctx.msg(add ? "command.group.addplayer-success" : "command.group.removeplayer-success",
                    Placeholder.unparsed("group", name),
                    Placeholder.unparsed("player", Players.nameOf(uuid)));
        }));
    }

    private static String nullSafe(String value) {
        return value == null ? "-" : value;
    }

    @Override
    public List<String> tabComplete(CommandContext ctx) {
        boolean premium = isPremiumActive(ctx);

        if (ctx.argCount() == 1) {
            if (!premium) {
                return FREE_ACTIONS;
            }
            List<String> all = new ArrayList<>(FREE_ACTIONS);
            all.addAll(PREMIUM_ACTIONS);
            return all;
        }

        String action = ctx.arg(0).toLowerCase(Locale.ROOT);
        if (!premium && PREMIUM_ACTIONS.contains(action)) {
            return List.of();
        }
        return switch (action) {
            case "delete", "set", "priority", "weight", "condition", "addplayer", "removeplayer" -> {
                if (ctx.argCount() == 2) {
                    yield CommandTab.groupNames(ctx.core());
                }
                if (ctx.argCount() == 3 && action.equals("set")) {
                    yield CommandTab.PREFIX_SUFFIX;
                }
                if (ctx.argCount() == 3 && action.equals("condition")) {
                    yield CONDITION_KEYS;
                }
                if (ctx.argCount() == 3 && (action.equals("addplayer") || action.equals("removeplayer"))) {
                    yield CommandTab.onlinePlayers();
                }
                yield List.of();
            }
            default -> List.of();
        };
    }
}
