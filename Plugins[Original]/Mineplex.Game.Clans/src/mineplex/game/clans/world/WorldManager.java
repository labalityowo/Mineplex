package mineplex.game.clans.world;

import java.util.*;

import mineplex.core.common.util.UtilWorld;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.java.JavaPlugin;

import mineplex.core.MiniPlugin;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import org.bukkit.Location;

public class WorldManager extends MiniPlugin
{
	private static final Map<EntityType, Integer> CULL_LIMITS = new HashMap<>();
	private static final Set<EntityType> MINECART_TYPES = new HashSet<>();
	private static final int MIN_RANGE = 64;
	private static final int MIN_RANGE_SQUARED = MIN_RANGE * MIN_RANGE;

	static
	{
		// Animals
		CULL_LIMITS.put(EntityType.BAT, 50);
		CULL_LIMITS.put(EntityType.CHICKEN, 150);
		CULL_LIMITS.put(EntityType.COW, 150);
		CULL_LIMITS.put(EntityType.HORSE, 50);
		CULL_LIMITS.put(EntityType.IRON_GOLEM, 50);
		CULL_LIMITS.put(EntityType.MUSHROOM_COW, 50);
		CULL_LIMITS.put(EntityType.OCELOT, 50);
		CULL_LIMITS.put(EntityType.PIG, 150);
		CULL_LIMITS.put(EntityType.RABBIT, 50);
		CULL_LIMITS.put(EntityType.SHEEP, 150);
		CULL_LIMITS.put(EntityType.WOLF, 150);

		// Monsters
		CULL_LIMITS.put(EntityType.CAVE_SPIDER, 100);
		CULL_LIMITS.put(EntityType.CREEPER, 100);
		CULL_LIMITS.put(EntityType.ENDERMAN, 50);
		CULL_LIMITS.put(EntityType.ENDERMITE, 50);
		CULL_LIMITS.put(EntityType.SILVERFISH, 50);
		CULL_LIMITS.put(EntityType.SKELETON, 100);
		CULL_LIMITS.put(EntityType.SLIME, 50);
		CULL_LIMITS.put(EntityType.SPIDER, 100);
		CULL_LIMITS.put(EntityType.ZOMBIE, 100);

		// Nether
		CULL_LIMITS.put(EntityType.BLAZE, 50);
		CULL_LIMITS.put(EntityType.GHAST, 50);
		CULL_LIMITS.put(EntityType.MAGMA_CUBE, 50);
		CULL_LIMITS.put(EntityType.PIG_ZOMBIE, 50);

		MINECART_TYPES.add(EntityType.MINECART);
		MINECART_TYPES.add(EntityType.MINECART_CHEST);
		MINECART_TYPES.add(EntityType.MINECART_COMMAND);
		MINECART_TYPES.add(EntityType.MINECART_FURNACE);
		MINECART_TYPES.add(EntityType.MINECART_HOPPER);
		MINECART_TYPES.add(EntityType.MINECART_MOB_SPAWNER);
		MINECART_TYPES.add(EntityType.MINECART_TNT);
	}

	public WorldManager(JavaPlugin plugin)
	{
		super("Clan World Manager", plugin);
	}

	@EventHandler
	public void cullMobs(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SLOW)
			return;

		for (World world : getPlugin().getServer().getWorlds())
		{
			List<Player> players = world.getPlayers();
			Map<EntityType, Set<Entity>> entities = new HashMap<>();
			// For optimization reasons reuse location objects
			Location entityLocation = new Location(world, 0, 0, 0);
			Location playerLocation = new Location(world, 0, 0, 0);

			for (Entity entity : world.getEntities())
			{
				if (entity.getCustomName() != null)
				{
					continue;
				}

				EntityType entityType = entity.getType();

				if (entityType == EntityType.ARROW)
				{
					if (entity.getTicksLived() > 800)
					{
						entity.remove();
					}
				}
				else if (entityType == EntityType.DROPPED_ITEM)
				{
					if (entity.getTicksLived() > 2400)
					{
						entity.remove();
					}
				}
				else if (CULL_LIMITS.containsKey(entityType))
				{
					boolean cull = true;
					entity.getLocation(entityLocation);
					for (Player player : players)
					{
						player.getLocation(playerLocation);
						if (playerLocation.distanceSquared(entityLocation) <= MIN_RANGE_SQUARED)
						{
							cull = false;
							break;
						}
					}
					if (cull)
					{
						entities.computeIfAbsent(entityType, key -> new HashSet<>()).add(entity);
					}
				}
				else if (MINECART_TYPES.contains(entityType))
				{
					if (entity.getTicksLived() > 800)
					{
						entity.remove();
					}
				}
			}

			for (Map.Entry<EntityType, Set<Entity>> entry : entities.entrySet())
			{
				cull(entry.getKey(), entry.getValue(), CULL_LIMITS.get(entry.getKey()));
			}
		}
	}

	private void cull(EntityType type, Set<Entity> ents, int limit)
	{
		Iterator<Entity> iterator = ents.iterator();
		int culled = 0;
		while (iterator.hasNext() && ents.size() > limit)
		{
			Entity entity = iterator.next();
			entity.remove();
			iterator.remove();
			culled++;
		}
		if (culled != 0)
		{
			log("Culled " + culled + " " + type);
		}
	}
}
