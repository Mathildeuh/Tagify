package fr.mathildeuh.tagify.internal.command.sub;

import fr.mathildeuh.tagify.internal.command.CommandContext;
import fr.mathildeuh.tagify.internal.command.CommandTab;
import fr.mathildeuh.tagify.internal.command.SubCommand;
import fr.mathildeuh.tagify.internal.util.Players;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

/** {@code /tagify remove <joueur> <prefix|suffix>}. */
public final class RemoveCommand implements SubCommand {

    @Override
    public String name() {
        return "remove";
    }

    @Override
    public List<String> aliases() {
        return List.of("unset", "delete");
    }

    @Override
    public String permission() {
        return "tagify.admin.remove";
    }

    @Override
    public String usageArgs() {
        return "<player> <prefix|suffix>";
    }

    @Override
    public String descriptionKey() {
        return "command.remove.description";
    }

    @Override
    public void run(CommandContext ctx) {
        if (ctx.argCount() < 2) {
            ctx.msg("command.remove.usage");
            return;
        }
        String targetName = ctx.arg(0);
        String field = ctx.arg(1).toLowerCase(Locale.ROOT);
        if (!field.equals("prefix") && !field.equals("suffix")) {
            ctx.msg("command.remove.usage");
            return;
        }

        Optional<UUID> target = Players.resolve(targetName);
        if (target.isEmpty()) {
            ctx.msg("command.player-unknown", Placeholder.unparsed("player", targetName));
            return;
        }
        UUID uuid = target.get();
        boolean prefix = field.equals("prefix");

        var future = prefix
                ? ctx.core().tags().setIndividualPrefix(uuid, null)
                : ctx.core().tags().setIndividualSuffix(uuid, null);

        future.whenComplete((v, error) -> ctx.core().platform().scheduler().global(() -> {
            if (error != null) {
                ctx.msg("command.error");
                return;
            }
            ctx.msg("command.remove.success",
                    Placeholder.unparsed("player", Players.nameOf(uuid)),
                    Placeholder.unparsed("field", field));
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
