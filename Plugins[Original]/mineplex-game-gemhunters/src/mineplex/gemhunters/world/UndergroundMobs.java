package mineplex.gemhunters.world;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Spider;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.plugin.java.JavaPlugin;

import mineplex.core.Managers;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilMath;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class UndergroundMobs implements Listener
{
	private static final int MAX_MOBS = 400;
	private static final String SEWER_KEY = "SEWER_MOB";
	private static final String SUBWAY_KEY = "SUBWAY_MOBS";

	private final WorldDataModule _worldData;
	
	private final World _world;
	private final Set<Entity> _entities;

	public UndergroundMobs(JavaPlugin plugin)
	{
		plugin.getServer().getPluginManager().registerEvents(this, plugin);

		_worldData = Managers.require(WorldDataModule.class);
		
		_world = _worldData.World;
		_entities = new HashSet<>();
	}

	@EventHandler
	public void update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC_20)
		{
			return;
		}

		Iterator<Entity> iterator = _entities.iterator();

		while (iterator.hasNext())
		{
			Entity entity = iterator.next();

			if (entity.isDead() || !entity.isValid())
			{
				entity.remove();
				iterator.remove();
			}
		}
				
		for (int i = 0; i < 10; i++)
		{
			if (_entities.size() >= MAX_MOBS)
			{
				break;
			}
			
			{
				Location location = UtilAlg.Random(_worldData.getCustomLocation(SEWER_KEY)).clone().add(0, 1, 0);
				Class<? extends Entity> clazz = UtilMath.random.nextBoolean() ? Zombie.class : Skeleton.class;
				Entity entity = _world.spawn(location, clazz);
				_entities.add(entity);
			}
			{
				Location location = UtilAlg.Random(_worldData.getCustomLocation(SUBWAY_KEY)).clone().add(0, 1, 0);
				Class<? extends Entity> clazz = Spider.class;
				Entity entity = _world.spawn(location, clazz);
				_entities.add(entity);
			}
		}
	}
	
	@EventHandler
	public void cancelSuffication(EntityDamageEvent event)
	{
		if (event.getCause() == DamageCause.SUFFOCATION && _entities.contains(event.getEntity()))
		{
			event.setCancelled(true);
		}
	}
}