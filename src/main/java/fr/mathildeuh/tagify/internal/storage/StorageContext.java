package fr.mathildeuh.tagify.internal.storage;

import fr.mathildeuh.tagify.internal.config.TagifyConfig;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.Executor;

/**
 * Dépendances fournies aux fabriques de {@link DataStore} (backends Premium inclus).
 *
 * @param plugin   instance du plugin (dossier de données, logger)
 * @param executor exécuteur asynchrone partagé pour les I/O
 * @param config   instantané de configuration courant (détails de connexion)
 */
public record StorageContext(Plugin plugin, Executor executor, TagifyConfig config) {
}
