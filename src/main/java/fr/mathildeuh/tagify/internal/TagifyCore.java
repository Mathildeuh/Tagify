package fr.mathildeuh.tagify.internal;

import fr.mathildeuh.tagify.TagifyPlugin;
import fr.mathildeuh.tagify.api.TagifyApi;
import fr.mathildeuh.tagify.api.service.GroupService;
import fr.mathildeuh.tagify.api.service.TagService;
import fr.mathildeuh.tagify.internal.command.TagifyCommand;
import fr.mathildeuh.tagify.internal.config.ConfigManager;
import fr.mathildeuh.tagify.internal.config.TagifyConfig;
import fr.mathildeuh.tagify.internal.gui.GuiManager;
import fr.mathildeuh.tagify.internal.gui.GuiProvider;
import fr.mathildeuh.tagify.internal.importer.NametagEditImporter;
import fr.mathildeuh.tagify.internal.integration.Integrations;
import fr.mathildeuh.tagify.internal.integration.permission.GroupProvider;
import fr.mathildeuh.tagify.internal.lang.Lang;
import fr.mathildeuh.tagify.internal.listener.PlayerConnectionListener;
import fr.mathildeuh.tagify.internal.metrics.TagifyMetrics;
import fr.mathildeuh.tagify.internal.module.ModuleContext;
import fr.mathildeuh.tagify.internal.module.ModuleManager;
import fr.mathildeuh.tagify.internal.module.SyncService;
import fr.mathildeuh.tagify.internal.platform.Platform;
import fr.mathildeuh.tagify.internal.platform.PlatformScheduler;
import fr.mathildeuh.tagify.internal.platform.nametag.NametagOptions;
import fr.mathildeuh.tagify.internal.platform.nametag.ScoreboardTeamRenderer;
import fr.mathildeuh.tagify.internal.platform.nametag.TablistSort;
import fr.mathildeuh.tagify.internal.storage.DataStore;
import fr.mathildeuh.tagify.internal.storage.StorageContext;
import fr.mathildeuh.tagify.internal.storage.StorageMigrator;
import fr.mathildeuh.tagify.internal.storage.StorageRegistry;
import fr.mathildeuh.tagify.internal.storage.StorageType;
import fr.mathildeuh.tagify.internal.storage.flatfile.FlatFileDataStore;
import fr.mathildeuh.tagify.internal.tag.GroupConditions;
import fr.mathildeuh.tagify.internal.tag.GroupManager;
import fr.mathildeuh.tagify.internal.tag.TagManager;
import fr.mathildeuh.tagify.internal.text.PlaceholderBridge;
import fr.mathildeuh.tagify.internal.text.TextPipeline;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.scoreboard.Team;

import java.io.File;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Assembles and drives every Core component. Implements {@link ModuleContext}: this is the
 * Premium module's point of contact.
 */
public final class TagifyCore implements ModuleContext {

    private final TagifyPlugin plugin;
    private final TagifyMetrics metrics = new TagifyMetrics();

    private Platform platform;
    private ConfigManager configManager;
    private Lang lang;
    private StorageRegistry storageRegistry;
    private TextPipeline text;
    private ScoreboardTeamRenderer renderer;
    private TagManager tags;
    private GroupManager groups;
    private Integrations integrations;
    private ModuleManager modules;
    private GuiManager guiManager;
    private StorageMigrator migrator;
    private NametagEditImporter importer;
    private TagifyImpl api;

    private volatile DataStore dataStore;
    private volatile String backendId = "flatfile";
    private volatile GuiProvider guiProvider;
    private volatile SyncService syncService = SyncService.NONE;
    private volatile TablistSort tablistSort = TablistSort.BY_PRIORITY;
    private volatile GroupConditions groupConditions = GroupConditions.NONE;

    public TagifyCore(TagifyPlugin plugin) {
        this.plugin = plugin;
    }

    // ================================================================ lifecycle

    public void enable() {
        this.platform = Platform.create(plugin);
        plugin.getLogger().info("Platform: " + platform.describe());

        this.configManager = new ConfigManager(plugin);
        configManager.load();

        this.lang = new Lang(plugin);
        lang.load(configManager.get().language());

        this.storageRegistry = new StorageRegistry();
        this.integrations = new Integrations(plugin, configManager::get);

        this.text = new TextPipeline(configManager::get,
                () -> integrations == null ? PlaceholderBridge.NONE : integrations.placeholderBridge());

        this.renderer = new ScoreboardTeamRenderer(platform, this::nametagOptions, () -> tablistSort);

        this.tags = new TagManager(platform, this::dataStore, text, renderer,
                configManager::get, () -> syncService);
        this.groups = new GroupManager(platform, this::dataStore,
                () -> integrations == null ? GroupProvider.NONE : integrations.groupProvider(),
                configManager::get, () -> syncService, () -> groupConditions, tags.refreshService());
        tags.setGroupManager(groups);

        integrations.wire(tags.refreshService(), tags);

        this.migrator = new StorageMigrator();
        this.importer = new NametagEditImporter();

        this.guiManager = new GuiManager(plugin, lang, groups, tags, platform, text, configManager::get);
        this.guiProvider = guiManager;
        Bukkit.getPluginManager().registerEvents(guiManager, plugin);

        this.api = new TagifyImpl(tags, groups, () -> backendId,
                () -> modules != null && modules.isPremiumActive());
        Bukkit.getServicesManager().register(TagifyApi.class, api, plugin, ServicePriority.Normal);

        // Premium module: registers its factories, then runs its licence check asynchronously.
        this.modules = new ModuleManager(plugin.getLogger());
        modules.load(this);

        // Storage: starts on the available backend (flatfile if Premium is not yet validated).
        initStorage();
        warnAboutPremiumOnlySort();

        groups.loadAll().whenComplete((v, err) -> {
            if (err != null) {
                plugin.getLogger().log(Level.WARNING, "Could not load groups", err);
            }
        });

        new TagifyCommand(this).register();
        Bukkit.getPluginManager().registerEvents(new PlayerConnectionListener(this), plugin);

        tags.refreshService().configureInterval(
                configManager.get().refresh().intervalSeconds(), tags::refreshAll);

        if (configManager.get().advanced().metrics()) {
            metrics.start(this);
        }

        // Apply to players that are already connected (server /reload or hot reload).
        for (Player online : Bukkit.getOnlinePlayers()) {
            tags.onJoin(online);
        }

        plugin.getLogger().info("Tagify " + BuildConstants.fullVersion() + " enabled.");
    }

    public void disable() {
        metrics.stop();
        if (modules != null) {
            modules.disable();
        }
        if (integrations != null) {
            integrations.shutdown();
        }
        if (tags != null) {
            tags.shutdown();
        }
        if (guiManager != null) {
            guiManager.shutdown();
        }
        try {
            syncService.close();
        } catch (Throwable ignored) {
            // ignore
        }
        if (dataStore != null) {
            try {
                dataStore.close().join();
            } catch (Throwable t) {
                plugin.getLogger().log(Level.WARNING, "Closing storage", t);
            }
        }
        try {
            Bukkit.getServicesManager().unregister(TagifyApi.class, api);
        } catch (Throwable ignored) {
            // ignore
        }
    }

    public void reload() {
        configManager.reload();
        lang.load(configManager.get().language());
        groups.loadAll().thenRun(() -> tags.reloadAndReapply());
        tags.refreshService().configureInterval(
                configManager.get().refresh().intervalSeconds(), tags::refreshAll);
        warnAboutPremiumOnlySort();

        StorageType desired = StorageType.fromId(configManager.get().storage().type())
                .orElse(StorageType.FLATFILE);
        if (!desired.id().equals(backendId)) {
            plugin.getLogger().info("Configured backend (" + desired.id() + ") differs from the active one ("
                    + backendId + "). Run /tagify convert " + backendId + " " + desired.id()
                    + " then restart to apply.");
        }
    }

    // ================================================================ storage

    private void initStorage() {
        TagifyConfig cfg = configManager.get();
        StorageType desired = StorageType.fromId(cfg.storage().type()).orElse(StorageType.FLATFILE);

        boolean premiumReady = modules != null && modules.isPremiumActive()
                && storageRegistry.isRegistered(desired);
        if (desired.requiresPremium() && !premiumReady) {
            if (desired != StorageType.FLATFILE) {
                plugin.getLogger().warning("Backend '" + desired.id()
                        + "' is unavailable (Premium edition required) - falling back to flatfile.");
            }
            desired = StorageType.FLATFILE;
        }

        StorageContext ctx = new StorageContext(plugin, asyncExecutor(), cfg);
        DataStore store = storageRegistry.create(desired, ctx)
                .orElseGet(() -> new FlatFileDataStore(plugin));
        try {
            store.init().join();
        } catch (Throwable t) {
            plugin.getLogger().log(Level.WARNING,
                    "Could not initialise backend '" + store.id() + "' - falling back to flatfile", t);
            store = new FlatFileDataStore(plugin);
            store.init().join();
        }
        this.dataStore = store;
        this.backendId = store.id();
        plugin.getLogger().info("Storage: " + backendId + ".");
    }

    private void activatePremiumStorage() {
        TagifyConfig cfg = configManager.get();
        StorageType desired = StorageType.fromId(cfg.storage().type()).orElse(StorageType.FLATFILE);
        if (!desired.requiresPremium() || !storageRegistry.isRegistered(desired)
                || desired.id().equals(backendId)) {
            return;
        }
        StorageContext ctx = new StorageContext(plugin, asyncExecutor(), cfg);
        storageRegistry.create(desired, ctx).ifPresent(store -> store.init().whenComplete((v, err) -> {
            if (err != null) {
                plugin.getLogger().log(Level.WARNING, "Premium backend '" + desired.id()
                        + "': initialisation failed - keeping " + backendId, err);
                return;
            }
            DataStore previous = this.dataStore;
            this.dataStore = store;
            this.backendId = store.id();
            groups.loadAll().thenRun(() -> tags.reloadAndReapply());
            if (previous != null && previous != store) {
                previous.close();
            }
            plugin.getLogger().info("Storage switched to the Premium backend: " + backendId + ".");
        }));
    }

    private void warnAboutPremiumOnlySort() {
        boolean premiumActive = modules != null && modules.isPremiumActive();
        if (configManager.get().display().tablistSort().mode().requiresPremium() && !premiumActive) {
            plugin.getLogger().warning("Tab-list sort mode '"
                    + configManager.get().display().tablistSort().mode().name().toLowerCase()
                    + "' requires the Premium edition - falling back to priority ordering.");
        }
    }

    private NametagOptions nametagOptions() {
        TagifyConfig.Display d = configManager.get().display();
        boolean sortEnabled = d.tablistSort().mode() != TagifyConfig.SortMode.DISABLED;
        return new NametagOptions(
                sortEnabled,
                NametagOptions.parseStatus(d.nameTagVisibility(), Team.OptionStatus.ALWAYS),
                NametagOptions.parseStatus(d.collisionRule(), Team.OptionStatus.NEVER),
                d.dedicatedScoreboard(),
                d.colorFromPrefix());
    }

    private DataStore dataStore() {
        return dataStore;
    }

    // ================================================================ accessors (commands / listeners)

    public TagifyPlugin plugin() {
        return plugin;
    }

    public Platform platform() {
        return platform;
    }

    public ConfigManager configManager() {
        return configManager;
    }

    public Lang lang() {
        return lang;
    }

    public TextPipeline text() {
        return text;
    }

    public TagManager tags() {
        return tags;
    }

    public GroupManager groups() {
        return groups;
    }

    public Integrations integrations() {
        return integrations;
    }

    public ModuleManager modules() {
        return modules;
    }

    public StorageRegistry storageRegistry() {
        return storageRegistry;
    }

    public StorageMigrator migrator() {
        return migrator;
    }

    public NametagEditImporter importer() {
        return importer;
    }

    public GuiProvider guiProvider() {
        return guiProvider;
    }

    public DataStore currentDataStore() {
        return dataStore;
    }

    // ================================================================ ModuleContext

    @Override
    public Logger logger() {
        return plugin.getLogger();
    }

    @Override
    public File moduleDataFolder() {
        File dir = new File(plugin.getDataFolder(), "premium");
        if (!dir.exists() && !dir.mkdirs()) {
            plugin.getLogger().warning("Could not create " + dir);
        }
        return dir;
    }

    @Override
    public TagifyConfig config() {
        return configManager.get();
    }

    @Override
    public PlatformScheduler scheduler() {
        return platform.scheduler();
    }

    @Override
    public Executor asyncExecutor() {
        return platform.scheduler().asyncExecutor();
    }

    @Override
    public TagService tagService() {
        return tags;
    }

    @Override
    public GroupService groupService() {
        return groups;
    }

    @Override
    public void setGuiProvider(GuiProvider provider) {
        this.guiProvider = provider != null ? provider : guiManager;
        plugin.getLogger().info("Admin interface: Premium provider active.");
    }

    @Override
    public void setSyncService(SyncService service) {
        this.syncService = service != null ? service : SyncService.NONE;
    }

    @Override
    public void setTablistSort(TablistSort sort) {
        this.tablistSort = sort != null ? sort : TablistSort.BY_PRIORITY;
        if (tags != null) {
            tags.refreshAll();
        }
    }

    @Override
    public void setGroupConditions(GroupConditions conditions) {
        this.groupConditions = conditions != null ? conditions : GroupConditions.NONE;
        if (tags != null) {
            tags.refreshAll();
        }
    }

    @Override
    public GuiProvider coreGuiProvider() {
        return guiManager;
    }

    @Override
    public void reloadGroups() {
        groups.onRemoteUpdate();
    }

    @Override
    public void refreshAll() {
        tags.refreshAll();
    }

    @Override
    public void onPremiumEnabled() {
        modules.markPremiumActive(true);
        plugin.getLogger().info("Premium edition: licence validated.");
        platform.scheduler().async(this::activatePremiumStorage);
    }

    @Override
    public void onPremiumDisabled(String reason) {
        modules.markPremiumActive(false);
        this.tablistSort = TablistSort.BY_PRIORITY;
        this.groupConditions = GroupConditions.NONE;
        plugin.getLogger().warning("Premium module inactive: " + reason
                + " - the plugin keeps running in the Free edition.");
    }
}
