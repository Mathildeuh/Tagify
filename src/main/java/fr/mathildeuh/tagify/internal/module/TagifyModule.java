package fr.mathildeuh.tagify.internal.module;

/**
 * Point d'extension implémenté par le module Premium (classe
 * {@code fr.mathildeuh.tagify.premium.PremiumModule}), découvert par réflexion.
 *
 * <p>{@link #onLoad(ModuleContext)} enregistre de façon synchrone ce qui est sûr (fabriques de
 * backends, providers). La vérification de licence est lancée en asynchrone par le module
 * lui-même, qui rappelle ensuite {@code ModuleContext#onPremiumEnabled()} ou
 * {@code onPremiumDisabled(reason)}.
 */
public interface TagifyModule {

    String id();

    void onLoad(ModuleContext context);

    default void onEnable() {
    }

    default void onDisable() {
    }
}
