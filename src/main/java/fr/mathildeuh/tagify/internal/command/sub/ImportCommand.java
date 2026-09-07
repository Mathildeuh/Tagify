package fr.mathildeuh.tagify.internal.command.sub;

import fr.mathildeuh.tagify.internal.command.CommandContext;
import fr.mathildeuh.tagify.internal.command.SubCommand;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

import java.io.File;
import java.util.List;
import java.util.Locale;

/** {@code /tagify import legacy} — importe la config d'un ancien NametagEdit. */
public final class ImportCommand implements SubCommand {

    @Override
    public String name() {
        return "import";
    }

    @Override
    public String permission() {
        return "tagify.admin.import";
    }

    @Override
    public String usageArgs() {
        return "legacy";
    }

    @Override
    public String descriptionKey() {
        return "command.import.description";
    }

    @Override
    public void run(CommandContext ctx) {
        if (ctx.argCount() < 1 || !ctx.arg(0).toLowerCase(Locale.ROOT).equals("legacy")) {
            ctx.msg("command.import.usage");
            return;
        }

        File pluginsDir = ctx.core().plugin().getDataFolder().getParentFile();
        File nametagEdit = new File(pluginsDir, "NametagEdit");
        if (!nametagEdit.isDirectory()) {
            ctx.msg("command.import.not-found");
            return;
        }

        ctx.msg("command.import.started");
        ctx.core().importer().importFrom(nametagEdit, ctx.core().currentDataStore())
                .whenComplete((result, error) -> ctx.core().platform().scheduler().global(() -> {
                    if (error != null || result == null) {
                        ctx.msg("command.import.failed");
                        ctx.core().plugin().getLogger().log(java.util.logging.Level.WARNING,
                                "NametagEdit import failed", error);
                        return;
                    }
                    ctx.core().groups().onRemoteUpdate();
                    ctx.msg("command.import.done",
                            Placeholder.unparsed("groups", String.valueOf(result.groups())),
                            Placeholder.unparsed("players", String.valueOf(result.players())));
                    for (String warning : result.warnings()) {
                        ctx.msg("command.import.warning", Placeholder.unparsed("warning", warning));
                    }
                }));
    }

    @Override
    public List<String> tabComplete(CommandContext ctx) {
        return ctx.argCount() == 1 ? List.of("legacy") : List.of();
    }
}
