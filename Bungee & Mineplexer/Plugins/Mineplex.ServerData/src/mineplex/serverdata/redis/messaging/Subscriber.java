package mineplex.serverdata.redis.messaging;

/**
 * A subscriber to a pub/sub channel.
 */
public interface Subscriber
{

    /**
     * Called when a message is sent on a channel that this is subscribed to.
     * <p>
     * No guarantees are made about what thread this will be called from.
     *
     * @param channel The channel that the message was sent on.
     * @param message The message that was received.
     */
    void onMessage(String channel, String message);

}
