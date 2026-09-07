package fr.mathildeuh.tagify.internal.command.sub;

import fr.mathildeuh.tagify.internal.command.CommandContext;
import fr.mathildeuh.tagify.internal.command.CommandTab;
import fr.mathildeuh.tagify.internal.command.SubCommand;
import fr.mathildeuh.tagify.internal.storage.DataStore;
import fr.mathildeuh.tagify.internal.storage.StorageContext;
import fr.mathildeuh.tagify.internal.storage.StorageType;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

import java.util.List;
import java.util.Optional;

/** {@code /tagify convert <source> <cible>} — migre le stockage entre backends. */
public final class ConvertCommand implements SubCommand {

    @Override
    public String name() {
        return "convert";
    }

    @Override
    public String permission() {
        return "tagify.admin.convert";
    }

    @Override
    public String usageArgs() {
        return "<flatfile|mysql|postgresql|mongodb> <flatfile|mysql|postgresql|mongodb>";
    }

    @Override
    public String descriptionKey() {
        return "command.convert.description";
    }

    @Override
    public void run(CommandContext ctx) {
        if (ctx.argCount() < 2) {
            ctx.msg("command.convert.usage");
            return;
        }
        Optional<StorageType> source = StorageType.fromId(ctx.arg(0));
        Optional<StorageType> target = StorageType.fromId(ctx.arg(1));
        if (source.isEmpty() || target.isEmpty()) {
            ctx.msg("command.convert.unknown-type");
            return;
        }
        if (source.get() == target.get()) {
            ctx.msg("command.convert.same");
            return;
        }

        for (StorageType type : List.of(source.get(), target.get())) {
            if (!ctx.core().storageRegistry().isRegistered(type)) {
                ctx.msg("command.convert.unavailable", Placeholder.unparsed("type", type.id()));
                return;
            }
        }

        StorageContext storageCtx = new StorageContext(
                ctx.core().plugin(), ctx.core().platform().scheduler().asyncExecutor(), ctx.core().config());
        DataStore from = ctx.core().storageRegistry().create(source.get(), storageCtx).orElseThrow();
        DataStore to = ctx.core().storageRegistry().create(target.get(), storageCtx).orElseThrow();

        ctx.msg("command.convert.started",
                Placeholder.unparsed("source", source.get().id()),
                Placeholder.unparsed("target", target.get().id()));

        ctx.core().migrator().migrate(from, to).whenComplete((result, error) ->
                ctx.core().platform().scheduler().global(() -> {
                    closeQuietly(from);
                    closeQuietly(to);
                    if (error != null || result == null) {
                        ctx.msg("command.convert.failed");
                        ctx.core().plugin().getLogger().log(java.util.logging.Level.WARNING,
                                "Storage migration failed", error);
                        return;
                    }
                    ctx.msg("command.convert.done",
                            Placeholder.unparsed("groups", String.valueOf(result.groups())),
                            Placeholder.unparsed("players", String.valueOf(result.players())));
                    for (String warning : result.errors()) {
                        ctx.msg("command.convert.warning", Placeholder.unparsed("warning", warning));
                    }
                    ctx.msg("command.convert.reminder", Placeholder.unparsed("target", target.get().id()));
                }));
    }

    private static void closeQuietly(DataStore store) {
        try {
            store.close();
        } catch (Throwable ignored) {
            // ignore
        }
    }

    @Override
    public List<String> tabComplete(CommandContext ctx) {
        return ctx.argCount() <= 2 ? CommandTab.STORAGE_TYPES : List.of();
    }
}
