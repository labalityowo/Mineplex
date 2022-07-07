package nautilus.game.minekart.item.use_active;

import java.util.List;

import org.bukkit.Location;

import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilMath;
import nautilus.game.minekart.item.KartItemActive;
import nautilus.game.minekart.item.KartItemEntity;
import nautilus.game.minekart.item.KartItemManager;
import nautilus.game.minekart.item.world_items_default.RedShell;
import nautilus.game.minekart.kart.Kart;
import nautilus.game.minekart.kart.KartUtil;

public class ActiveShells extends KartItemActive
{
	public ActiveShells(KartItemManager manager, Kart kart, List<KartItemEntity> ents) 
	{
		super(manager, kart, ActiveType.Orbit, ents);
	}

	@Override
	public boolean Use() 
	{
		if (GetEntities().isEmpty())
			return true;
		
		//Find Closest to firing trajectory
		Location loc = KartUtil.GetLook(GetKart());
		
		KartItemEntity closest = null;
		double closestDist = 10;
		
		for (KartItemEntity item : GetEntities())
		{
			double dist = UtilMath.offset(item.GetEntity().getLocation(), loc);
			
			if (closest == null)
			{
				closest = item;
				closestDist = dist;
				continue;
			}
	
			if (dist < closestDist)
			{
				closest = item;
				closestDist = dist;
			}				
		}
		
		//Fire Shell
		closest.SetHost(null);
		closest.SetVelocity(UtilAlg.Normalize(GetKart().GetDriver().getLocation().getDirection().setY(0)));
		GetEntities().remove(closest);
		
		//Counter Collision with driver
		closest.SetFired();
		
		//Red Shell Chase 
		if (closest instanceof RedShell)
		{
			Kart target = null;
			
			for (Kart other : GetKart().GetGP().GetKarts())
			{
				if (other.GetLapPlace() + 1 == GetKart().GetLapPlace())
					target = other;
			}
			
			closest.SetTarget(target);
		}
		
		return GetEntities().isEmpty();
	}
}
