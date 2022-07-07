package mineplex.serverdata.redis;

import mineplex.serverdata.Region;
import mineplex.serverdata.Utility;
import mineplex.serverdata.servers.ConnectionData;
import mineplex.serverdata.servers.ServerManager;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * Repository for managing Redis Connections
 * @author Shaun Bennett
 */
public class RedisRepository
{
	protected static final char KEY_DELIMITER = '.';

	private JedisPool _writePool;
	private JedisPool _readPool;
	private Region _region;

	public RedisRepository(ConnectionData writeConn, ConnectionData readConn, Region region)
	{
		_writePool = Utility.generatePool(writeConn);
		_readPool = (writeConn == readConn) ? _writePool : Utility.generatePool(readConn);
		_region = region;
	}

	public RedisRepository(Region region)
	{
		this(ServerManager.getMasterConnection(), ServerManager.getSlaveConnection(), region);
	}

	/**
	 * Get a Jedis Resource from the pool. This Jedis instance needs to be closed when you are done with using it.
	 * Call jedis.close() or use try with resources when using getResource()
	 *
	 * @param writeable If we need to be able to write to redis. Trying to write to a non writeable jedis instance will
	 *                  throw an error.
	 * @return {@link Jedis} instance from pool
	 */
	protected Jedis getResource(boolean writeable)
	{
		return (writeable ? _writePool : _readPool).getResource();
	}

	/**
	 * Get the server region that this redis repository is for. The region will affect the keys for redis
	 * @return server region
	 */
	public Region getRegion()
	{
		return _region;
	}

	protected String getKey(String dataKey)
	{
		return concatenate("minecraft", "data", _region.name(), dataKey);
	}

	/**
	 * @param elements - the elements to concatenate together
	 * @return the concatenated form of all {@code elements}
	 * separated by the delimiter {@value KEY_DELIMITER}.
	 */
	protected String concatenate(String... elements)
	{
		return Utility.concatenate(KEY_DELIMITER, elements);
	}
}
