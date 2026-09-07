package fr.mathildeuh.tagify.internal.command.sub;

import fr.mathildeuh.tagify.internal.command.CommandContext;
import fr.mathildeuh.tagify.internal.command.CommandTab;
import fr.mathildeuh.tagify.internal.command.SubCommand;
import fr.mathildeuh.tagify.internal.util.Components;
import fr.mathildeuh.tagify.internal.util.Players;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

/** {@code /tagify set <joueur> <prefix|suffix> <valeur>}. */
public final class SetCommand implements SubCommand {

    @Override
    public String name() {
        return "set";
    }

    @Override
    public String permission() {
        return "tagify.admin.set";
    }

    @Override
    public String usageArgs() {
        return "<player> <prefix|suffix> <value>";
    }

    @Override
    public String descriptionKey() {
        return "command.set.description";
    }

    @Override
    public void run(CommandContext ctx) {
        if (ctx.argCount() < 3) {
            ctx.msg("command.set.usage");
            return;
        }
        String targetName = ctx.arg(0);
        String field = ctx.arg(1).toLowerCase(Locale.ROOT);
        if (!field.equals("prefix") && !field.equals("suffix")) {
            ctx.msg("command.set.usage");
            return;
        }
        String value = ctx.rest(2);

        Optional<UUID> target = Players.resolve(targetName);
        if (target.isEmpty()) {
            ctx.msg("command.player-unknown", Placeholder.unparsed("player", targetName));
            return;
        }
        UUID uuid = target.get();

        boolean prefix = field.equals("prefix");
        int max = prefix ? ctx.core().config().display().maxPrefixLength()
                : ctx.core().config().display().maxSuffixLength();
        int length = Components.plainLength(ctx.core().text().renderPlain(value));
        if (length > max && !ctx.has("tagify.bypass.length")) {
            ctx.msg("command.set.too-long",
                    Placeholder.unparsed("max", String.valueOf(max)),
                    Placeholder.unparsed("length", String.valueOf(length)));
            return;
        }

        var future = prefix
                ? ctx.core().tags().setIndividualPrefix(uuid, value)
                : ctx.core().tags().setIndividualSuffix(uuid, value);

        future.whenComplete((v, error) -> ctx.core().platform().scheduler().global(() -> {
            if (error != null) {
                ctx.msg("command.error");
                return;
            }
            ctx.msg("command.set.success",
                    Placeholder.unparsed("player", Players.nameOf(uuid)),
                    Placeholder.unparsed("field", field),
                    Placeholder.parsed("value", value));
        }));
    }

    @Override
    public List<String> tabComplete(CommandContext ctx) {
        return switch (ctx.argCount()) {
            case 1 -> CommandTab.onlinePlayers();
            case 2 -> CommandTab.PREFIX_SUFFIX;
            default -> List.of();
        };
    }
}
