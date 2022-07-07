package mineplex.core.gadget.types;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;

import mineplex.core.gadget.GadgetManager;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public abstract class ArrowEffectGadget extends Gadget
{

	protected final Set<Arrow> _arrows = new HashSet<>();
	
	public ArrowEffectGadget(GadgetManager manager, String name, String[] desc, int cost, Material mat, byte data, String...altNames) 
	{
		super(manager, GadgetType.ARROW_TRAIL, name, desc, cost, mat, data, 1, altNames);
	}
	
	@EventHandler
	public void arrowLaunch(ProjectileLaunchEvent event)
	{
		if (Manager.hideParticles())
			return;
		
		if (event.getEntity() instanceof Arrow)
		{
			if (event.getEntity().getShooter() != null)
			{
				if (getActive().contains(event.getEntity().getShooter()))
				{
					_arrows.add((Arrow)event.getEntity());
				}
			}
		}
	} 
	
	@EventHandler
	public void clean(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;
		
		for (Iterator<Arrow> arrowIterator = _arrows.iterator(); arrowIterator.hasNext();) 
		{
			Arrow arrow = arrowIterator.next();
			
			if (arrow.isDead() || !arrow.isValid() || arrow.isOnGround())
			{
				arrowIterator.remove();
			}
			else
			{
				doTrail(arrow);
			}
		}
	}
	
	@EventHandler
	public void arrowHit(ProjectileHitEvent event)
	{
		if (!_arrows.remove(event.getEntity()))
			return;
		
		if (Manager.hideParticles())
			return;
		
		doHitEffect((Arrow)event.getEntity());
	}
	
	public abstract void doTrail(Arrow arrow);
	public abstract void doHitEffect(Arrow arrow);
}