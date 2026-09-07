package fr.mathildeuh.tagify.internal.command.sub;

import fr.mathildeuh.tagify.api.model.PlayerTags;
import fr.mathildeuh.tagify.internal.command.CommandContext;
import fr.mathildeuh.tagify.internal.command.CommandTab;
import fr.mathildeuh.tagify.internal.command.SubCommand;
import fr.mathildeuh.tagify.internal.util.Players;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/** {@code /tagify info <joueur>} — tags actifs et leur origine. */
public final class InfoCommand implements SubCommand {

    @Override
    public String name() {
        return "info";
    }

    @Override
    public String permission() {
        return "tagify.admin.info";
    }

    @Override
    public String usageArgs() {
        return "<player>";
    }

    @Override
    public String descriptionKey() {
        return "command.info.description";
    }

    @Override
    public void run(CommandContext ctx) {
        if (ctx.argCount() < 1) {
            ctx.msg("command.info.usage");
            return;
        }
        String targetName = ctx.arg(0);
        Optional<UUID> target = Players.resolve(targetName);
        if (target.isEmpty()) {
            ctx.msg("command.player-unknown", Placeholder.unparsed("player", targetName));
            return;
        }
        UUID uuid = target.get();

        ctx.core().tags().resolve(uuid).whenComplete((tags, error) ->
                ctx.core().platform().scheduler().global(() -> {
                    if (error != null || tags == null) {
                        ctx.msg("command.error");
                        return;
                    }
                    render(ctx, uuid, tags);
                }));
    }

    private void render(CommandContext ctx, UUID uuid, PlayerTags tags) {
        String groups = tags.groups().isEmpty() ? "-"
                : tags.groups().stream()
                .map(g -> g.name() + " (" + g.priority() + ")")
                .collect(Collectors.joining(", "));

        ctx.msg("command.info.header", Placeholder.unparsed("player", Players.nameOf(uuid)));
        ctx.msg("command.info.prefix",
                Placeholder.parsed("value", tags.prefix().orElse("-")),
                Placeholder.unparsed("source", tags.prefixSource().name().toLowerCase(java.util.Locale.ROOT)));
        ctx.msg("command.info.suffix",
                Placeholder.parsed("value", tags.suffix().orElse("-")),
                Placeholder.unparsed("source", tags.suffixSource().name().toLowerCase(java.util.Locale.ROOT)));
        ctx.msg("command.info.groups", Placeholder.unparsed("groups", groups));
    }

    @Override
    public List<String> tabComplete(CommandContext ctx) {
        return ctx.argCount() == 1 ? CommandTab.onlinePlayers() : List.of();
    }
}
