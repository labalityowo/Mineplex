package mineplex.serverdata.redis.messaging;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

import mineplex.serverdata.Utility;
import mineplex.serverdata.servers.ConnectionData;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisDataException;

/**
 * A subscription to a Redis pub/sub system through a Jedis client. Includes a publishing mechanism
 * as well.
 * <p>
 * Subscribes to Jedis and offers a variety of methods to edit the channels that this listens to
 * over time. Does not support pattern-matching, even though Jedis can. Takes a single subscriber to
 * inform of incoming messages (on all channels this is subscribed to).
 * <p>
 * For the sake of internal efficiency, this does not protect the sanity or unchangeability of
 * arguments passed into its methods. Clients should not generally interact with this directly.
 * <p>
 * The Jedis pub/sub interface is a little confusing, especially when it comes to multithreading. At
 * any given time, if this class is subscribed to any channels at all, it will have a single thread
 * that is listening for incoming messages from redis with a blocking method. After that listening
 * thread is created, we can add and remove subscriptions as desired, but the initial subscription
 * and actual listening must be done on its own thread. When all channels are unsubscribed from, the
 * listening thread returns. Note that this is stated with about 70% certainty, as the internals of
 * the pub/sub mechanism are not entirely clear to me.
 * <p>
 * This class maintains a constant connection to its redis server by subscribing to a base channel.
 * This makes it much easier to protect its operation from potentially insane client commands.
 * <p>
 * If the connection to the given redis instance fails or is interrupted, will keep attempting to
 * reconnect periodically until destroyed. Publishers and subscribers are not informed of failure in
 * any way.
 * <p>
 * When {@link #unsubscribe()} or {@link #destroy()} is called, this class ceases operation.
 */
public class PubSubJedisClient extends JedisPubSub implements PubSubLibraryClient
{

    private static final long RECONNECT_PERIOD_MILLIS = 800;
    private static final String BASE_CHANNEL = "pG8n5jp#";

    private static final String BOLD = "\u001B[1m";
    private static final String RESET = "\u001B[0m";

    private final String _id;
    private JedisPool _writePool;
    private final ConnectionData _readConn;
    private JedisPool _readPool;
    private final ExecutorService _threadPool = Executors.newCachedThreadPool();
    private volatile Subscriber _sub;

    private final Set<String> _channels = Collections
            .newSetFromMap(new ConcurrentHashMap<String, Boolean>());
    private final Map<String, SettableFuture<Boolean>> _pendingFutures = new ConcurrentHashMap<String, SettableFuture<Boolean>>();

    private volatile boolean _subscribed; // Is there a base subscription yet?
    private volatile boolean _destroyed; // has this been deliberately destroyed?

    /**
     * Class constructor.
     *
     * @param writeTo The connection info for the redis instance this client should publish to.
     * @param readFrom The connection info for the redis instance this client to subscribe to.
     */
    public PubSubJedisClient(ConnectionData writeTo, ConnectionData readFrom)
    {
        if (writeTo == null)
        {
            throw new IllegalArgumentException("redis connection info cannot be null");
        }

        _id = writeTo.getName();
        _writePool = Utility.generatePool(writeTo);

        _readConn = readFrom;
        _readPool = Utility.generatePool(readFrom);

        createSubscription(BASE_CHANNEL);
    }

    @Override
    public final synchronized void setSubscriber(Subscriber sub)
    {
        _sub = sub;
    }

    @Override
    public final ListenableFuture<Boolean> addChannel(String channel)
    {
        SettableFuture<Boolean> ret = _pendingFutures.get(channel);
        if (ret == null)
        {
            ret = SettableFuture.create();
            _pendingFutures.put(channel, ret);
        }

        try
        {
            _channels.add(channel);

            if (_subscribed)
            { // Already has a subscription thread and can just add a new channel to it.
                subscribe(channel);
            }
        } catch (Exception ex)
        {
            log("Encountered issue subscribing to a channel.");
            ex.printStackTrace();
            ret.setException(ex);
        }

        return ret;
    }

    @Override
    public final void removeChannel(String channel)
    {
        if (BASE_CHANNEL.equals(channel))
        { // Protects the base subscription
            return;
        }

        _channels.remove(channel);

        if (_subscribed)
        {
            unsubscribe(channel);
        }
    }

    @Override
    public final void unsubscribe()
    {
        destroy();
    }

    @Override
    public final void destroy()
    {
        _destroyed = true;
        try
        {
            super.unsubscribe();
        } catch (NullPointerException e)
        {
        }

        for (SettableFuture<Boolean> fut : _pendingFutures.values())
        {
            fut.set(false);
        }
    }

    @Override
    public final void onMessage(String channel, String message)
    {
        if (_sub == null || BASE_CHANNEL.equals(channel))
        {
            return;
        }

        try
        {
            _sub.onMessage(channel, message);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public final ListenableFuture<Boolean> publish(final String channel, final String message)
    {
        final SettableFuture<Boolean> ret = SettableFuture.create();
        _threadPool.execute(new Runnable()
        {
            @Override
            public void run()
            {
                Jedis bJedis = null;
                try
                {
                    bJedis = _writePool.getResource();
                    bJedis.publish(channel, message);
                    _writePool.returnResource((Jedis) bJedis);
                    ret.set(true);

                } catch (Exception e)
                {
                    log("Encountered issue while publishing a message.");
                    e.printStackTrace();
                    if (bJedis != null)
                    {
                        _writePool.returnBrokenResource((Jedis) bJedis);
                    }
                    ret.set(false);
                }
            }
        });
        return ret;
    }

    // Confirms successful subscriptions/unsubscriptions.
    @Override
    public void onSubscribe(String channel, int subscribedChannels)
    {

        // informs subscriber that this subscription completed successfully
        SettableFuture<Boolean> fut = _pendingFutures.remove(channel);
        if (fut != null)
        {
            fut.set(true);
        }

        if (!_subscribed)
        {
            for (String subscribeTo : _channels)
            {
                subscribe(subscribeTo);
            }
        }
        _subscribed = true;

        log("Subscribed to channel: " + channel);
    }

    @Override
    public void onUnsubscribe(String channel, int subscribedChannels)
    {
        log("Unsubscribed from channel: " + channel);
    }

    /**
     * Creates the initial listening thread which blocks as it polls redis for new messages.
     * Subsequent subscriptions can simply be added using {@link #subscribe(String...)} after the
     * subscription thread has been created.
     *
     * @param firstChannel The first channel to initially subscribe to. If you do not have a first
     *            channel, there is no reason to create a subscriber thread yet.
     */
    private void createSubscription(final String firstChannel)
    {

        final JedisPubSub pubsub = this;

        new Thread(new Runnable()
        {
            @Override
            public void run()
            {

                boolean first = true;

                while (!_destroyed)
                {

                    if (!first)
                    {
                        log("Jedis connection to " + _readConn.getHost() + ":"
                                + _readConn.getPort()
                                + " failed or was interrupted, attempting to reconnect");
                    }
                    first = false;

                    Jedis jedisInstance = null;

                    try
                    {
                        // gets a non-thread-safe jedis instance from the thread-safe pool.
                        jedisInstance = _readPool.getResource();

                        log("Creating initial jedis subscription to channel " + firstChannel);
                        // this will block as long as there are subscriptions
                        jedisInstance.subscribe(pubsub, firstChannel);

                        log("jedisInstance.subscribe() returned, subscription over.");

                        // when the subscription ends (subscribe() returns), returns the instance to
                        // the pool
                        _readPool.returnResource(jedisInstance);

                    } catch (JedisConnectionException e)
                    {
                        log("Jedis connection encountered an issue.");
                        e.printStackTrace();
                        if (jedisInstance != null)
                        {
                            _readPool.returnBrokenResource((Jedis) jedisInstance);
                        }

                    } catch (JedisDataException e)
                    {
                        log("Jedis connection encountered an issue.");
                        e.printStackTrace();
                        if (jedisInstance != null)
                        {
                            _readPool.returnBrokenResource((Jedis) jedisInstance);
                        }
                    }
                    _subscribed = false;

                    // sleeps for a short pause, rather than constantly retrying connection
                    if (!_destroyed)
                    {
                        try
                        {
                            Thread.sleep(RECONNECT_PERIOD_MILLIS);
                        } catch (InterruptedException e)
                        {
                            _destroyed = true;
                            log("Reconnection pause thread was interrupted.");
                            e.printStackTrace();
                        }
                    }
                }
            }
        }).start();
    }

    // This implementation does not support pattern-matching subscriptions
    @Override
    public void onPMessage(String pattern, String channel, String message)
    {
    }

    @Override
    public void onPSubscribe(String pattern, int subscribedChannels)
    {
    }

    @Override
    public void onPUnsubscribe(String pattern, int subscribedChannels)
    {
    }

    private void log(String msg)
    {
        System.out.println(BOLD + "[" + getClass().getSimpleName()
                + (_id != null && !_id.isEmpty() ? " " + _id : "") + "] " + RESET + msg);
    }

}
