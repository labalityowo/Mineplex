package mineplex.serverdata.redis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mineplex.serverdata.Region;
import mineplex.serverdata.Utility;
import mineplex.serverdata.data.Data;
import mineplex.serverdata.data.DataRepository;
import mineplex.serverdata.servers.ConnectionData;
import mineplex.serverdata.servers.ServerManager;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;

public class RedisDataRepository<T extends Data> extends RedisRepository implements DataRepository<T>
{
	// The class type of the elements stored in this repository
	private Class<T> _elementType;
	// A unique label designating the elements and this repository.
	private String _elementLabel;
	
	/**
	 * Class constructor
	 * @param writeConn
	 * @param readConn
	 * @param region
	 */
	public RedisDataRepository(ConnectionData writeConn, ConnectionData readConn, Region region, 
								Class<T> elementType, String elementLabel)
	{
		super(writeConn, readConn, region);
		_elementType = elementType;
		_elementLabel = elementLabel;
	}
	
	public RedisDataRepository(ConnectionData conn, Region region, Class<T> elementType, String elementLabel)
	{
		this(conn, conn, region, elementType, elementLabel);
	}
	
	public RedisDataRepository(Region region, Class<T> elementType, String elementLabel)
	{
		this(ServerManager.getMasterConnection(), ServerManager.getSlaveConnection(), region,
				elementType, elementLabel);
	}
	
	public String getElementSetKey()
	{
		return concatenate("data", _elementLabel, getRegion().toString());
	}
	
	public String generateKey(T element)
	{
		return generateKey(element.getDataId());
	}
	
	public String generateKey(String dataId)
	{
		return concatenate(getElementSetKey(), dataId);
	}
	
	@Override
	public Collection<T> getElements() 
	{
		return getElements(getActiveElements());
	}

	@Override
	public Collection<T> getElements(Collection<String> dataIds)
	{
		Collection<T> elements = new HashSet<T>();
		
		try(Jedis jedis = getResource(false))
		{
			Pipeline pipeline = jedis.pipelined();

			List<Response<String>> responses = new ArrayList<Response<String>>();
			for (String dataId : dataIds)
			{
				responses.add(pipeline.get(generateKey(dataId)));
			}
			
			// Block until all requests have received pipelined responses
			pipeline.sync();
			
			for (Response<String> response : responses)
			{
				String serializedData = response.get();
				T element = deserialize(serializedData);
				
				if (element != null)
				{
					elements.add(element);
				}
			}
		}

		return elements;
	}

	@Override
	public Map<String,T> getElementsMap(List<String> dataIds)
	{
		Map<String,T> elements = new HashMap<>();

		try(Jedis jedis = getResource(false))
		{
			Pipeline pipeline = jedis.pipelined();

			List<Response<String>> responses = new ArrayList<>();
			for (String dataId : dataIds)
			{
				responses.add(pipeline.get(generateKey(dataId)));
			}

			// Block until all requests have received pipelined responses
			pipeline.sync();

			for (int i = 0; i < responses.size(); i++)
			{
				String key = dataIds.get(i);

				Response<String> response = responses.get(i);
				String serializedData = response.get();
				T element = deserialize(serializedData);

				elements.put(key, element);
			}
		}

		return elements;
	}

	@Override
	public T getElement(String dataId)
	{
		T element = null;

		try(Jedis jedis = getResource(false))
		{
			String key = generateKey(dataId);
			String serializedData = jedis.get(key);
			element = deserialize(serializedData);
		}

		return element;
	}

	@Override
	public void addElement(T element, int timeout) 
	{
		try(Jedis jedis = getResource(true))
		{
			String serializedData = serialize(element);
			String dataId = element.getDataId();
			String setKey = getElementSetKey();
			String dataKey = generateKey(element);
			long expiry = currentTime() + timeout;
			
			Transaction transaction = jedis.multi();
			transaction.set(dataKey, serializedData);
			transaction.zadd(setKey, expiry, dataId.toString());
			transaction.exec();
		}
	}
	
	@Override
	public void addElement(T element)
	{
		addElement(element, 60 * 60 * 24 * 7 * 4 * 12 * 10);	// Set the timeout to 10 years
	}

	@Override
	public void removeElement(T element) 
	{
		removeElement(element.getDataId());
	}
	
	@Override
	public void removeElement(String dataId)
	{
		try(Jedis jedis = getResource(true))
		{
			String setKey = getElementSetKey();
			String dataKey = generateKey(dataId);
			
			Transaction transaction = jedis.multi();
			transaction.del(dataKey);
			transaction.zrem(setKey, dataId);
			transaction.exec();
		}
	}

	@Override
	public boolean elementExists(String dataId)
	{
		return getElement(dataId) != null;
	}

	@Override
	public int clean() 
	{
		try(Jedis jedis = getResource(true))
		{
			for (String dataId : getDeadElements())
			{
				String dataKey = generateKey(dataId);
				
				Transaction transaction = jedis.multi();
				transaction.del(dataKey);
				transaction.zrem(getElementSetKey(), dataId);
				transaction.exec();
			}
		}

		return 0;
	}

	protected Set<String> getActiveElements()
	{
		Set<String> dataIds = new HashSet<String>();

		try(Jedis jedis = getResource(false))
		{
			String min = "(" + currentTime();
			String max = "+inf";
			dataIds = jedis.zrangeByScore(getElementSetKey(), min, max);
		}

		return dataIds;
	}
	
	protected Set<String> getDeadElements()
	{
		Set<String> dataIds = new HashSet<String>();

		try(Jedis jedis = getResource(false))
		{
			String min = "-inf";
			String max = currentTime() + "";
			dataIds = jedis.zrangeByScore(getElementSetKey(), min, max);
		}

		return dataIds;
	}
	
	protected T deserialize(String serializedData)
	{
		return Utility.deserialize(serializedData, _elementType);
	}
	
	protected String serialize(T element)
	{
		return Utility.serialize(element);
	}
	
	protected Long currentTime()
	{
		return Utility.currentTimeSeconds();
	}
}
