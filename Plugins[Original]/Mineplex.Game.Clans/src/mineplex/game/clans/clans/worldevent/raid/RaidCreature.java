package mineplex.game.clans.clans.worldevent.raid;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

import mineplex.game.clans.clans.worldevent.api.EventCreature;

public abstract class RaidCreature<T extends LivingEntity> extends EventCreature<T>
{
	public RaidCreature(RaidWorldEvent event, Location spawnLocation, String name, boolean useName, double health, double walkRange, boolean healthRegen, Class<T> entityClass)
	{
		super(event, spawnLocation, name, useName, health, walkRange, healthRegen, entityClass);
	}
	
	public abstract void handleDeath(Location location);
}