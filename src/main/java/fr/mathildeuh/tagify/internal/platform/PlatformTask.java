package fr.mathildeuh.tagify.internal.platform;

/**
 * Poignée d'annulation d'une tâche planifiée, indépendante de l'implémentation
 * (Bukkit {@code BukkitTask} ou Folia {@code ScheduledTask}).
 */
@FunctionalInterface
public interface PlatformTask {

    PlatformTask NOOP = () -> {
    };

    void cancel();
}
