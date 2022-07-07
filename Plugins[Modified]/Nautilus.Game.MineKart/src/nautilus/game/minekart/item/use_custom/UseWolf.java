package nautilus.game.minekart.item.use_custom;

import nautilus.game.minekart.item.KartItemManager;
import nautilus.game.minekart.item.use_default.ItemUse;
import nautilus.game.minekart.kart.Kart;
import nautilus.game.minekart.kart.condition.ConditionData;
import nautilus.game.minekart.kart.condition.ConditionType;

public class UseWolf extends ItemUse
{
	@Override
	public void Use(KartItemManager manager, Kart kart) 
	{
		kart.SetItemStored(null);

		kart.AddCondition(new ConditionData(ConditionType.WolfHeart, 60000));
	}
}
