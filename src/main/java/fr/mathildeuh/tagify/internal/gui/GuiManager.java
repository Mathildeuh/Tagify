package fr.mathildeuh.tagify.internal.gui;

import fr.mathildeuh.tagify.api.service.GroupService;
import fr.mathildeuh.tagify.api.service.TagService;
import fr.mathildeuh.tagify.internal.config.TagifyConfig;
import fr.mathildeuh.tagify.internal.lang.Lang;
import fr.mathildeuh.tagify.internal.platform.Platform;
import fr.mathildeuh.tagify.internal.text.TextPipeline;
import fr.mathildeuh.tagify.internal.util.Quotes;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Default GUI provider (Free edition): chest menus + chat input. Also tracks open menus and
 * the related events. The Premium module builds its own menus on top of these accessors.
 */
public final class GuiManager implements GuiProvider, Listener {

    private final Plugin plugin;
    private final Lang lang;
    private final GroupService groups;
    private final TagService tags;
    private final Platform platform;
    private final TextPipeline text;
    private final Supplier<TagifyConfig> config;

    private final Map<UUID, AbstractMenu> openMenus = new ConcurrentHashMap<>();
    private final Map<UUID, Consumer<String>> chatPrompts = new ConcurrentHashMap<>();

    public GuiManager(Plugin plugin, Lang lang, GroupService groups, TagService tags,
                      Platform platform, TextPipeline text, Supplier<TagifyConfig> config) {
        this.plugin = plugin;
        this.lang = lang;
        this.groups = groups;
        this.tags = tags;
        this.platform = platform;
        this.text = text;
        this.config = config;
    }

    public Lang lang() {
        return lang;
    }

    public GroupService groups() {
        return groups;
    }

    public TagService tags() {
        return tags;
    }

    public TextPipeline text() {
        return text;
    }

    public Platform platform() {
        return platform;
    }

    public void track(Player viewer, AbstractMenu menu) {
        openMenus.put(viewer.getUniqueId(), menu);
    }

    /** Asks the player for chat input; closes the menu for the duration of the prompt. */
    public void prompt(Player viewer, String promptKey, Consumer<String> onInput) {
        viewer.closeInventory();
        lang.send(viewer, promptKey);
        lang.send(viewer, "gui.prompt-cancel");
        chatPrompts.put(viewer.getUniqueId(), onInput);
    }

    // ----------------------------------------------------------- GuiProvider

    @Override
    public void openMain(Player admin) {
        new MainMenu(this).open(admin);
    }

    @Override
    public void openGroupList(Player admin) {
        new GroupListMenu(this, 0).open(admin);
    }

    @Override
    public void openGroupEditor(Player admin, String groupName) {
        groups.getGroup(groupName).ifPresentOrElse(
                group -> new GroupEditorMenu(this, group.name()).open(admin),
                () -> lang.send(admin, "group.not-found",
                        net.kyori.adventure.text.minimessage.tag.resolver.Placeholder.unparsed("group", groupName)));
    }

    @Override
    public void openPlayerEditor(Player admin, UUID target, String targetName) {
        new PlayerEditorMenu(this, target, targetName).open(admin);
    }

    // ----------------------------------------------------------- events

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        AbstractMenu menu = openMenus.get(player.getUniqueId());
        if (menu != null && event.getView().getTopInventory().equals(menu.inventory())) {
            menu.handleClick(event);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        openMenus.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    @SuppressWarnings("deprecation") // universal Spigot/Paper event, used for GUI chat input
    public void onChat(AsyncPlayerChatEvent event) {
        Consumer<String> prompt = chatPrompts.remove(event.getPlayer().getUniqueId());
        if (prompt == null) {
            return;
        }
        event.setCancelled(true);
        // Checked against the raw input, before unwrapping: wrapping the literal word "cancel"
        // in quotes ('cancel') is how an admin sets that exact value instead of aborting.
        String input = event.getMessage().trim();
        platform.scheduler().global(() -> {
            if (!input.equalsIgnoreCase("cancel") && !input.equalsIgnoreCase("annuler")) {
                // Same single-quote unwrapping as slash-command values (CommandContext#rest):
                // 'Prefix ' keeps its wrapping/trailing whitespace, matching the chat-prompt
                // path to the command-line one.
                prompt.accept(Quotes.unwrap(input));
            } else {
                lang.send(event.getPlayer(), "gui.cancelled");
            }
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        openMenus.remove(event.getPlayer().getUniqueId());
        chatPrompts.remove(event.getPlayer().getUniqueId());
    }

    public void shutdown() {
        openMenus.clear();
        chatPrompts.clear();
    }
}
