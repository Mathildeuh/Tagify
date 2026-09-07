package fr.mathildeuh.tagify.internal.module;

import java.util.UUID;

/**
 * Service de synchronisation cross-serveur (implémenté par le module Premium via Redis pub/sub).
 * Le Core publie les changements ; l'implémentation propage aux autres serveurs et
 * rappelle le Core à la réception (via {@link ModuleContext}).
 */
public interface SyncService {

    SyncService NONE = new SyncService() {
        @Override
        public void publishPlayerUpdate(UUID playerId) {
        }

        @Override
        public void publishGroupUpdate(String groupKey) {
        }

        @Override
        public void publishReload() {
        }

        @Override
        public void close() {
        }
    };

    void publishPlayerUpdate(UUID playerId);

    void publishGroupUpdate(String groupKey);

    void publishReload();

    void close();
}
