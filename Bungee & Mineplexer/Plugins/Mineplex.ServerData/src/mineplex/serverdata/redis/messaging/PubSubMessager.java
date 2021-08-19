package mineplex.serverdata.redis.messaging;

import com.google.common.util.concurrent.ListenableFuture;

/**
 * Messager for standard pub/sub model. Handles multiple publishers and subscribers.
 * <p>
 * All messaging is asynchronous and non-blocking, even to local subscribers.
 * <p>
 * For more about the pub/sub messaging paradigm, see <a
 * href="http://en.wikipedia.org/wiki/Publish%E2
 * %80%93subscribe_pattern">http://en.wikipedia.org/wiki/Publish%E2%80%93subscribe_pattern</a>
 */
public interface PubSubMessager
{

    /**
     * Publishes a message to all subscribers of a given channel.
     * <p>
     * Publishes to all connected subscribers, including local ones.
     *
     * @param channel The channel to publish the message on.
     * @param message The message to send.
     * @return A future object that will complete after an unknown amount of time with
     *         <code>false</code> if for some locally known reason the message definitely could not
     *         be published, else completes with <code>true</code> (which does not guarantee it
     *         succeeded 100%).
     */
    ListenableFuture<Boolean> publish(String channel, String message);

    /**
     * Subscribes to a messaging channel.
     * <p>
     * When incoming messages arrive, the subscriber is called from an arbitrary new thread.
     *
     * @param channel The channel to subscribe to.
     * @param sub The subscriber to inform of incoming messages.
     * @return The asynchronous, future result of this attempt to subscribe to the channel. Will
     *         have <code>true</code> when the subscription starts successfully.
     */
    ListenableFuture<Boolean> subscribe(String channel, Subscriber sub);

    /**
     * Unsubscribes from a messaging channel.
     *
     * @param channel The channel to unsubscribe from.
     * @param sub The subscriber to stop informing of incoming messages.
     */
    void unsubscribe(String channel, Subscriber sub);

    /**
     * Attempts to gracefully shut down this messager. Generally irreversible.
     */
    void shutdown();

}
