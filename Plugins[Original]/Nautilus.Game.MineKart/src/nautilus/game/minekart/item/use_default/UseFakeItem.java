package nautilus.game.minekart.item.use_default;

import nautilus.game.minekart.item.KartItemManager;
import nautilus.game.minekart.item.world_items_default.FakeItem;
import nautilus.game.minekart.kart.Kart;
import nautilus.game.minekart.kart.KartUtil;

public class UseFakeItem extends ItemUse
{
	@Override
	public void Use(KartItemManager manager, Kart kart) 
	{
		kart.SetItemStored(null);

		new FakeItem(manager, kart, KartUtil.GetBehind(kart));		
	}
}
