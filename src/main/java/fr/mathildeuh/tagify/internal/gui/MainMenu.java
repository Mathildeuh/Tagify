package fr.mathildeuh.tagify.internal.gui;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

/** Menu racine : navigation vers les groupes et actions rapides. */
final class MainMenu extends AbstractMenu {

    MainMenu(GuiManager gui) {
        super(gui);
    }

    @Override
    protected Component title() {
        return gui.lang().render("gui.main.title");
    }

    @Override
    protected int rows() {
        return 3;
    }

    @Override
    protected void build(Player viewer) {
        fillBorder(item(Material.GRAY_STAINED_GLASS_PANE, Component.empty(), List.of()));

        set(11, item(Material.CHEST,
                        gui.lang().render("gui.main.groups.name"),
                        gui.lang().renderList("gui.main.groups.lore")),
                event -> new GroupListMenu(gui, 0).open((Player) event.getWhoClicked()));

        set(13, item(Material.NAME_TAG,
                        gui.lang().render("gui.main.info.name"),
                        gui.lang().renderList("gui.main.info.lore")),
                null);

        set(15, item(Material.SUNFLOWER,
                        gui.lang().render("gui.main.reload.name"),
                        gui.lang().renderList("gui.main.reload.lore")),
                event -> {
                    Player admin = (Player) event.getWhoClicked();
                    admin.closeInventory();
                    admin.performCommand("tagify reload");
                });
    }
}
