package nautilus.game.minekart.item.world_items_default;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilPlayer;
import nautilus.game.minekart.item.KartItemEntity;
import nautilus.game.minekart.item.KartItemManager;
import nautilus.game.minekart.kart.Kart;
import nautilus.game.minekart.kart.crash.Crash_Spin;

public class Banana extends KartItemEntity
{
	public Banana(KartItemManager manager, Kart kart, Location loc)
	{
		super(manager, kart, loc, Material.INK_SACK, (byte)11);

		SetRadius(1.5);
	}

	@Override
	public void CollideHandle(Kart kart)
	{
		if (!kart.IsInvulnerable(true))
		{
			//Inform
			if (kart.equals(GetOwner()))
			{
				UtilPlayer.message(kart.GetDriver(), 		F.main("MK", "You hit yourself with " + F.item("Banana") + "."));
			}
			else
			{
				UtilPlayer.message(kart.GetDriver(), 		F.main("MK", F.elem(UtilEnt.getName(GetOwner().GetDriver())) + " hit you with " + F.item("Banana") + "."));
				UtilPlayer.message(GetOwner().GetDriver(), 	F.main("MK", "You hit " + F.elem(UtilEnt.getName(kart.GetDriver())) + " with " + F.item("Banana") + "."));
			}
			
			//Crash
			if (kart.GetVelocity().length() == 0)
				kart.SetVelocity(kart.GetDriver().getLocation().getDirection().setY(0));
			
			kart.SetVelocity(UtilAlg.Normalize(kart.GetVelocityClone().setY(0)).multiply(0.6));
			new Crash_Spin(kart, 0.6f);
			kart.SetStability(0);
		}

		//Effect
		GetEntity().getWorld().playSound(GetEntity().getLocation(), Sound.BAT_HURT, 1f, 1f);
	}
}
