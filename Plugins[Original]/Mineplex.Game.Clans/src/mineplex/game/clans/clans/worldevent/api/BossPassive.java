package mineplex.game.clans.clans.worldevent.api;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Listener;

import mineplex.core.common.util.UtilServer;

public abstract class BossPassive<T extends EventCreature<Y>, Y extends LivingEntity> implements Listener
{
	private T _creature;
	private final int _cooldown;

	public BossPassive(T creature)
	{
		this(creature, 30);
	}
	
	public BossPassive(T creature, int cooldown)
	{
		_creature = creature;
		_cooldown = cooldown;
		Bukkit.getPluginManager().registerEvents(this, UtilServer.getPlugin());
	}
	
	public abstract boolean isProgressing();

	public abstract void tick();
	
	public int getCooldown()
	{
		return _cooldown;
	}

	public Y getEntity()
	{
		return getBoss().getEntity();
	}

	public T getBoss()
	{
		return _creature;
	}

	public Location getLocation()
	{
		return getEntity().getLocation();
	}
}