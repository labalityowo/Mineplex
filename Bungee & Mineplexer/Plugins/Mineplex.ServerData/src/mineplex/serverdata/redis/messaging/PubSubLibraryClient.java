package mineplex.serverdata.redis.messaging;

import com.google.common.util.concurrent.ListenableFuture;

/**
 * A multi-channel subscription and publisher to a pub/sub messaging implementation. An interface to
 * the actual low-level pub/sub library, whatever it may be.
 *
 * For the sake of internal efficiency, this makes no guarantees for the sanity or unchangeability
 * of arguments passed into its methods. Clients should not generally interact with this directly.
 */
public interface PubSubLibraryClient
{

    /**
     * Publishes a message to all subscribers of a given channel.
     *
     * @param channel The channel to publish the message on.
     * @param message The message to send.
     * @return A future object that will complete after an unknown amount of time with
     *         <code>false</code> if for some locally known reason the message definitely could not
     *         be published, else completes with <code>true</code>.
     */
    ListenableFuture<Boolean> publish(String channel, String message);

    /**
     * Adds a channel to this subscription.
     *
     * @param channel The channel to add. Should not change after being passed in.
     * @return The asynchronous, future result of this attempt to add the channel. Will have
     *         <code>true</code> when the subscription starts successfully.
     */
    ListenableFuture<Boolean> addChannel(String channel);

    /**
     * Removes a channel from this subscription.
     *
     * @param channel The channel to remove. Should not change after being passed in.
     */
    void removeChannel(String channel);

    /**
     * Removes all channels from this subscription, kills its connections, and relinquishes any
     * resources it was occupying.
     * <p>
     * Depending on the implementation, once a subscription has been destroyed, it may not be
     * reusable and it may be necessary to construct a new one in order to resume.
     * <p>
     * Call this when the subscription is no longer being used. Holding unnecessary connections can
     * cause serious performance and other issues on both ends.
     */
    void destroy();

    /**
     * Sets the subscriber to inform of messages received by this subscription.
     *
     * @param sub The listener for this subscription.
     */
    void setSubscriber(Subscriber sub);

}
