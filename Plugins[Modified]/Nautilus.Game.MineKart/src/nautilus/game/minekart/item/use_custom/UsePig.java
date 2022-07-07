package nautilus.game.minekart.item.use_custom;

import org.bukkit.Sound;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilEnt;
import nautilus.game.minekart.item.KartItemManager;
import nautilus.game.minekart.item.use_default.ItemUse;
import nautilus.game.minekart.kart.Kart;
import nautilus.game.minekart.kart.condition.ConditionType;

public class UsePig extends ItemUse
{
	@Override
	public void Use(KartItemManager manager, Kart kart) 
	{
		kart.SetItemStored(null);
		
		kart.GetGP().Announce(F.main("MK", F.elem(UtilEnt.getName(kart.GetDriver())) + " used " + F.item("Pig Stink") + "."));
		
		for (Kart other : manager.KartManager.GetKarts().values())
		{
			if (other.equals(kart))
				continue;
			
			if (other.HasCondition(ConditionType.Star) || other.HasCondition(ConditionType.Ghost))
				continue;
			
			PotionEffect effect = new PotionEffect(PotionEffectType.CONFUSION, 400, 0, false);
			effect.apply(other.GetDriver());
			
			//Sound
			other.GetDriver().getWorld().playSound(other.GetDriver().getLocation(), Sound.PIG_IDLE, 2f, 0.5f);
		}	
		
		//Sound
		kart.GetDriver().getWorld().playSound(kart.GetDriver().getLocation(), Sound.PIG_IDLE, 2f, 0.5f);
		kart.GetDriver().getWorld().playSound(kart.GetDriver().getLocation(), Sound.PIG_IDLE, 2f, 0.5f);
	}
}
