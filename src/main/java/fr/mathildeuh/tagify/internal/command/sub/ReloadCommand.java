package fr.mathildeuh.tagify.internal.command.sub;

import fr.mathildeuh.tagify.internal.command.CommandContext;
import fr.mathildeuh.tagify.internal.command.SubCommand;

/** {@code /tagify reload}. */
public final class ReloadCommand implements SubCommand {

    @Override
    public String name() {
        return "reload";
    }

    @Override
    public String permission() {
        return "tagify.admin.reload";
    }

    @Override
    public String usageArgs() {
        return "";
    }

    @Override
    public String descriptionKey() {
        return "command.reload.description";
    }

    @Override
    public void run(CommandContext ctx) {
        try {
            ctx.core().reload();
            ctx.msg("command.reload.success");
        } catch (Exception e) {
            ctx.msg("command.reload.failed");
            ctx.core().plugin().getLogger().log(java.util.logging.Level.WARNING, "Reload failed", e);
        }
    }
}
