package nautilus.game.minekart.item.use_default;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilEnt;
import nautilus.game.minekart.item.KartItemManager;
import nautilus.game.minekart.kart.Kart;
import nautilus.game.minekart.kart.condition.ConditionData;
import nautilus.game.minekart.kart.condition.ConditionType;
import nautilus.game.minekart.kart.crash.Crash_Spin;

public class UseLightning extends ItemUse
{
	@Override
	public void Use(KartItemManager manager, Kart kart) 
	{
		if (kart.HasCondition(ConditionType.Star) || kart.HasCondition(ConditionType.Ghost) || kart.HasCondition(ConditionType.Lightning))
			return;
		
		kart.SetItemStored(null);
		
		kart.GetGP().Announce(F.main("MK", F.elem(UtilEnt.getName(kart.GetDriver())) + " used " + F.item("Lightning") + "."));
		
		for (Kart other : kart.GetGP().GetKarts())
		{
			if (kart.equals(other))
				continue;
			
			if (other.HasCondition(ConditionType.Star) || other.HasCondition(ConditionType.Ghost))
				continue;
			
			new Crash_Spin(other, 0.8f);
			
			other.GetDriver().getWorld().strikeLightningEffect(other.GetDriver().getLocation());
			other.AddCondition(new ConditionData(ConditionType.Lightning, 10000));	
		}
	}
}