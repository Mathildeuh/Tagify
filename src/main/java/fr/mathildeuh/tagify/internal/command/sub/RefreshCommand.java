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

/** {@code /tagify refresh [joueur|all]}. */
public final class RefreshCommand implements SubCommand {

    @Override
    public String name() {
        return "refresh";
    }

    @Override
    public String permission() {
        return "tagify.admin.refresh";
    }

    @Override
    public String usageArgs() {
        return "[player]";
    }

    @Override
    public String descriptionKey() {
        return "command.refresh.description";
    }

    @Override
    public void run(CommandContext ctx) {
        if (ctx.argCount() == 0 || ctx.arg(0).equalsIgnoreCase("all") || ctx.arg(0).equals("*")) {
            ctx.core().tags().refreshAll();
            ctx.msg("command.refresh.all");
            return;
        }
        String targetName = ctx.arg(0);
        Optional<UUID> target = Players.resolve(targetName);
        if (target.isEmpty()) {
            ctx.msg("command.player-unknown", Placeholder.unparsed("player", targetName));
            return;
        }
        ctx.core().tags().refresh(target.get());
        ctx.msg("command.refresh.player", Placeholder.unparsed("player", Players.nameOf(target.get())));
    }

    @Override
    public List<String> tabComplete(CommandContext ctx) {
        if (ctx.argCount() != 1) {
            return List.of();
        }
        List<String> options = new java.util.ArrayList<>(CommandTab.onlinePlayers());
        options.add("all");
        return options;
    }
}
