package fr.mathildeuh.tagify.internal.command.sub;

import fr.mathildeuh.tagify.internal.command.CommandContext;
import fr.mathildeuh.tagify.internal.command.SubCommand;
import fr.mathildeuh.tagify.internal.command.TagifyCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

/**
 * {@code /tagify help} (and bare {@code /tagify}) — lists the commands available to the sender.
 *
 * <p>Each line shows only the command name to avoid the chat-wrapping wall of text a full
 * "usage — description" listing produces; the description, full usage and permission are on
 * hover, and clicking a line inserts the command into the chat bar.
 */
public final class HelpCommand implements SubCommand {

    private final TagifyCommand dispatcher;

    public HelpCommand(TagifyCommand dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    public String name() {
        return "help";
    }

    @Override
    public String permission() {
        return "tagify.use";
    }

    @Override
    public String usageArgs() {
        return "";
    }

    @Override
    public String descriptionKey() {
        return "command.help.description";
    }

    @Override
    public void run(CommandContext ctx) {
        ctx.msg("command.help.header");

        int shown = 0;
        for (SubCommand sub : dispatcher.uniqueSubCommands()) {
            if (!ctx.has(sub.permission())) {
                continue;
            }
            String command = "/tagify " + sub.name();
            String usage = command + (sub.usageArgs().isEmpty() ? "" : " " + sub.usageArgs());

            Component hover = ctx.lang().render("command.help.hover",
                    Placeholder.parsed("description", ctx.lang().raw(sub.descriptionKey())),
                    Placeholder.unparsed("usage", usage),
                    Placeholder.unparsed("permission", sub.permission()));

            Component line = ctx.lang().render("command.help.entry", Placeholder.unparsed("command", command))
                    .hoverEvent(HoverEvent.showText(hover))
                    .clickEvent(ClickEvent.suggestCommand(command + " "));

            ctx.sender().sendMessage(line);
            shown++;
        }

        ctx.msg("command.help.footer", Placeholder.unparsed("count", String.valueOf(shown)));
    }
}
