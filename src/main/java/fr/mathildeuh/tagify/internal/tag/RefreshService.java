package fr.mathildeuh.tagify.internal.tag;

import fr.mathildeuh.tagify.internal.platform.PlatformScheduler;
import fr.mathildeuh.tagify.internal.platform.PlatformTask;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.IntSupplier;

/**
 * File de rafraîchissement débouncée. Les demandes accumulent des UUID puis sont vidées
 * en un seul lot après {@code debounce-ticks}, sur le thread global. Évite les rafales lors
 * de recalculs de permissions massifs.
 */
public final class RefreshService {

    private final PlatformScheduler scheduler;
    private final Consumer<UUID> apply;
    private final IntSupplier debounceTicks;

    private final Set<UUID> pending = ConcurrentHashMap.newKeySet();
    private final AtomicBoolean flushScheduled = new AtomicBoolean(false);

    private PlatformTask intervalTask;

    public RefreshService(PlatformScheduler scheduler, Consumer<UUID> apply, IntSupplier debounceTicks) {
        this.scheduler = scheduler;
        this.apply = apply;
        this.debounceTicks = debounceTicks;
    }

    /** Programme un rafraîchissement débouncé du joueur. */
    public void request(UUID playerId) {
        pending.add(playerId);
        if (flushScheduled.compareAndSet(false, true)) {
            scheduler.globalLater(this::flush, Math.max(1, debounceTicks.getAsInt()));
        }
    }

    public void requestAll(Collection<UUID> playerIds) {
        playerIds.forEach(this::request);
    }

    /** Rafraîchit immédiatement (commandes admin), toujours sur le thread global. */
    public void requestImmediate(UUID playerId) {
        scheduler.global(() -> apply.accept(playerId));
    }

    private void flush() {
        flushScheduled.set(false);
        List<UUID> batch = new ArrayList<>(pending);
        pending.clear();
        for (UUID id : batch) {
            try {
                apply.accept(id);
            } catch (Throwable ignored) {
                // une erreur sur un joueur ne doit pas bloquer le lot
            }
        }
    }

    /** (Ré)active le rafraîchissement périodique global. {@code seconds <= 0} le désactive. */
    public void configureInterval(int seconds, Runnable refreshAll) {
        if (intervalTask != null) {
            intervalTask.cancel();
            intervalTask = null;
        }
        if (seconds > 0) {
            long ticks = seconds * 20L;
            intervalTask = scheduler.globalTimer(refreshAll, ticks, ticks);
        }
    }

    public void shutdown() {
        if (intervalTask != null) {
            intervalTask.cancel();
            intervalTask = null;
        }
        pending.clear();
    }
}
