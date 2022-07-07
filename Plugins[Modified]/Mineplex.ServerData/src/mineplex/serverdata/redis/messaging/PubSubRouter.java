package mineplex.serverdata.redis.messaging;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

/**
 * A pub/sub messager that simply routes messages to some underlying pub/sub implementation, which
 * is in turn represented by a multi-channel subscription and a publishing mechanism.
 * <p>
 * This class handles:
 * <ol>
 * <li>Providing a modular messaging interface that is thread-safe.
 * <li>Protecting pub/sub implementations from some bad client behavior/data.
 * <li>Routing messages for multiple subscribers to the same channel(s).
 * </ol>
 */
public class PubSubRouter implements PubSubMessager, Subscriber
{

    private final PubSubLibraryClient _pubSubClient;
    private final Map<String, Set<Subscriber>> _subscribers;

    private final ExecutorService _threadPool;

    public PubSubRouter(PubSubLibraryClient client)
    {
        if (client == null)
        {
            throw new IllegalArgumentException("pubsub client cannot be null");
        }

        this._pubSubClient = client;
        this._pubSubClient.setSubscriber(this);
        this._subscribers = new ConcurrentHashMap<String, Set<Subscriber>>();

        this._threadPool = Executors.newCachedThreadPool();
    }

    @Override
    public final ListenableFuture<Boolean> publish(String channel, String message)
    {
        if (channel == null || channel.isEmpty())
        {
            throw new IllegalArgumentException("channel cannot be null or empty");
        }

        // messages of 0 length are allowed. Null messages are treated as messages of 0 length.
        if (message == null)
        {
            message = "";
        }

        return _pubSubClient.publish(channel, message);
    }

    @Override
    public final ListenableFuture<Boolean> subscribe(String channel, Subscriber sub)
    {
        if (channel == null || channel.isEmpty() || sub == null)
        {
            throw new IllegalArgumentException("params cannot be null and channel cannot be empty");
        }

        ListenableFuture<Boolean> ret = null;

        Set<Subscriber> channelSubs = _subscribers.get(channel);
        if (channelSubs == null)
        {
            // uses CopyOnWriteArraySet for fast and consistent iteration (forwarding messages to
            // subscribers) but slow writes (adding/removing subscribers).
            // See a discussion of the issue here:
            // http://stackoverflow.com/questions/6720396/different-types-of-thread-safe-sets-in-java
            channelSubs = new CopyOnWriteArraySet<Subscriber>();
            _subscribers.put(channel, channelSubs);

            // starts a jedis subscription to the channel if there were no subscribers before
            ret = _pubSubClient.addChannel(channel);

        } else
        {
            ret = SettableFuture.create();
            ((SettableFuture<Boolean>) ret).set(true); // already subscribed, calls back immediately
        }

        channelSubs.add(sub);

        return ret;
    }

    @Override
    public final void unsubscribe(String channel, Subscriber sub)
    {
        if (channel == null || channel.isEmpty() || sub == null)
        {
            throw new IllegalArgumentException("params cannot be null and channel cannot be empty");
        }

        Set<Subscriber> channelSubs = _subscribers.get(channel);
        if (channelSubs == null)
        { // no subscribers for the channel to begin with.
            return;
        }

        channelSubs.remove(sub);

        // stops the subscription to this channel if the unsubscribed was the last subscriber
        if (channelSubs.isEmpty())
        {
            _subscribers.remove(channel);
            _pubSubClient.removeChannel(channel);
        }
    }

    @Override
    public final void onMessage(final String channel, final String message)
    {
        if (channel == null || message == null || channel.isEmpty())
        {
            return;
        }

        Set<Subscriber> channelSubs = _subscribers.get(channel);

        if (channelSubs == null)
        { // We should not still be listening
            _pubSubClient.removeChannel(channel);
            return;
        } else if (channelSubs.isEmpty())
        {
            _subscribers.remove(channel);
            _pubSubClient.removeChannel(channel);
            return;
        }

        for (final Subscriber sub : channelSubs)
        {

            // Gives subscribers their own thread from the thread pool in which to react to the
            // message.
            // Avoids interruptions and other problems while iterating over the subscriber set.
            _threadPool.execute(new Runnable()
            {
                @Override
                public void run()
                {
                    sub.onMessage(channel, message);
                }
            });
        }
    }

    @Override
    public void shutdown()
    {
        _pubSubClient.destroy();
    }

}
