package fr.mathildeuh.tagify.internal.command;

import fr.mathildeuh.tagify.internal.TagifyCore;
import fr.mathildeuh.tagify.internal.command.sub.ConvertCommand;
import fr.mathildeuh.tagify.internal.command.sub.GroupCommand;
import fr.mathildeuh.tagify.internal.command.sub.GuiCommand;
import fr.mathildeuh.tagify.internal.command.sub.HelpCommand;
import fr.mathildeuh.tagify.internal.command.sub.ImportCommand;
import fr.mathildeuh.tagify.internal.command.sub.InfoCommand;
import fr.mathildeuh.tagify.internal.command.sub.RefreshCommand;
import fr.mathildeuh.tagify.internal.command.sub.ReloadCommand;
import fr.mathildeuh.tagify.internal.command.sub.RemoveCommand;
import fr.mathildeuh.tagify.internal.command.sub.SetCommand;
import fr.mathildeuh.tagify.internal.command.sub.VersionCommand;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Dispatcher de {@code /tagify} (alias {@code /tgy}, {@code /ty}, {@code /tfy}). */
public final class TagifyCommand implements CommandExecutor, TabCompleter {

    private final TagifyCore core;
    private final Map<String, SubCommand> subCommands = new LinkedHashMap<>();
    private final HelpCommand help;

    public TagifyCommand(TagifyCore core) {
        this.core = core;
        this.help = new HelpCommand(this);
        register(help);
        register(new SetCommand());
        register(new RemoveCommand());
        register(new InfoCommand());
        register(new GroupCommand());
        register(new GuiCommand());
        register(new ReloadCommand());
        register(new ConvertCommand());
        register(new ImportCommand());
        register(new RefreshCommand());
        register(new VersionCommand());
    }

    private void register(SubCommand sub) {
        subCommands.put(sub.name().toLowerCase(Locale.ROOT), sub);
        for (String alias : sub.aliases()) {
            subCommands.put(alias.toLowerCase(Locale.ROOT), sub);
        }
    }

    public void register() {
        PluginCommand command = core.plugin().getCommand("tagify");
        if (command == null) {
            core.plugin().getLogger().severe("Command 'tagify' missing from plugin.yml - commands disabled.");
            return;
        }
        command.setExecutor(this);
        command.setTabCompleter(this);
    }

    public TagifyCore core() {
        return core;
    }

    public java.util.Collection<SubCommand> uniqueSubCommands() {
        List<SubCommand> list = new ArrayList<>();
        for (Map.Entry<String, SubCommand> entry : subCommands.entrySet()) {
            if (entry.getKey().equals(entry.getValue().name().toLowerCase(Locale.ROOT))) {
                list.add(entry.getValue());
            }
        }
        return list;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            help.run(new CommandContext(core, sender, label, new String[0]));
            return true;
        }

        SubCommand sub = subCommands.get(args[0].toLowerCase(Locale.ROOT));
        if (sub == null) {
            core.lang().send(sender, "command.unknown", Placeholder.unparsed("input", args[0]));
            return true;
        }
        if (!sender.hasPermission(sub.permission())) {
            core.lang().send(sender, "command.no-permission");
            return true;
        }
        if (sub.playerOnly() && !(sender instanceof org.bukkit.entity.Player)) {
            core.lang().send(sender, "command.player-only");
            return true;
        }

        String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
        try {
            sub.run(new CommandContext(core, sender, label, subArgs));
        } catch (Exception e) {
            core.lang().send(sender, "command.error");
            core.plugin().getLogger().log(java.util.logging.Level.WARNING,
                    "Error during /tagify " + sub.name(), e);
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> names = new ArrayList<>();
            for (SubCommand sub : uniqueSubCommands()) {
                if (sender.hasPermission(sub.permission())
                        && sub.name().startsWith(args[0].toLowerCase(Locale.ROOT))) {
                    names.add(sub.name());
                }
            }
            return names;
        }

        SubCommand sub = subCommands.get(args[0].toLowerCase(Locale.ROOT));
        if (sub == null || !sender.hasPermission(sub.permission())) {
            return List.of();
        }
        String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
        List<String> raw = sub.tabComplete(new CommandContext(core, sender, alias, subArgs));
        String current = subArgs.length == 0 ? "" : subArgs[subArgs.length - 1].toLowerCase(Locale.ROOT);
        List<String> filtered = new ArrayList<>();
        for (String option : raw) {
            if (option.toLowerCase(Locale.ROOT).startsWith(current)) {
                filtered.add(option);
            }
        }
        return filtered;
    }
}
