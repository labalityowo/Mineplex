package mineplex.core.common.animation;

import org.bukkit.Location;

import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

/**
 * An implementation of the {@link Animator} which will teleport the provided entity along the animation path each tick.
 */

public class AnimatorEntity extends Animator
{
	
	private final Entity _ent;

	public AnimatorEntity(Plugin plugin, Entity ent)
	{
		super(plugin);
		_ent = ent;
	}
	
	@Override
	protected void tick(Location loc)
	{
		if(!_ent.isValid()) 
		{
			stop();
			return;
		}
		_ent.setVelocity(new Vector(0,0,0));
		_ent.teleport(loc);
	}

	@Override
    protected void finish(Location loc) {}
	
	public Entity getEntity()
	{
		return _ent;
	}

}
