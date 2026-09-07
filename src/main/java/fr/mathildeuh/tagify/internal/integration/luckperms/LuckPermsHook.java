package fr.mathildeuh.tagify.internal.integration.luckperms;

import fr.mathildeuh.tagify.internal.integration.permission.GroupProvider;
import fr.mathildeuh.tagify.internal.tag.RefreshService;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.event.EventSubscription;
import net.luckperms.api.event.user.UserDataRecalculateEvent;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.NodeType;
import org.bukkit.plugin.Plugin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.logging.Level;

/**
 * Intégration LuckPerms :
 * <ul>
 *   <li>rafraîchit un joueur dès que ses données de permissions sont recalculées
 *       ({@link UserDataRecalculateEvent}) — sans polling ;</li>
 *   <li>expose ses groupes pour le mappage « groupe LuckPerms → groupe de tags ».</li>
 * </ul>
 */
public final class LuckPermsHook implements GroupProvider, AutoCloseable {

    private final Plugin plugin;
    private final LuckPerms luckPerms;
    private final Supplier<Boolean> refreshOnPermissionChange;
    private EventSubscription<UserDataRecalculateEvent> subscription;

    private LuckPermsHook(Plugin plugin, LuckPerms luckPerms, Supplier<Boolean> refreshOnPermissionChange) {
        this.plugin = plugin;
        this.luckPerms = luckPerms;
        this.refreshOnPermissionChange = refreshOnPermissionChange;
    }

    /** @return le hook, ou {@code null} si LuckPerms est absent / son API indisponible. */
    public static LuckPermsHook tryHook(Plugin plugin, RefreshService refresh,
                                        Supplier<Boolean> refreshOnPermissionChange) {
        try {
            LuckPerms api = LuckPermsProvider.get();
            LuckPermsHook hook = new LuckPermsHook(plugin, api, refreshOnPermissionChange);
            hook.subscription = api.getEventBus().subscribe(plugin, UserDataRecalculateEvent.class, event -> {
                if (Boolean.TRUE.equals(refreshOnPermissionChange.get())) {
                    refresh.request(event.getUser().getUniqueId());
                }
            });
            return hook;
        } catch (Throwable t) {
            plugin.getLogger().log(Level.FINE, "LuckPerms unavailable", t);
            return null;
        }
    }

    @Override
    public Set<String> groupsOf(UUID playerId) {
        User user = luckPerms.getUserManager().getUser(playerId);
        if (user == null) {
            return Set.of();
        }
        Set<String> groups = new HashSet<>();
        groups.add(user.getPrimaryGroup());
        user.getNodes(NodeType.INHERITANCE).forEach(node -> groups.add(node.getGroupName()));
        return groups;
    }

    @Override
    public void close() {
        if (subscription != null) {
            subscription.close();
            subscription = null;
        }
    }
}
