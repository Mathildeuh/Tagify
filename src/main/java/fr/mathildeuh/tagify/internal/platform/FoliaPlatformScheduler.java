package fr.mathildeuh.tagify.internal.platform;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.TimeUnit;

/** Implémentation région-aware pour Folia, basée sur les schedulers de {@code io.papermc.paper.threadedregions.scheduler}. */
public final class FoliaPlatformScheduler implements PlatformScheduler {

    private final Plugin plugin;

    public FoliaPlatformScheduler(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean isFolia() {
        return true;
    }

    @Override
    public void global(Runnable task) {
        Bukkit.getGlobalRegionScheduler().execute(plugin, task);
    }

    @Override
    public void globalLater(Runnable task, long delayTicks) {
        Bukkit.getGlobalRegionScheduler().runDelayed(plugin, t -> task.run(), Math.max(1L, delayTicks));
    }

    @Override
    public PlatformTask globalTimer(Runnable task, long delayTicks, long periodTicks) {
        var handle = Bukkit.getGlobalRegionScheduler()
                .runAtFixedRate(plugin, t -> task.run(), Math.max(1L, delayTicks), Math.max(1L, periodTicks));
        return handle::cancel;
    }

    @Override
    public void entity(Entity entity, Runnable task) {
        entity.getScheduler().run(plugin, t -> task.run(), null);
    }

    @Override
    public void async(Runnable task) {
        Bukkit.getAsyncScheduler().runNow(plugin, t -> task.run());
    }

    @Override
    public void asyncLater(Runnable task, long delayMillis) {
        Bukkit.getAsyncScheduler().runDelayed(plugin, t -> task.run(), Math.max(1L, delayMillis), TimeUnit.MILLISECONDS);
    }

    @Override
    public PlatformTask asyncTimer(Runnable task, long delayMillis, long periodMillis) {
        var handle = Bukkit.getAsyncScheduler().runAtFixedRate(
                plugin, t -> task.run(),
                Math.max(1L, delayMillis), Math.max(1L, periodMillis), TimeUnit.MILLISECONDS);
        return handle::cancel;
    }

    @Override
    public void shutdown() {
        Bukkit.getGlobalRegionScheduler().cancelTasks(plugin);
        Bukkit.getAsyncScheduler().cancelTasks(plugin);
    }
}
