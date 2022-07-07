package nautilus.game.pvp.worldevent.creature;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.util.Vector;

import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.core.updater.UpdateType;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilTime;
import nautilus.game.pvp.worldevent.EventBase;
import nautilus.game.pvp.worldevent.EventMobMinion;

public class Broodling extends EventMobMinion
{
	private LivingEntity _mount = null;
	private long _mountTime = 0;

	public Broodling(EventBase event, Location location, BroodMother host) 
	{
		super(event, location, "Broodling", true, 24, EntityType.CAVE_SPIDER, host);

		_mountTime = System.currentTimeMillis();
	}
	
	@Override
	public void StateChange(int newState) 
	{
		//SPIDERLINGS DONT CARE!!!!
	}
	
	@Override
	public void DamagerCustom(CustomDamageEvent event)
	{
		Mount(event);		
	}

	public void Mount(CustomDamageEvent event) 
	{
		if (_target == null)
			return;

		if (!(_target instanceof Player))

			if (!event.GetDamageeEntity().equals(_target))
				return;

		if (Math.random() > 0.50)
			return;

		if (UtilMath.offset(GetEntity(), _target) > 2)
			return;

		if (!UtilTime.elapsed(_mountTime, 8000))
			return;

		if (_target.getPassenger() != null && _target.getPassenger().getType() == EntityType.CAVE_SPIDER)
			return;

		//Condition Indicators
		Event.Manager.Condition().SetIndicatorVisibility(_target, false);

		//Action
		_target.eject();
		_target.setPassenger(GetEntity());

		_mount = _target;
		_mountTime = System.currentTimeMillis();
	}
	
	@EventHandler
	public void Dismount(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;

		if (_mount == null)
			return;

		if (!UtilTime.elapsed(_mountTime, 4000))
			return;

		GetEntity().leaveVehicle();
		_mount.eject();

		_mountTime = System.currentTimeMillis();

		Event.Manager.Condition().SetIndicatorVisibility(_mount, true);		
	}

	@Override
	public void Die()
	{
		Event.Manager.Blood().Effects(GetEntity().getEyeLocation(), 4, 0.5, 
				Sound.SPIDER_DEATH, 1f, 1f, Material.BONE, (byte)0, false);
		
		Loot();
		Remove();
	}

	@EventHandler
	public void Leap(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTER)
			return;

		if (_target == null || _target.isDead() || !_target.isValid())
			return;

		if (_mount != null)
			return;

		if (!Valid())
			return;

		//Leap
		if (	Math.random() > 0.75 &&
				UtilEnt.isGrounded(GetEntity()) &&  
				UtilMath.offset(GetEntity().getLocation(), _target.getEyeLocation()) > 2)
		{
			Vector vec = UtilAlg.getTrajectory(GetEntity().getLocation(), _target.getEyeLocation());
			UtilAction.velocity(GetEntity(), vec, Math.random()/2 + 0.5, false, 0, Math.random()/3, 0.3, true);
		}
	}

		
}
