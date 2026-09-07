package fr.mathildeuh.tagify.internal.command.sub;

import fr.mathildeuh.tagify.internal.command.CommandContext;
import fr.mathildeuh.tagify.internal.command.CommandTab;
import fr.mathildeuh.tagify.internal.command.SubCommand;
import fr.mathildeuh.tagify.internal.util.Players;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** {@code /tagify gui [joueur]} — ouvre l'interface d'administration. */
public final class GuiCommand implements SubCommand {

    @Override
    public String name() {
        return "gui";
    }

    @Override
    public List<String> aliases() {
        return List.of("menu");
    }

    @Override
    public String permission() {
        return "tagify.admin.gui";
    }

    @Override
    public boolean playerOnly() {
        return true;
    }

    @Override
    public String usageArgs() {
        return "[player]";
    }

    @Override
    public String descriptionKey() {
        return "command.gui.description";
    }

    @Override
    public void run(CommandContext ctx) {
        if (!ctx.core().config().guiEnabled()) {
            ctx.msg("command.gui.disabled");
            return;
        }
        if (ctx.argCount() >= 1) {
            String targetName = ctx.arg(0);
            Optional<UUID> target = Players.resolve(targetName);
            if (target.isEmpty()) {
                ctx.msg("command.player-unknown", Placeholder.unparsed("player", targetName));
                return;
            }
            ctx.core().guiProvider().openPlayerEditor(ctx.player(), target.get(), Players.nameOf(target.get()));
            return;
        }
        ctx.core().guiProvider().openMain(ctx.player());
    }

    @Override
    public List<String> tabComplete(CommandContext ctx) {
        return ctx.argCount() == 1 ? CommandTab.onlinePlayers() : List.of();
    }
}
