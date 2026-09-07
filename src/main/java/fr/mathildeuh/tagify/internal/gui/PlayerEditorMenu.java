package fr.mathildeuh.tagify.internal.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

/** Édition du tag individuel d'un joueur. */
final class PlayerEditorMenu extends AbstractMenu {

    private final UUID target;
    private final String targetName;

    PlayerEditorMenu(GuiManager gui, UUID target, String targetName) {
        super(gui);
        this.target = target;
        this.targetName = targetName;
    }

    @Override
    protected Component title() {
        return gui.lang().render("gui.player-editor.title", Placeholder.unparsed("player", targetName));
    }

    @Override
    protected int rows() {
        return 3;
    }

    @Override
    protected void build(Player viewer) {
        fillBorder(item(Material.GRAY_STAINED_GLASS_PANE, Component.empty(), List.of()));

        set(11, item(Material.NAME_TAG, gui.lang().render("gui.player-editor.prefix.name"),
                        gui.lang().renderList("gui.player-editor.prefix.lore")),
                e -> gui.prompt(viewer, "gui.player-editor.prefix.prompt",
                        input -> gui.tags().setIndividualPrefix(target, unset(input))
                                .thenRun(() -> reopen(viewer))));

        set(13, item(Material.NAME_TAG, gui.lang().render("gui.player-editor.suffix.name"),
                        gui.lang().renderList("gui.player-editor.suffix.lore")),
                e -> gui.prompt(viewer, "gui.player-editor.suffix.prompt",
                        input -> gui.tags().setIndividualSuffix(target, unset(input))
                                .thenRun(() -> reopen(viewer))));

        set(15, item(Material.LAVA_BUCKET, gui.lang().render("gui.player-editor.clear.name"),
                        gui.lang().renderList("gui.player-editor.clear.lore")),
                e -> gui.tags().clearIndividual(target).thenRun(() -> {
                    gui.lang().send(viewer, "tag.cleared", Placeholder.unparsed("player", targetName));
                    reopen(viewer);
                }));
    }

    private void reopen(Player viewer) {
        gui.platform().scheduler().global(() -> new PlayerEditorMenu(gui, target, targetName).open(viewer));
    }

    private static String unset(String input) {
        return input == null || input.isBlank() || input.equals("-") || input.equalsIgnoreCase("none")
                ? null : input;
    }
}
