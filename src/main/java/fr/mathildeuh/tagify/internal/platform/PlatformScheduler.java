package fr.mathildeuh.tagify.internal.platform;

import org.bukkit.entity.Entity;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

/**
 * Abstraction du scheduler, seule couche réellement dépendante du modèle de threads du
 * serveur (Bukkit mono-thread vs Folia région-aware).
 *
 * <ul>
 *   <li>{@link #global(Runnable)} : contexte « global region » sous Folia, thread principal sinon.
 *       Requis pour toute manipulation de scoreboard / team.</li>
 *   <li>{@link #entity(Entity, Runnable)} : contexte de la région de l'entité (suit l'entité sous Folia).</li>
 *   <li>{@link #async(Runnable)} : hors du tick serveur (I/O, requêtes DB/cache).</li>
 * </ul>
 */
public interface PlatformScheduler {

    boolean isFolia();

    void global(Runnable task);

    void globalLater(Runnable task, long delayTicks);

    PlatformTask globalTimer(Runnable task, long delayTicks, long periodTicks);

    void entity(Entity entity, Runnable task);

    void async(Runnable task);

    void asyncLater(Runnable task, long delayMillis);

    PlatformTask asyncTimer(Runnable task, long delayMillis, long periodMillis);

    void shutdown();

    /** Vue {@link Executor} du scheduler asynchrone (pour {@code CompletableFuture.supplyAsync}). */
    default Executor asyncExecutor() {
        return this::async;
    }

    /** Vue {@link Executor} du contexte global (thread principal / global region). */
    default Executor globalExecutor() {
        return this::global;
    }

    default <T> CompletableFuture<T> supplyGlobal(Supplier<T> supplier) {
        CompletableFuture<T> future = new CompletableFuture<>();
        global(() -> {
            try {
                future.complete(supplier.get());
            } catch (Throwable t) {
                future.completeExceptionally(t);
            }
        });
        return future;
    }

    default CompletableFuture<Void> runAsync(Runnable task) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        async(() -> {
            try {
                task.run();
                future.complete(null);
            } catch (Throwable t) {
                future.completeExceptionally(t);
            }
        });
        return future;
    }
}
