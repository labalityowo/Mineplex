package mineplex.serverdata.redis.atomic;

import mineplex.serverdata.Region;
import mineplex.serverdata.redis.RedisRepository;
import mineplex.serverdata.servers.ConnectionData;

import redis.clients.jedis.Jedis;

public class RedisStringRepository extends RedisRepository
{
	private final String _dataKey;
	private final int _expiration;

	public RedisStringRepository(ConnectionData writeConn, ConnectionData readConn, Region region, String dataKey, int expiryInSeconds)
	{
		super(writeConn, readConn, region);
		this._dataKey = dataKey;
		this._expiration = expiryInSeconds;
	}

	public RedisStringRepository(ConnectionData writeConn, ConnectionData readConn, Region region, String dataKey)
	{
		this(writeConn, readConn, region, dataKey, -1);
	}

	public void set(String key, String value)
	{
		try (Jedis jedis = getResource(true))
		{
			if (_expiration == -1)
			{
				jedis.set(generateKey(key), value);
			}
			else
			{
				jedis.setex(generateKey(key), _expiration, value);
			}
		}
	}

	public String get(String key)
	{
		String element;

		try (Jedis jedis = getResource(false))
		{
			element = jedis.get(generateKey(key));
		}

		return element;
	}

	public void del(String key)
	{
		try (Jedis jedis = getResource(true))
		{
			jedis.del(generateKey(key));
		}
	}

	private String getElementSetKey()
	{
		return concatenate("data", _dataKey, getRegion().toString());
	}

	private String generateKey(String dataId)
	{
		return concatenate(getElementSetKey(), dataId);
	}
}
