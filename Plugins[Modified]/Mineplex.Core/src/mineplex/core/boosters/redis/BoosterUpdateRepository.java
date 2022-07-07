package mineplex.core.boosters.redis;

import mineplex.serverdata.Region;
import mineplex.serverdata.redis.RedisRepository;
import org.bukkit.plugin.java.JavaPlugin;
import redis.clients.jedis.Jedis;

/**
 * @author Shaun Bennett
 */
public class BoosterUpdateRepository extends RedisRepository
{
	private JavaPlugin _plugin;

	public BoosterUpdateRepository(JavaPlugin plugin)
	{
		super(Region.ALL);

		_plugin = plugin;
		init();
	}

	private void init()
	{
		Thread thread = new Thread("Booster Subscriber")
		{
			@Override
			public void run()
			{
				try (Jedis jedis = getResource(false))
				{
					jedis.subscribe(new BoosterUpdateListener(_plugin), "minecraft.boosters");
				}
			}
		};

		thread.start();
	}
}
