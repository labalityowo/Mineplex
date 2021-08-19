package nautilus.game.arcade.game.games.halloween.creatures;

import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilTime;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.game.games.halloween.Halloween;

import org.bukkit.Location;
import org.bukkit.entity.Giant;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.util.Vector;

public class MobGiant extends CreatureBase<Giant>
{
	private Location _tpLoc; 
	
	public MobGiant(Halloween game, Location loc) 
	{
		super(game, null, Giant.class, loc);
	}

	@Override
	public void SpawnCustom(Giant ent) 
	{
		_tpLoc = ent.getLocation();
		
		ent.setMaxHealth(600);
		ent.setHealth(600);
		
		ent.setCustomName("Giant");
	}
	
	@Override
	public void Damage(CustomDamageEvent event) 
	{
		if (event.GetDamageeEntity().equals(GetEntity()))
			event.SetKnockback(false);
		
		if (event.GetCause() == DamageCause.SUFFOCATION)
			event.SetCancelled("Suffocation Cancel");
	}

	@Override
	public void Target(EntityTargetEvent event)
	{
		
	}
	
	@Override
	public void Update(UpdateEvent event) 
	{
		if (event.getType() == UpdateType.TICK)
			Move();

		if (event.getType() == UpdateType.SEC)
			Destroy();

	}

	private void Destroy() 
	{
		Host.Manager.GetExplosion().BlockExplosion(UtilBlock.getInRadius(GetEntity().getLocation().add(0, 8, 0), 6d).keySet(), GetEntity().getLocation().add(0, 8, 0), false);
		Host.Manager.GetExplosion().BlockExplosion(UtilBlock.getInRadius(GetEntity().getLocation().add(0, 2, 0), 5d).keySet(), GetEntity().getLocation(), true);
		Host.Manager.GetExplosion().BlockExplosion(UtilBlock.getInRadius(GetEntity().getLocation().add(0, 0, 0), 5d).keySet(), GetEntity().getLocation(), true);
	}

	private void Move() 
	{
		//New Target via Distance
		if (GetTarget() == null || 
			UtilMath.offset2d(GetEntity().getLocation(), GetTarget()) < 0.5 ||
			UtilTime.elapsed(GetTargetTime(), 20000))
		{
			SetTarget(GetPlayerTarget());
			return;
		}
		
		if (_tpLoc == null)
			_tpLoc = GetEntity().getLocation();
		
		Vector dir = UtilAlg.getTrajectory2d(GetEntity().getLocation(), GetTarget());
		
		_tpLoc.setPitch(UtilAlg.GetPitch(dir));
		_tpLoc.setYaw(UtilAlg.GetYaw(dir));
		
		double speed = Math.min(0.35, 0.1 + (GetEntity().getTicksLived() / 12000d));
		

		_tpLoc.add(dir.multiply(speed));
				
		//Move
		GetEntity().teleport(_tpLoc);
	}
}
