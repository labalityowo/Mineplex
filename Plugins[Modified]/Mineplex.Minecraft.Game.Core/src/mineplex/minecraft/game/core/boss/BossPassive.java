package mineplex.minecraft.game.core.boss;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.Listener;

public abstract class BossPassive<T extends EventCreature, Y extends Entity> implements Listener
{
	private T _creature;

	public BossPassive(T creature)
	{
		_creature = creature;
		Bukkit.getPluginManager().registerEvents(this, creature.getEvent().getPlugin());
	}
	
	public int getCooldown()
	{
		return 3;
	}

	public Y getEntity()
	{
		return (Y) getBoss().getEntity();
	}

	public T getBoss()
	{
		return _creature;
	}

	public Location getLocation()
	{
		return getEntity().getLocation();
	}
	
	public abstract boolean isProgressing();

	public abstract void tick();
}