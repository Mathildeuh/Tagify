package fr.mathildeuh.tagify.internal.gui;

import fr.mathildeuh.tagify.api.model.Tag;
import fr.mathildeuh.tagify.api.model.TagGroup;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;

/** Édition d'un groupe : préfixe / suffixe (saisie chat), priorité, suppression. */
final class GroupEditorMenu extends AbstractMenu {

    private final String groupName;

    GroupEditorMenu(GuiManager gui, String groupName) {
        super(gui);
        this.groupName = groupName;
    }

    @Override
    protected Component title() {
        return gui.lang().render("gui.group-editor.title", Placeholder.unparsed("group", groupName));
    }

    @Override
    protected int rows() {
        return 3;
    }

    @Override
    protected void build(Player viewer) {
        fillBorder(item(Material.GRAY_STAINED_GLASS_PANE, Component.empty(), List.of()));

        Optional<TagGroup> maybe = gui.groups().getGroup(groupName);
        if (maybe.isEmpty()) {
            gui.lang().send(viewer, "group.not-found", Placeholder.unparsed("group", groupName));
            return;
        }
        TagGroup group = maybe.get();

        set(10, item(Material.NAME_TAG, gui.lang().render("gui.group-editor.prefix.name"),
                        gui.lang().renderList("gui.group-editor.prefix.lore",
                                Placeholder.parsed("value", nullSafe(group.tag().prefix())))),
                e -> gui.prompt(viewer, "gui.group-editor.prefix.prompt",
                        input -> apply(viewer, g -> g.tag().withPrefix(unset(input)))));

        set(13, item(Material.NAME_TAG, gui.lang().render("gui.group-editor.suffix.name"),
                        gui.lang().renderList("gui.group-editor.suffix.lore",
                                Placeholder.parsed("value", nullSafe(group.tag().suffix())))),
                e -> gui.prompt(viewer, "gui.group-editor.suffix.prompt",
                        input -> apply(viewer, g -> g.tag().withSuffix(unset(input)))));

        set(15, item(Material.COMPARATOR,
                        gui.lang().render("gui.group-editor.priority.name",
                                Placeholder.unparsed("priority", String.valueOf(group.priority()))),
                        gui.lang().renderList("gui.group-editor.priority.lore",
                                Placeholder.unparsed("priority", String.valueOf(group.priority())))),
                e -> {
                    int delta = e.getClick().isRightClick() ? -1 : 1;
                    if (e.getClick().isShiftClick()) {
                        delta *= 10;
                    }
                    int updated = group.priority() + delta;
                    gui.groups().setGroupPriority(groupName, updated)
                            .thenRun(() -> gui.platform().scheduler().global(() ->
                                    new GroupEditorMenu(gui, groupName).open(viewer)));
                });

        set(22, item(Material.BARRIER, gui.lang().render("gui.group-editor.delete.name"),
                        gui.lang().renderList("gui.group-editor.delete.lore")),
                e -> {
                    if (e.getClick().isShiftClick()) {
                        gui.groups().deleteGroup(groupName);
                        gui.lang().send(viewer, "group.deleted", Placeholder.unparsed("group", groupName));
                        gui.platform().scheduler().global(() -> new GroupListMenu(gui, 0).open(viewer));
                    } else {
                        gui.lang().send(viewer, "gui.group-editor.delete.confirm");
                    }
                });

        set(18, item(Material.ARROW, gui.lang().render("gui.back"), List.of()),
                e -> new GroupListMenu(gui, 0).open(viewer));
    }

    private void apply(Player viewer, java.util.function.Function<TagGroup, Tag> mutator) {
        gui.groups().getGroup(groupName).ifPresent(group ->
                gui.groups().setGroupTag(groupName, mutator.apply(group))
                        .thenRun(() -> gui.platform().scheduler().global(() ->
                                new GroupEditorMenu(gui, groupName).open(viewer))));
    }

    private static String nullSafe(String value) {
        return value == null ? "-" : value;
    }

    private static String unset(String input) {
        return input == null || input.isBlank() || input.equals("-") || input.equalsIgnoreCase("none")
                ? null : input;
    }
}
