package fr.mathildeuh.tagify;

import fr.mathildeuh.tagify.internal.BuildConstants;
import fr.mathildeuh.tagify.internal.TagifyCore;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

/**
 * Tagify plugin entry point. All the logic is assembled by {@link TagifyCore}.
 */
public final class TagifyPlugin extends JavaPlugin {

    private static TagifyPlugin instance;
    private TagifyCore core;

    public static TagifyPlugin get() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        getLogger().info("Tagify " + BuildConstants.fullVersion() + " (" + BuildConstants.BRANCH + ", "
                + (BuildConstants.SNAPSHOT ? "dev" : "stable") + ") - starting up.");
        try {
            this.core = new TagifyCore(this);
            core.enable();
        } catch (Throwable t) {
            getLogger().log(Level.SEVERE, "Tagify failed to start - the plugin is disabled.", t);
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        if (core != null) {
            try {
                core.disable();
            } catch (Throwable t) {
                getLogger().log(Level.WARNING, "Error while stopping Tagify", t);
            }
            core = null;
        }
        instance = null;
    }

    public TagifyCore core() {
        return core;
    }
}
