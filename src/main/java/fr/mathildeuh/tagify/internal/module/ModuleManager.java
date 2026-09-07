package fr.mathildeuh.tagify.internal.module;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Discovers and drives the Premium module by reflection. A missing module or a load error
 * leave the plugin running in the Free edition.
 */
public final class ModuleManager {

    private static final String PREMIUM_CLASS = "fr.mathildeuh.tagify.premium.PremiumModule";

    private final Logger logger;
    private TagifyModule premium;
    private volatile boolean premiumActive;

    public ModuleManager(Logger logger) {
        this.logger = logger;
    }

    /** Loads and initialises the Premium module if it is present on the classpath. */
    public void load(ModuleContext context) {
        Class<?> clazz;
        try {
            clazz = Class.forName(PREMIUM_CLASS);
        } catch (ClassNotFoundException notPresent) {
            logger.info("Free edition - Premium module not present.");
            return;
        }

        try {
            premium = (TagifyModule) clazz.getDeclaredConstructor().newInstance();
            premium.onLoad(context);
            premium.onEnable();
            logger.info("Premium module detected - checking licence...");
        } catch (Throwable t) {
            logger.log(Level.WARNING, "Could not load the Premium module - Free edition: "
                    + t.getMessage());
            logger.log(Level.FINE, "Details", t);
            premium = null;
        }
    }

    public void disable() {
        if (premium != null) {
            try {
                premium.onDisable();
            } catch (Throwable t) {
                logger.log(Level.FINE, "Error while stopping the Premium module", t);
            }
            premium = null;
        }
        premiumActive = false;
    }

    public boolean isPremiumPresent() {
        return premium != null;
    }

    public boolean isPremiumActive() {
        return premiumActive;
    }

    public void markPremiumActive(boolean active) {
        this.premiumActive = active;
    }
}
