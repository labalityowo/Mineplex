package nautilus.game.minekart.item.use_default;

import java.util.ArrayList;

import nautilus.game.minekart.item.KartItemEntity;
import nautilus.game.minekart.item.KartItemManager;
import nautilus.game.minekart.item.use_active.ActiveShells;
import nautilus.game.minekart.item.world_items_default.GreenShell;
import nautilus.game.minekart.kart.Kart;
import nautilus.game.minekart.kart.KartUtil;

public class UseTripleGreenShell extends ItemUse
{
	@Override
	public void Use(KartItemManager manager, Kart kart) 
	{
		kart.SetItemStored(null);
		
		ArrayList<KartItemEntity> ents = new ArrayList<KartItemEntity>();
		
		ents.add(new GreenShell(manager, kart, KartUtil.GetLook(kart)));
		ents.add(new GreenShell(manager, kart, KartUtil.GetLook(kart)));
		ents.add(new GreenShell(manager, kart, KartUtil.GetLook(kart)));	
		
		new ActiveShells(manager, kart, ents);	
	}
}
