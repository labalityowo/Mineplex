package nautilus.game.arcade.game.games.christmas.content;

import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilServer;
import mineplex.core.recharge.Recharge;
import nautilus.game.arcade.game.games.christmas.parts.Part4;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Giant;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.util.Vector;

public class CaveGiant 
{
	private Part4 Host;
	
	private Giant _ent;
	private Location _target;
	private Location _tpLoc; 
	public CaveGiant(Part4 host, Location loc)
	{
		Host = host;

		Host.Host.CreatureAllowOverride = true;
		_ent = loc.getWorld().spawn(loc, Giant.class);
		Host.Host.CreatureAllowOverride = false;
		UtilEnt.vegetate(_ent);
		
		_ent.setMaxHealth(300);
		_ent.setHealth(300);

		_tpLoc = _ent.getLocation();
		
		for (Player player : UtilServer.getPlayers())
			player.playSound(_ent.getLocation(), Sound.ZOMBIE_PIG_ANGRY, 10f, 0.5f);
	}

	public boolean IsDead() 
	{
		return (_ent != null && !_ent.isValid());
	}
	
	public void SetTarget(Location loc)
	{
		_target = loc;
	}
	
	public Location GetTarget()
	{
		return _target;
	}

	public Giant GetEntity() 
	{
		return _ent;
	}
	
	public void MoveUpdate()
	{
		if (IsDead())
			return;
		
		Destroy();
		
		SetTarget(Host.Host.GetSleigh().GetLocation());
		
		//Move
		Vector dir = UtilAlg.getTrajectory2d(GetEntity().getLocation(), GetTarget());
		
		_tpLoc.setPitch(UtilAlg.GetPitch(dir));
		_tpLoc.setYaw(UtilAlg.GetYaw(dir));
		
		_tpLoc.add(dir.multiply(0.075));
				
		GetEntity().teleport(_tpLoc);
		
		//Attack
		for (Player player : Host.Host.GetPlayers(true))
		{
			if (UtilMath.offset(player, _ent) > 5)
				continue;
			
			if (!Recharge.Instance.usable(player, "Giant Damage"))
				continue;
			
			//Damage Event
			Host.Host.Manager.GetDamage().NewDamageEvent(player, _ent, null, 
					DamageCause.ENTITY_ATTACK, 6, true, false, false,
					UtilEnt.getName(_ent), null);
			
			Recharge.Instance.useForce(player, "Giant Damage", 1000);
		}
		
		if (UtilMath.offset(_ent.getLocation(), Host.Host.GetSleigh().GetLocation()) < 8)
		{
			Host.Host.End();
		}
	}
	
	private void Destroy() 
	{
		Host.Host.Manager.GetExplosion().BlockExplosion(UtilBlock.getInRadius(GetEntity().getLocation().add(0, 8, 0), 6d).keySet(), GetEntity().getLocation().add(0, 8, 0), false);
		Host.Host.Manager.GetExplosion().BlockExplosion(UtilBlock.getInRadius(GetEntity().getLocation().add(0, 2, 0), 5d).keySet(), GetEntity().getLocation(), true);
		Host.Host.Manager.GetExplosion().BlockExplosion(UtilBlock.getInRadius(GetEntity().getLocation().add(0, 0, 0), 5d).keySet(), GetEntity().getLocation(), true);
	}
}
