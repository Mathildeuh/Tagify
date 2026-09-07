package fr.mathildeuh.tagify.internal.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/** Base d'un menu coffre : contenu + associations slot → action. */
public abstract class AbstractMenu {

    protected final GuiManager gui;
    private final Map<Integer, Consumer<InventoryClickEvent>> actions = new HashMap<>();
    private Inventory inventory;

    protected AbstractMenu(GuiManager gui) {
        this.gui = gui;
    }

    protected abstract Component title();

    protected abstract int rows();

    protected abstract void build(Player viewer);

    public final void open(Player viewer) {
        this.inventory = org.bukkit.Bukkit.createInventory(null, rows() * 9, title());
        actions.clear();
        build(viewer);
        viewer.openInventory(inventory);
        gui.track(viewer, this);
    }

    public final void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        Consumer<InventoryClickEvent> action = actions.get(event.getRawSlot());
        if (action != null) {
            action.accept(event);
        }
    }

    protected void set(int slot, ItemStack item, Consumer<InventoryClickEvent> action) {
        inventory.setItem(slot, item);
        if (action != null) {
            actions.put(slot, action);
        }
    }

    protected void fillBorder(ItemStack filler) {
        int size = inventory.getSize();
        for (int i = 0; i < size; i++) {
            int row = i / 9;
            int col = i % 9;
            if (row == 0 || row == rows() - 1 || col == 0 || col == 8) {
                if (inventory.getItem(i) == null) {
                    inventory.setItem(i, filler);
                }
            }
        }
    }

    protected static ItemStack item(Material material, Component name, List<Component> lore) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.displayName(name.decoration(TextDecoration.ITALIC, false));
            if (lore != null && !lore.isEmpty()) {
                List<Component> decorated = new ArrayList<>(lore.size());
                for (Component line : lore) {
                    decorated.add(line.decoration(TextDecoration.ITALIC, false));
                }
                meta.lore(decorated);
            }
            stack.setItemMeta(meta);
        }
        return stack;
    }

    protected Inventory inventory() {
        return inventory;
    }
}
