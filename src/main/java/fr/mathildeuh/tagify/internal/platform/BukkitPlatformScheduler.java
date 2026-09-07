package fr.mathildeuh.tagify.internal.platform;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

/** Implémentation classique basée sur le {@code BukkitScheduler} mono-thread. */
public final class BukkitPlatformScheduler implements PlatformScheduler {

    private final Plugin plugin;

    public BukkitPlatformScheduler(Plugin plugin) {
        this.plugin = plugin;
    }

    private static long toTicks(long millis) {
        return Math.max(1L, millis / 50L);
    }

    @Override
    public boolean isFolia() {
        return false;
    }

    @Override
    public void global(Runnable task) {
        if (Bukkit.isPrimaryThread()) {
            task.run();
        } else {
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }

    @Override
    public void globalLater(Runnable task, long delayTicks) {
        Bukkit.getScheduler().runTaskLater(plugin, task, Math.max(1L, delayTicks));
    }

    @Override
    public PlatformTask globalTimer(Runnable task, long delayTicks, long periodTicks) {
        BukkitTask handle = Bukkit.getScheduler()
                .runTaskTimer(plugin, task, Math.max(1L, delayTicks), Math.max(1L, periodTicks));
        return handle::cancel;
    }

    @Override
    public void entity(Entity entity, Runnable task) {
        global(task);
    }

    @Override
    public void async(Runnable task) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
    }

    @Override
    public void asyncLater(Runnable task, long delayMillis) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, task, toTicks(delayMillis));
    }

    @Override
    public PlatformTask asyncTimer(Runnable task, long delayMillis, long periodMillis) {
        BukkitTask handle = Bukkit.getScheduler()
                .runTaskTimerAsynchronously(plugin, task, toTicks(delayMillis), toTicks(periodMillis));
        return handle::cancel;
    }

    @Override
    public void shutdown() {
        Bukkit.getScheduler().cancelTasks(plugin);
    }
}
