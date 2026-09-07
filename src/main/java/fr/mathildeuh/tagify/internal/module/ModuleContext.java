package fr.mathildeuh.tagify.internal.module;

import fr.mathildeuh.tagify.api.service.GroupService;
import fr.mathildeuh.tagify.api.service.TagService;
import fr.mathildeuh.tagify.internal.config.TagifyConfig;
import fr.mathildeuh.tagify.internal.gui.GuiProvider;
import fr.mathildeuh.tagify.internal.platform.PlatformScheduler;
import fr.mathildeuh.tagify.internal.platform.nametag.TablistSort;
import fr.mathildeuh.tagify.internal.storage.StorageRegistry;
import fr.mathildeuh.tagify.internal.tag.GroupConditions;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.concurrent.Executor;
import java.util.logging.Logger;

/**
 * The surface the Core exposes to the Premium module. Lets the module graft its features in
 * without the Core knowing its classes.
 */
public interface ModuleContext {

    Plugin plugin();

    Logger logger();

    /** The {@code plugins/Tagify/premium/} folder (created on demand). */
    File moduleDataFolder();

    TagifyConfig config();

    PlatformScheduler scheduler();

    Executor asyncExecutor();

    StorageRegistry storageRegistry();

    TagService tagService();

    GroupService groupService();

    void setGuiProvider(GuiProvider provider);

    void setSyncService(SyncService syncService);

    /** Replaces the tab-list ordering strategy (Premium weight / automatic modes). */
    void setTablistSort(TablistSort tablistSort);

    /** Replaces the per-player group evaluation (Premium conditions: world / permission / boosts). */
    void setGroupConditions(GroupConditions conditions);

    /** The Core's default GUI provider (Premium wraps / delegates to it). */
    GuiProvider coreGuiProvider();

    /** Reloads the group cache from storage, then refreshes online players. */
    void reloadGroups();

    void refreshAll();

    /**
     * The Premium module reports a valid licence: the Core (re-)evaluates the configured
     * storage backend, marks the Premium edition active and logs.
     */
    void onPremiumEnabled();

    /**
     * The Premium module reports it cannot activate (missing / invalid / expired licence).
     * The Core stays in the Free edition and logs {@code reason} without a stack trace.
     */
    void onPremiumDisabled(String reason);
}
