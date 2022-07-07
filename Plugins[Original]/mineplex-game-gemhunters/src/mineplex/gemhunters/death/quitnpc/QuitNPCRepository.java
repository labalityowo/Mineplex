package mineplex.gemhunters.death.quitnpc;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import mineplex.core.common.util.UtilServer;
import mineplex.serverdata.Region;
import mineplex.serverdata.redis.RedisRepository;
import redis.clients.jedis.Jedis;

public class QuitNPCRepository extends RedisRepository
{
	private static final String REDIS_KEY_PREFIX = "GemHuntersNPC.";

	public QuitNPCRepository()
	{
		super(Region.ALL);
	}

	public CompletableFuture<String> loadNpcServer(UUID uuid)
	{
		return CompletableFuture.supplyAsync(() ->
		{
			try (Jedis jedis = getResource(false))
			{
				return jedis.get(getKey(REDIS_KEY_PREFIX + uuid.toString()));
			}
		});
	}

	public void deleteNpc(UUID uuid)
	{
		UtilServer.runAsync(() ->
		{
			try (Jedis jedis = getResource(true))
			{
				jedis.del(getKey(REDIS_KEY_PREFIX + uuid.toString()));
			}
		});
	}

	public void insertNpc(UUID uuid, String serverName)
	{
		UtilServer.runAsync(() ->
		{
			try (Jedis jedis = getResource(true))
			{
				jedis.setex(getKey(REDIS_KEY_PREFIX + uuid.toString()), 60, serverName);
			}
		});
	}
}