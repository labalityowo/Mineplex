package nautilus.game.minekart.item.use_active;

import java.util.List;

import nautilus.game.minekart.item.KartItemActive;
import nautilus.game.minekart.item.KartItemEntity;
import nautilus.game.minekart.item.KartItemManager;
import nautilus.game.minekart.kart.Kart;

public class ActiveBananas extends KartItemActive
{
	public ActiveBananas(KartItemManager manager, Kart kart, List<KartItemEntity> ents) 
	{
		super(manager, kart, ActiveType.Trail, ents);
	}

	@Override
	public boolean Use() 
	{
		if (GetEntities().isEmpty())
			return true;
		
		//Find Closest to firing trajectory
		KartItemEntity back = GetEntities().get(GetEntities().size() - 1);

		//Fire Shell
		back.SetHost(null);
		back.SetVelocity(null);
		GetEntities().remove(back);
		
		//Counter Collision with driver
		back.SetFired();
		
		return GetEntities().isEmpty();
	}
}
