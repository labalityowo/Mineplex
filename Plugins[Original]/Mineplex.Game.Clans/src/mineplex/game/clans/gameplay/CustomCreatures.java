package mineplex.game.clans.gameplay;

import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

public class CustomCreatures implements Listener
{

	private static final EntityType[] DISABLED_CREATURES = { EntityType.WITCH, EntityType.PIG_ZOMBIE,
															 EntityType.ENDERMAN };
	
	private static final EntityType[] DISABLED_NATURAL = { EntityType.HORSE };
	
	@EventHandler
	public void onCreatureSpawn(CreatureSpawnEvent event)
	{
		if (isDisabledCreature(event.getEntityType()))
		{
			event.setCancelled(true);
		}
		if (isDisabledNatural(event.getEntityType()) && event.getSpawnReason() != SpawnReason.CUSTOM)
		{
			event.setCancelled(true);
		}
	}
	
	private boolean isDisabledCreature(EntityType entityType)
	{
		for (EntityType disabledCreature : DISABLED_CREATURES)
		{
			if (disabledCreature == entityType)
			{
				return true;
			}
		}
		
		return false;
	}
	
	private boolean isDisabledNatural(EntityType entityType)
	{
		for (EntityType disabledCreature : DISABLED_NATURAL)
		{
			if (disabledCreature == entityType)
			{
				return true;
			}
		}
		
		return false;
	}
}