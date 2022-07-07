package nautilus.game.minekart.item.use_custom;

import org.bukkit.Sound;
import org.bukkit.entity.Egg;
import org.bukkit.util.Vector;

import nautilus.game.minekart.item.KartItemManager;
import nautilus.game.minekart.item.use_default.ItemUse;
import nautilus.game.minekart.kart.Kart;

public class UseChicken extends ItemUse
{
	@Override
	public void Use(KartItemManager manager, Kart kart) 
	{
		if (kart.GetDriver().getInventory().getItem(3) == null)
		{
			kart.SetItemStored(null);
		}
		else if (kart.GetDriver().getInventory().getItem(3).getAmount() > 1)
		{
			kart.GetDriver().getInventory().getItem(3).setAmount(kart.GetDriver().getInventory().getItem(3).getAmount() - 1);
		}
		else
		{
			kart.SetItemStored(null);
		}
		
		Egg egg = kart.GetDriver().launchProjectile(Egg.class);
		
		Vector vel = kart.GetDriver().getLocation().getDirection();
		vel.setY(0.1);
		vel.multiply(2);
		egg.setVelocity(vel);	
		
		kart.GetDriver().getWorld().playSound(kart.GetDriver().getLocation(), Sound.CHICKEN_EGG_POP, 2f, 1f);
	}
}
