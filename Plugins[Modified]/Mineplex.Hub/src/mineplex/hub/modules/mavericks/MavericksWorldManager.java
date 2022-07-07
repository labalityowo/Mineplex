package mineplex.hub.modules.mavericks;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.plugin.java.JavaPlugin;

import mineplex.core.MiniPlugin;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.worldgen.WorldGenCleanRoom;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

/**
 * A small world manager for the mavericks world.
 */
public class MavericksWorldManager extends MiniPlugin
{
	private World _world;
	private Location _spawn;

	public MavericksWorldManager(JavaPlugin plugin)
	{
		super("Mavericks World", plugin);

		WorldCreator wc = new WorldCreator("Mav_Lobby");
		wc.generator(new WorldGenCleanRoom());
		_world = wc.createWorld();

		_world.setGameRuleValue("doDaylightCycle", "false");
		_world.setTime(6000);
		_world.setPVP(true);

		_spawn = new Location(_world, 1.5, 22, 287.5, -180, 0);
	}

	public Location getSpawn()
	{
		return _spawn.clone();
	}

	public World getWorld()
	{
		return _world;
	}

	public boolean isInWorld(Entity e)
	{
		return _world.equals(e.getWorld());
	}

	@EventHandler
	public void onSpawn(CreatureSpawnEvent event)
	{
		if(isInWorld(event.getEntity()))
		{
			if(event.getSpawnReason() == SpawnReason.CUSTOM) return;
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onWeather(WeatherChangeEvent event)
	{
		if(!event.getWorld().equals(_world)) return;

		if(!event.toWeatherState()) return;
		event.setCancelled(true);
	}

	@EventHandler
	public void borderCheck(UpdateEvent event)
	{
		if(event.getType() != UpdateType.FAST)
		{
			for(Player p : _world.getPlayers())
			{
				if (UtilMath.offset(p.getLocation(), _world.getSpawnLocation()) > 400)
				{
					p.eject();
					p.leaveVehicle();
					p.teleport(getSpawn());
				}
			}
		}
	}
}