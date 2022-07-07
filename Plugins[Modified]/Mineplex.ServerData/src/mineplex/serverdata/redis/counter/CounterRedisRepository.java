package mineplex.serverdata.redis.counter;

import mineplex.serverdata.Region;
import mineplex.serverdata.redis.RedisRepository;
import mineplex.serverdata.servers.ConnectionData;
import redis.clients.jedis.Jedis;

/**
 * Redis repository to store the count for {@link Counter}
 * @author Shaun Bennett
 */
public class CounterRedisRepository extends RedisRepository
{
	private String _dataKey;

	public CounterRedisRepository(ConnectionData writeConnection, ConnectionData readConnection, Region region, String dataKey)
	{
		super(writeConnection, readConnection, region);

		_dataKey = dataKey;
	}

	public CounterRedisRepository(String dataKey)
	{
		super(Region.ALL);

		_dataKey = dataKey;
	}

	/**
	 * Get the current count inside the fountain
	 * @return The current count for the fountain
	 */
	public long getCount()
	{
		long count = 0;

		try (Jedis jedis = getResource(false))
		{
			count = Long.parseLong(jedis.get(getKey()));
		}
		catch (NumberFormatException ex)
		{
			ex.printStackTrace();
		}

		return count;
	}

	/**
	 * Increment the current count by {@code increment} and then return the latest
	 * count from redis. This is handled in an atomic process
	 * @param increment Amount to increment counter by
	 * @return The updated count from redis
	 */
	public long incrementCount(long increment)
	{
		long count = 0;

		try (Jedis jedis = getResource(true))
		{
			count = jedis.incrBy(getKey(), increment);
		}

		return count;
	}

	/**
	 * Reset the counter back to 0
	 * @return the value of the counter before it was reset
	 */
	public long reset()
	{
		long count = -1;

		try (Jedis jedis = getResource(true))
		{
			count = Long.parseLong(jedis.getSet(getKey(), "0"));
		}

		return count;
	}

	/**
	 * Get the key for this counter
	 * @return The key is used to store the value in redis
	 */
	private String getKey()
	{
		return getKey(_dataKey);
	}

//	private void setNX()
//	{
//		try (Jedis jedis = getResource(true))
//		{
//			jedis.setnx(getKey(), "0");
//		}
//	}
}
