package fr.mathildeuh.tagify.internal.integration.vault;

import fr.mathildeuh.tagify.internal.integration.permission.GroupProvider;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Intégration Vault — repli de source de groupes lorsque LuckPerms est absent.
 * Utilisé uniquement si {@code integrations.vault} est activé.
 */
public final class VaultHook implements GroupProvider {

    private final Permission permission;

    private VaultHook(Permission permission) {
        this.permission = permission;
    }

    public static VaultHook tryHook(Plugin plugin) {
        try {
            RegisteredServiceProvider<Permission> rsp =
                    Bukkit.getServicesManager().getRegistration(Permission.class);
            if (rsp == null) {
                return null;
            }
            return new VaultHook(rsp.getProvider());
        } catch (Throwable t) {
            plugin.getLogger().log(Level.FINE, "Vault unavailable", t);
            return null;
        }
    }

    @Override
    public Set<String> groupsOf(UUID playerId) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerId);
        try {
            String[] groups = permission.getPlayerGroups(null, player);
            return groups == null ? Set.of() : Set.of(groups);
        } catch (Throwable t) {
            return Set.of();
        }
    }
}
