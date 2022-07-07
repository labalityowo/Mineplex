package mineplex.serverdata.redis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import mineplex.serverdata.Region;
import mineplex.serverdata.Utility;
import mineplex.serverdata.data.DedicatedServer;
import mineplex.serverdata.data.MinecraftServer;
import mineplex.serverdata.data.ServerGroup;
import mineplex.serverdata.servers.ConnectionData;
import mineplex.serverdata.servers.ServerRepository;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;
import redis.clients.jedis.Tuple;
import redis.clients.jedis.exceptions.JedisConnectionException;

/**
 * RedisServerRepository offers a Redis-based implementation of {@link ServerRepository}
 * using a mixture of hash and JSON encoded storage.
 * @author Ty
 *
 */
public class RedisServerRepository extends RedisRepository implements ServerRepository
{
	public RedisServerRepository(ConnectionData writeConn, ConnectionData readConn, Region region)
	{
		super(writeConn, readConn, region);
	}
	
	@Override
	public Collection<MinecraftServer> getServerStatuses() 
	{
		return getServerStatusesByPrefix("");
	}

	@Override
	public Collection<MinecraftServer> getServerStatusesByPrefix(String prefix) 
	{
		Collection<MinecraftServer> servers = new HashSet<MinecraftServer>();

		try(Jedis jedis = getResource(false))
		{
			String setKey = concatenate("serverstatus", "minecraft", getRegion().toString());
			Pipeline pipeline = jedis.pipelined();

			List<Response<String>> responses = new ArrayList<Response<String>>();
			for (String serverName : getActiveNames(setKey))
			{
				if (prefix.isEmpty() || serverName.startsWith(prefix))
				{
					String dataKey = concatenate(setKey, serverName);
					responses.add(pipeline.get(dataKey));
				}
			}
			
			pipeline.sync();
			
			for (Response<String> response : responses)
			{
				String serializedData = response.get();
				MinecraftServer server = Utility.deserialize(serializedData, MinecraftServer.class);
				
				if (server != null)
				{
					servers.add(server);
				}
			}
		}

		return servers;
	}
	
	@Override
	public Collection<MinecraftServer> getServersByGroup(String serverGroup)
	{
		Collection<MinecraftServer> servers = new HashSet<MinecraftServer>();
		
		for (MinecraftServer server : getServerStatuses())
		{
			if (server.getGroup().equalsIgnoreCase(serverGroup))
			{
				servers.add(server);
			}
		}
		
		return servers;
	}
	
	@Override
	public MinecraftServer getServerStatus(String serverName)
	{
		MinecraftServer server = null;

		try(Jedis jedis = getResource(false))
		{
			String setKey = concatenate("serverstatus", "minecraft", getRegion().toString());
			String dataKey = concatenate(setKey, serverName);
			String serializedData = jedis.get(dataKey);
			server = Utility.deserialize(serializedData, MinecraftServer.class);
		}

		return server;
	}

	@Override
	public void updataServerStatus(MinecraftServer serverData, int timeout) 
	{
		try(Jedis jedis = getResource(true))
		{
			String serializedData = Utility.serialize(serverData);
			String serverName = serverData.getName();
			String setKey = concatenate("serverstatus", "minecraft", getRegion().toString());
			String dataKey = concatenate(setKey, serverName);
			long expiry = Utility.currentTimeSeconds() + timeout;
			
			Transaction transaction = jedis.multi();
			transaction.set(dataKey, serializedData);
			transaction.zadd(setKey, expiry, serverName);
			transaction.exec();
		}
	}

	@Override
	public void removeServerStatus(MinecraftServer serverData) 
	{
		try(Jedis jedis = getResource(true))
		{
			String serverName = serverData.getName();
			String setKey = concatenate("serverstatus", "minecraft", getRegion().toString());
			String dataKey = concatenate(setKey, serverName);
			
			Transaction transaction = jedis.multi();
			transaction.del(dataKey);
			transaction.zrem(setKey, serverName);
			transaction.exec();
		}
	}

	@Override
	public boolean serverExists(String serverName)
	{
		return getServerStatus(serverName) != null;
	}
	
	@Override
	public Collection<DedicatedServer> getDedicatedServers()
	{
		Collection<DedicatedServer> servers = new HashSet<DedicatedServer>();

		try(Jedis jedis = getResource(false))
		{
			String key = concatenate("serverstatus", "dedicated");
			Set<String> serverNames = jedis.smembers(key);
			HashMap<String, Response<Map<String, String>>> serverDatas = new HashMap<String, Response<Map<String, String>>>();
			
			Pipeline pipeline = jedis.pipelined();
			
			for (String serverName : serverNames)
			{
				String dataKey = concatenate(key, serverName);
				serverDatas.put(serverName, pipeline.hgetAll(dataKey));
			}
			
			pipeline.sync();

			for (Entry<String, Response<Map<String, String>>> responseEntry : serverDatas.entrySet())
			{
				Map<String, String> data = responseEntry.getValue().get();
				
				try
				{
					DedicatedServer server = new DedicatedServer(data);
					
					if (server.getRegion() == getRegion())
						servers.add(server);
				}
				catch (Exception ex)
				{
					System.out.println(responseEntry.getKey() + " Errored");
					throw ex;
				}
			}
		}

		return servers;
	}

	@Override
	public Collection<ServerGroup> getServerGroups(Collection<MinecraftServer> serverStatuses) 
	{
		Collection<ServerGroup> servers = new HashSet<ServerGroup>();

		try(Jedis jedis = getResource(false))
		{
			String key = "servergroups";
			Set<String> names = jedis.smembers(key);
			Set<Response<Map<String, String>>> serverDatas = new HashSet<Response<Map<String, String>>>();

			Pipeline pipeline = jedis.pipelined();

			for (String serverName : names)
			{
				String dataKey = concatenate(key, serverName);
				serverDatas.add(pipeline.hgetAll(dataKey));
			}
			
			pipeline.sync();
			
			for (Response<Map<String, String>> response : serverDatas)
			{
				Map<String, String> data = response.get();
				
				if (data.entrySet().size() == 0) 
				{
					// please no
//					System.out.println("Encountered empty map! Skipping...");
					continue;
				}
				
				try
				{
					ServerGroup serverGroup = new ServerGroup(data, serverStatuses);
					
					if (serverGroup.getRegion() == Region.ALL || serverGroup.getRegion() == getRegion())
						servers.add(serverGroup);
				}
				catch (Exception exception)
				{
					System.out.println("Error parsing ServerGroup : " + data.get("name"));
					exception.printStackTrace();
				}
			}
		}

		return servers;
	}

	/**
	 * @param key - the key where the sorted set of server sessions is stored
	 * @return the {@link Set} of active server names stored at {@code key} for non-expired
	 * servers.
	 */
	protected Set<String> getActiveNames(String key)
	{
		Set<String> names = new HashSet<String>();

		try(Jedis jedis = getResource(false))
		{
			String min = "(" + Utility.currentTimeSeconds();
			String max = "+inf";
			names = jedis.zrangeByScore(key, min, max);
		}

		return names;
	}
	
	/**
	 * @param key - the key where the sorted set of server sessions is stored
	 * @return the {@link Set} of dead (expired) server names stored at {@code key}.
	 */
	protected Set<String> getDeadNames(String key)
	{
		Set<String> names = new HashSet<String>();

		try(Jedis jedis = getResource(false))
		{
			String min = "-inf";
			String max = Utility.currentTimeSeconds() + "";
			names = jedis.zrangeByScore(key, min, max);
		}

		return names;
	}
	
	@Override
	public Collection<MinecraftServer> getDeadServers()
	{
		Set<MinecraftServer> servers = new HashSet<MinecraftServer>();

		try(Jedis jedis = getResource(false))
		{
			Pipeline pipeline = jedis.pipelined();
			String setKey = concatenate("serverstatus", "minecraft", getRegion().toString());
			String min = "-inf";
			String max = Utility.currentTimeSeconds() + "";
	
			List<Response<String>> responses = new ArrayList<Response<String>>();
			for (Tuple serverName : jedis.zrangeByScoreWithScores(setKey, min, max))
			{
				String dataKey = concatenate(setKey, serverName.getElement());
				responses.add(pipeline.get(dataKey));
			}
			
			pipeline.sync();
			
			for (Response<String> response : responses)
			{
				String serializedData = response.get();
				MinecraftServer server = Utility.deserialize(serializedData, MinecraftServer.class);
				
				if (server != null)
					servers.add(server);
			}
		}

		return servers;
	}
	
	@Override
	public void updateServerGroup(ServerGroup serverGroup)
	{
		try(Jedis jedis = getResource(true))
		{
			HashMap<String, String> serializedData = serverGroup.getDataMap();
			System.out.println(serializedData);
			String serverGroupName = serverGroup.getName();
			String key = "servergroups";
			String dataKey = concatenate(key, serverGroupName);
			
			Transaction transaction = jedis.multi();
			transaction.hmset(dataKey, serializedData);
			transaction.sadd(key, serverGroupName);
			transaction.exec();
		}
	}

	@Override
	public void removeServerGroup(ServerGroup serverGroup)
	{
		try(Jedis jedis = getResource(true))
		{
			String serverName = serverGroup.getName();
			String setKey = "servergroups";
			String dataKey = concatenate(setKey, serverName);
			
			Transaction transaction = jedis.multi();
			transaction.del(dataKey);
			transaction.srem(setKey, serverName);
			transaction.exec();
		}
	}

	@Override
	public ServerGroup getServerGroup(String serverGroup)
	{
		ServerGroup server = null;
		try(Jedis jedis = getResource(false))
		{
			String key = concatenate("servergroups", serverGroup);
			Map<String, String> data = jedis.hgetAll(key);

			server = new ServerGroup(data, null);
		}

		return server;
	}
	
	/*
	 * <region> = "US" or "EU"
	 * serverstatus.minecraft.<region>.<name> stores the JSON encoded information of an active MinecraftServer instance.
	 * serverstatus.minecraft.<region> stores a sorted set with the set of name's for MinecraftServers 
	 * with a value of their expiry date (in ms)
	 * 
	 * -----------------------
	 * 
	 * serverstatus.dedicated.<name> stores the hash containing information of an active dedicated server instance
	 * serverstatus.dedicated stores the set of active dedicated server names.
	 * serverstatus.dedicated uses a hash with the following keys:
	 * name, publicAddress, privateAddress, region, cpu, ram
	 * 
	 * Example commands for adding/creating a new dedicated server:
	 * 1. HMSET serverstatus.dedicated.<name> name <?> publicAddress <?> privateAddress <?> region <?> cpu <?> ram <?>
	 * 2. SADD serverstatus.dedicated <name>
	 * 
	 * ------------------------
	 * 
	 * servergroups.<name> stores the hash-set containing information for the server group type.
	 * servergroups stores the set of active server group names.
	 * servergroups.<name> stores a hash of the following key name/values
	 * name, prefix, scriptName, ram, cpu, totalServers, joinableServers
	 * 
	 * Example commands for adding/creating a new server group:
	 * 
	 * 1. HMSET servergroups.<name> name <?> prefix <?> scriptName <?> ram <?> cpu <?> totalServers <?> joinableServers <?>
	 * 2. SADD servergroups <name>
	 */
}
