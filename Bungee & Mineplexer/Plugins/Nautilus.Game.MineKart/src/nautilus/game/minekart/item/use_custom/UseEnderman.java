package nautilus.game.minekart.item.use_custom;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import mineplex.core.common.util.UtilBlock;
import nautilus.game.minekart.item.KartItemManager;
import nautilus.game.minekart.item.use_default.ItemUse;
import nautilus.game.minekart.kart.Kart;

public class UseEnderman extends ItemUse
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

		Player player = kart.GetDriver();

		//Trail
		Block lastEffect = player.getLocation().getBlock();

		double maxRange = 20;
		double curRange = 0;
		while (curRange <= maxRange)
		{
			Vector look = player.getLocation().getDirection();
			look.setY(0);
			look.normalize();
			look.multiply(curRange);

			Location newTarget = player.getLocation().add(0, 0.1, 0).add(look);

			if (UtilBlock.solid(newTarget.getBlock()))
				break;

			//Progress Forwards
			curRange += 0.2;

			//Trail
			if (!lastEffect.equals(newTarget.getBlock()))
				lastEffect.getWorld().playEffect(lastEffect.getLocation(), Effect.STEP_SOUND, 49);

			lastEffect = newTarget.getBlock();
		}

		//Modify Range
		curRange -= 0.4;
		if (curRange < 0)
			curRange = 0;

		//Destination
		Vector look = player.getLocation().getDirection();
		look.setY(0);
		look.normalize();
		look.multiply(curRange);

		Location loc = player.getLocation().add(look).add(new Vector(0, 0.4, 0));

		//Sound
		kart.GetDriver().getWorld().playSound(kart.GetDriver().getLocation(), Sound.ZOMBIE_UNFECT, 2f, 2f);

		Entity item = player.getPassenger();
		if (item != null)	item.leaveVehicle();
		player.eject();

		//Action
		if (curRange > 0)
		{
			player.leaveVehicle();
			player.teleport(loc);
		}

		//Re-Attach Item
		if (item != null)
		{
			item.teleport(loc.add(0, 1.5, 0));
			player.setPassenger(item);
		}

		//Adjust Velocity
		double length = kart.GetVelocity().length();
		Vector vel = player.getLocation().getDirection();
		vel.setY(0);
		vel.normalize();
		vel.multiply(length);
		kart.SetVelocity(vel);
	}
}
