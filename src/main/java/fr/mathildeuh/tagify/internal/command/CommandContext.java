package fr.mathildeuh.tagify.internal.command;

import fr.mathildeuh.tagify.internal.TagifyCore;
import fr.mathildeuh.tagify.internal.lang.Lang;
import fr.mathildeuh.tagify.internal.util.Quotes;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

/** Execution context of a sub-command: {@code args} are the arguments <em>after</em> the sub-command's name. */
public final class CommandContext {

    private final TagifyCore core;
    private final CommandSender sender;
    private final String label;
    private final String[] args;

    public CommandContext(TagifyCore core, CommandSender sender, String label, String[] args) {
        this.core = core;
        this.sender = sender;
        this.label = label;
        this.args = args;
    }

    public TagifyCore core() {
        return core;
    }

    public CommandSender sender() {
        return sender;
    }

    public Lang lang() {
        return core.lang();
    }

    public String label() {
        return label;
    }

    public String[] args() {
        return args;
    }

    public int argCount() {
        return args.length;
    }

    public @Nullable String arg(int index) {
        return index >= 0 && index < args.length ? args[index] : null;
    }

    public boolean hasArg(int index) {
        return index >= 0 && index < args.length && !args[index].isEmpty();
    }

    /**
     * Joins every argument from {@code from} onward with a single space — the free-text tail
     * of a command (a prefix/suffix/condition value). If the result is wrapped in a matching
     * pair of single quotes, they are stripped (see {@link Quotes#unwrap}), which lets a value
     * start or end with whitespace, e.g. {@code '[VIP] '}.
     */
    public String rest(int from) {
        if (from >= args.length) {
            return "";
        }
        return Quotes.unwrap(String.join(" ", java.util.Arrays.copyOfRange(args, from, args.length)));
    }

    public boolean isPlayer() {
        return sender instanceof Player;
    }

    public @Nullable Player player() {
        return sender instanceof Player p ? p : null;
    }

    public boolean has(String permission) {
        return sender.hasPermission(permission);
    }

    public void msg(String key, TagResolver... resolvers) {
        core.lang().send(sender, key, resolvers);
    }
}
