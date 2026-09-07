package fr.mathildeuh.tagify.internal.command;

import fr.mathildeuh.tagify.api.model.TagGroup;
import fr.mathildeuh.tagify.internal.TagifyCore;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/** Aides de complétion partagées entre sous-commandes. */
public final class CommandTab {

    public static final List<String> PREFIX_SUFFIX = List.of("prefix", "suffix");
    public static final List<String> STORAGE_TYPES = List.of("flatfile", "mysql", "postgresql", "mongodb");

    private CommandTab() {
    }

    public static List<String> onlinePlayers() {
        List<String> names = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            names.add(player.getName());
        }
        return names;
    }

    public static List<String> groupNames(TagifyCore core) {
        List<String> names = new ArrayList<>();
        for (TagGroup group : core.groups().getGroups()) {
            names.add(group.name());
        }
        return names;
    }
}
