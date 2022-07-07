package nautilus.game.minekart.item.use_default;

import nautilus.game.minekart.item.KartItemManager;
import nautilus.game.minekart.kart.Kart;
import nautilus.game.minekart.kart.condition.ConditionData;
import nautilus.game.minekart.kart.condition.ConditionType;

public class UseStar extends ItemUse
{
	@Override
	public void Use(KartItemManager manager, Kart kart) 
	{
		if (kart.HasCondition(ConditionType.Star) || kart.HasCondition(ConditionType.Ghost) || kart.HasCondition(ConditionType.Lightning))
			return;
		
		kart.SetItemStored(null);
		
		kart.AddCondition(new ConditionData(ConditionType.Star, 10000));
	}
}
