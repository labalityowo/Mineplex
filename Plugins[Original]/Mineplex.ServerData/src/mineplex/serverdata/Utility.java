package mineplex.serverdata;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.Gson;

import mineplex.serverdata.servers.ConnectionData;
import mineplex.serverdata.servers.ServerManager;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * Utility offers various necessary utility-based methods for use in Mineplex.ServerData.
 * @author Ty
 *
 */
public class Utility
{
	private static boolean _retrievedRedisTime = false;
	private static long _millisTimeDifference;
	
	// The Gson instance used to serialize/deserialize objects in JSON form.
	private static Gson _gson = new Gson();
	public static Gson getGson() { return _gson; }

    // map of all instantiated connection pools, distinguished by their ip:port combination
    private static final ConcurrentHashMap<String, JedisPool> _pools = new ConcurrentHashMap<String, JedisPool>();

	// Public static jedis pool for interacting with central default jedis repo.
	private static JedisPool _masterPool;
	private static JedisPool _slavePool;
	private static final Object _poolLock = new Object();

	/**
	 * @param object - the (non-null) object to serialize
	 * @return the serialized form of {@code object}.
	 */
	public static String serialize(Object object)
	{
		return _gson.toJson(object);
	}

	/**
	 * @param serializedData - the serialized data to be deserialized
	 * @param type - the resulting class type of the object to be deserialized
	 * @return the deserialized form of {@code serializedData} for class {@code type}.
	 */
	public static <T> T deserialize(String serializedData, Class<T> type)
	{
		if (serializedData == null) return null;
		
		return _gson.fromJson(serializedData, type);
	}

	/**
	 * @param delimiter - the delimiter character used to separate the concatenated elements
	 * @param elements - the set of string elements to be concatenated and returned.
	 * @return the concatenated string of all {@code elements} separated by the {@code delimiter}.
	 */
	public static String concatenate(char delimiter, String... elements)
	{
		int length = elements.length;
		String result = length > 0 ? elements[0] : new String();

		for (int i = 1; i < length; i++)
		{
			result += delimiter + elements[i];
		}

		return result;
	}

	/**
	 * @return the current timestamp (in seconds) fetched from the central jedis repository
	 * for synced timestamps.
	 */
	public static long currentTimeSeconds()
	{
		if (!_retrievedRedisTime)
			setTimeDifference();
		
		return (System.currentTimeMillis() + _millisTimeDifference) / 1000;
	}

	/**
	 * @return the current timestamp (in milliseconds) fetched from the central jedis repository
	 * for synced timestamps.
	 */
	public static long currentTimeMillis()
	{
		if (!_retrievedRedisTime)
			setTimeDifference();

		return System.currentTimeMillis() + _millisTimeDifference;
	}

	/**
	 * @param connData - the connection data specifying the database to be connected to.
	 * @return a newly instantiated {@link JedisPool} connected to the provided {@link ConnectionData} repository.
	 */
	public static JedisPool generatePool(ConnectionData connData)
	{	    
	    synchronized(_poolLock)
	    {
		    String key = getConnKey(connData);
		    JedisPool pool = _pools.get(key);
		    
	    	if (pool == null)
		    {
		        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
		        jedisPoolConfig.setMaxWaitMillis(1000);
		        jedisPoolConfig.setMinIdle(5);
//		        jedisPoolConfig.setTestOnBorrow(true);

		        jedisPoolConfig.setMaxTotal(20);
		        jedisPoolConfig.setBlockWhenExhausted(true);

		        pool = new JedisPool(jedisPoolConfig, connData.getHost(), connData.getPort());
		        _pools.put(key, pool);
		    }
	    	
	    	return pool;
	    }
	}

	/**
	 * @param writeable - whether or not the Jedis connections returned should be writeable to.
	 * @return a globally available {@link JedisPool}
	 */
	public static JedisPool getPool(boolean writeable)
	{
		if (writeable)
		{
			if (_masterPool == null)
			{
				_masterPool = generatePool(ServerManager.getMasterConnection());
			}

			return _masterPool;
		}
		else
		{
			if (_slavePool == null)
			{
				ConnectionData slave = ServerManager.getSlaveConnection();

				_slavePool = generatePool(slave);
			}

			return _slavePool;
		}
	}

    private static String getConnKey(ConnectionData connData)
    {
        return connData.getHost() + ":" + connData.getPort();
    }

    private static void setTimeDifference()
    {
	    long currentTime = 0;
	    JedisPool pool = getPool(false);

	    try (Jedis jedis = pool.getResource())
	    {
	    	// Try multiple times in case one isn't valid
		    // Addresses an error in sentry
		    List<String> times = jedis.time();
		    for (String time : times.subList(0, Math.min(5, times.size())))
		    {
			    try
			    {
				    currentTime = Long.parseLong(time);
				    break;
			    } catch (NumberFormatException ex) { }
		    }
	    }

	    _millisTimeDifference = (currentTime * 1000) - System.currentTimeMillis();
    }
}
