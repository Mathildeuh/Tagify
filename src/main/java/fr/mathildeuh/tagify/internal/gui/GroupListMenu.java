package fr.mathildeuh.tagify.internal.gui;

import fr.mathildeuh.tagify.api.model.TagGroup;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/** Liste paginée des groupes ; clic = éditer, clic droit = supprimer, bouton « + » = créer. */
final class GroupListMenu extends AbstractMenu {

    private static final int PAGE_SIZE = 28;
    private final int page;

    GroupListMenu(GuiManager gui, int page) {
        super(gui);
        this.page = Math.max(0, page);
    }

    @Override
    protected Component title() {
        return gui.lang().render("gui.groups.title", Placeholder.unparsed("page", String.valueOf(page + 1)));
    }

    @Override
    protected int rows() {
        return 6;
    }

    @Override
    protected void build(Player viewer) {
        fillBorder(item(Material.GRAY_STAINED_GLASS_PANE, Component.empty(), List.of()));

        List<TagGroup> all = gui.groups().getGroups();
        int from = page * PAGE_SIZE;
        List<TagGroup> slice = from >= all.size() ? List.of()
                : all.subList(from, Math.min(all.size(), from + PAGE_SIZE));

        int[] slots = contentSlots();
        for (int i = 0; i < slice.size() && i < slots.length; i++) {
            TagGroup group = slice.get(i);
            set(slots[i], groupItem(group), event -> {
                if (event.getClick().isRightClick()) {
                    gui.groups().deleteGroup(group.name());
                    gui.lang().send((Player) event.getWhoClicked(), "group.deleted",
                            Placeholder.unparsed("group", group.name()));
                    new GroupListMenu(gui, page).open((Player) event.getWhoClicked());
                } else {
                    new GroupEditorMenu(gui, group.name()).open((Player) event.getWhoClicked());
                }
            });
        }

        if (page > 0) {
            set(45, item(Material.ARROW, gui.lang().render("gui.previous-page"), List.of()),
                    e -> new GroupListMenu(gui, page - 1).open((Player) e.getWhoClicked()));
        }
        if (from + PAGE_SIZE < all.size()) {
            set(53, item(Material.ARROW, gui.lang().render("gui.next-page"), List.of()),
                    e -> new GroupListMenu(gui, page + 1).open((Player) e.getWhoClicked()));
        }

        set(49, item(Material.LIME_DYE,
                        gui.lang().render("gui.groups.create.name"),
                        gui.lang().renderList("gui.groups.create.lore")),
                e -> gui.prompt((Player) e.getWhoClicked(), "gui.groups.create.prompt", name -> {
                    Player admin = (Player) e.getWhoClicked();
                    gui.groups().createGroup(name).whenComplete((group, error) -> gui.platform().scheduler().global(() -> {
                        if (error != null) {
                            gui.lang().send(admin, "group.create-failed", Placeholder.unparsed("group", name));
                        } else {
                            gui.lang().send(admin, "group.created", Placeholder.unparsed("group", name));
                            new GroupEditorMenu(gui, group.name()).open(admin);
                        }
                    }));
                }));

        set(48, item(Material.BARRIER, gui.lang().render("gui.back"), List.of()),
                e -> new MainMenu(gui).open((Player) e.getWhoClicked()));
    }

    private org.bukkit.inventory.ItemStack groupItem(TagGroup group) {
        List<Component> lore = new ArrayList<>();
        lore.add(gui.lang().render("gui.groups.entry.priority",
                Placeholder.unparsed("priority", String.valueOf(group.priority()))));
        lore.add(gui.lang().render("gui.groups.entry.prefix",
                Placeholder.parsed("value", group.tag().prefix() == null ? "-" : group.tag().prefix())));
        lore.add(gui.lang().render("gui.groups.entry.suffix",
                Placeholder.parsed("value", group.tag().suffix() == null ? "-" : group.tag().suffix())));
        lore.add(gui.lang().render("gui.groups.entry.members",
                Placeholder.unparsed("count", String.valueOf(group.members().size()))));
        lore.add(Component.empty());
        lore.add(gui.lang().render("gui.groups.entry.hint"));
        return item(Material.NAME_TAG, gui.lang().parse("<white>" + group.name()), lore);
    }

    private static int[] contentSlots() {
        int[] slots = new int[28];
        int index = 0;
        for (int row = 1; row <= 4; row++) {
            for (int col = 1; col <= 7; col++) {
                slots[index++] = row * 9 + col;
            }
        }
        return slots;
    }
}
