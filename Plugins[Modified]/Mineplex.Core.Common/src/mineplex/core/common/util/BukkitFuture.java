package mineplex.core.common.util;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Utilities for interleaving Bukkit scheduler operations as
 * intermediate and terminal operations in a {@link CompletionStage}
 * pipeline.
 * <p>
 * Any {@link Function}s returned by methods are suitable for use
 * in {@link CompletionStage#thenCompose(Function)}
 *
 * @see CompletableFuture#thenCompose(Function)
 */
public class BukkitFuture
{
    private static final Plugin LOADING_PLUGIN = JavaPlugin.getProvidingPlugin(BukkitFuture.class);

    private static void runBlocking(Runnable action)
    {
        Bukkit.getScheduler().runTask(LOADING_PLUGIN, action);
    }

    /**
     * Finalize a {@link CompletionStage} by consuming its value
     * on the main thread.
     *
     * @param action the {@link Consumer} to call on the main thread
     * @return a {@link Function} to be passed as an argument to
     *         {@link CompletionStage#thenCompose(Function)}
     * @see CompletableFuture#thenCompose(Function)
     */
    public static <T> Function<T, CompletionStage<Void>> accept(Consumer<? super T> action)
    {
        return val ->
        {
            CompletableFuture<Void> future = new CompletableFuture<>();
            runBlocking(() ->
            {
                action.accept(val);
                future.complete(null);
            });
            return future;
        };
    }

    /**
     * Finalize a {@link CompletionStage} by executing code on the
     * main thread after its completion.
     *
     * @param action the {@link Runnable} that will execute
     * @return a {@link Function} to be passed as an argument to
     *         {@link CompletionStage#thenCompose(Function)}
     * @see CompletableFuture#thenCompose(Function)
     */
    public static <T> Function<T, CompletionStage<Void>> run(Runnable action)
    {
        return val ->
        {
            CompletableFuture<Void> future = new CompletableFuture<>();
            runBlocking(() ->
            {
                action.run();
                future.complete(null);
            });
            return future;
        };
    }

    /**
     * Transform a value contained within a {@link CompletionStage}
     * by executing a mapping {@link Function} on the main thread.
     *
     * @param fn the {@link Function} used to transform the value
     * @return a {@link Function} to be passed as an argument to
     *         {@link CompletionStage#thenCompose(Function)}
     * @see CompletableFuture#thenCompose(Function)
     */
    public static <T,U> Function<T, CompletionStage<U>> map(Function<? super T,? extends U> fn)
    {
        return val ->
        {
            CompletableFuture<U> future = new CompletableFuture<>();
            runBlocking(() -> future.complete(fn.apply(val)));
            return future;
        };
    }

    /**
     * Finalize a {@link CompletionStage} by executing code on the
     * main thread after its normal or exceptional completion.
     *
     * @param action the {@link BiConsumer} that will execute
     * @return a {@link BiConsumer} to be passed as an argument to
     *         {@link CompletionStage#whenComplete(BiConsumer)}
     * @see CompletableFuture#whenComplete(BiConsumer)
     */
    public static <T> BiConsumer<? super T,? super Throwable> complete(BiConsumer<? super T,? super Throwable> action)
    {
        return (val, throwable) -> runBlocking(() -> action.accept(val, throwable));
    }

    /**
     * Create a {@link CompletionStage} from a supplier executed on the
     * main thread.
     *
     * @param supplier the supplier to run on the main thread
     * @return a {@link CompletionStage} whose value will be supplied
     *         during the next Minecraft tick
     */
    public static <T> CompletionStage<T> supply(Supplier<T> supplier)
    {
        CompletableFuture<T> future = new CompletableFuture<>();
        runBlocking(() -> future.complete(supplier.get()));
        return future;
    }
}
