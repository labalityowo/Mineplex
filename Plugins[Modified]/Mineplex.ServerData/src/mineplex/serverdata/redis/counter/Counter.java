package mineplex.serverdata.redis.counter;

import mineplex.serverdata.Region;
import mineplex.serverdata.servers.ConnectionData;

import java.util.concurrent.atomic.AtomicLong;

/**
 * A counter represents an incrementing atomic number that is stored and updated through redis. This allows for
 * multiple servers to share and update the same counter
 *
 * @author Shaun Bennett
 */
public class Counter
{
	// Cached count of the counter
	private final AtomicLong _count = new AtomicLong(0);
	// The System.currentTimeMillis() when cached count was last updated
	private volatile long _lastUpdated;
	// The unique key to reference this counter
	private final String _dataKey;

	// Redis repository to store the count
	private final CounterRedisRepository _redisRepository;

	public Counter(ConnectionData writeConnection, ConnectionData readConnection, Region region, String dataKey)
	{
		_dataKey = dataKey;
		_redisRepository = new CounterRedisRepository(writeConnection, readConnection, region, dataKey);
	}

	public Counter(String dataKey)
	{
		_dataKey = dataKey;
		_redisRepository = new CounterRedisRepository(dataKey);
	}


	/**
	 * Add a value to the counter and return the new counter value. This method is thread-safe and interacts
	 * directly with the atomic value stored in redis. The value returned from redis is then returned
	 *
	 * addAndGet will also update the cached counter value so we don't need to make extra trips to redis
	 *
	 * @param amount the amount to add to the counter
	 * @return the updated value of the counter from redis repository
	 */
	public long addAndGet(long amount)
	{
		long newCount = _redisRepository.incrementCount(amount);
		updateCount(newCount);
		return newCount;
	}

	/**
	 * Get the latest cached count from the counter. This value will not be changed until {@link #addAndGet(long)}
	 * or {@link #updateCount} is called.
	 *
	 * @return The counter count
	 */
	public long getCount()
	{
		return _count.get();
	}

	/**
	 * Update the cached count to reflect the count in redis. This should be called async
	 */
	public void updateCount()
	{
		updateCount(_redisRepository.getCount());
	}

	/**
	 * Reset the counter back to 0. Immediately updates the redis repository.
	 *
	 * @return The value of the counter before it was reset
	 */
	public long reset()
	{
		updateCount(0);
		return _redisRepository.reset();
	}

	/**
	 * Get the data key for this counter. The data key is used as the redis repository key
	 * @return The data key for this counter
	 */
	public String getDataKey()
	{
		return _dataKey;
	}

	/**
	 * Update the cached count with a new value
	 * @param newCount updated count
	 */
	protected void updateCount(long newCount)
	{
		_count.set(newCount);
		_lastUpdated = System.currentTimeMillis();
	}
}
