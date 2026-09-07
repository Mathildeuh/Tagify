package fr.mathildeuh.tagify.internal.gui;

import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Fournisseur d'interfaces d'administration. Le Core en fournit une version simplifiée ;
 * le module Premium peut la remplacer (édition live, aperçu de gradient, etc.) via
 * {@code ModuleContext#setGuiProvider}.
 */
public interface GuiProvider {

    void openMain(Player admin);

    void openGroupList(Player admin);

    void openGroupEditor(Player admin, String groupName);

    void openPlayerEditor(Player admin, UUID target, String targetName);
}
