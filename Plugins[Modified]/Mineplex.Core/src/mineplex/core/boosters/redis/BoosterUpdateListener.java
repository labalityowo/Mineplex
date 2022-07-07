package mineplex.core.boosters.redis;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import mineplex.core.boosters.Booster;
import mineplex.core.boosters.event.BoosterUpdateEvent;
import mineplex.core.common.api.ApiFieldNamingStrategy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import redis.clients.jedis.JedisPubSub;

import java.util.List;
import java.util.Map;

/**
 * @author Shaun Bennett
 */
public class BoosterUpdateListener extends JedisPubSub
{
	private Gson _gson = new GsonBuilder().setFieldNamingStrategy(new ApiFieldNamingStrategy())
			.setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX").create();
	private JavaPlugin _plugin;

	public BoosterUpdateListener(JavaPlugin plugin)
	{
		_plugin = plugin;
	}

	@Override
	public void onMessage(String channel, String message)
	{
		try
		{
			Map<String, List<Booster>> boosterMap = _gson.fromJson(message, new TypeToken<Map<String, List<Booster>>>() {}.getType());
			_plugin.getServer().getScheduler().runTask(_plugin, () -> Bukkit.getPluginManager().callEvent(new BoosterUpdateEvent(boosterMap)));
		}
		catch (Exception e)
		{
			System.out.println("Failed to load booster update");
			e.printStackTrace();
		}
	}
}
