package nautilus.game.minekart.item.world_items_default;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Entity;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilPlayer;
import nautilus.game.minekart.item.KartItemEntity;
import nautilus.game.minekart.item.KartItemManager;
import nautilus.game.minekart.kart.Kart;
import nautilus.game.minekart.kart.crash.Crash_Explode;

public class FakeItem extends KartItemEntity
{
	public FakeItem(KartItemManager manager, Kart kart, Location loc)
	{
		super(manager, kart, loc, Material.TRAPPED_CHEST, (byte)0);

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
				UtilPlayer.message(kart.GetDriver(), 		F.main("MK", "You hit yourself with " + F.item("Fake Item") + "."));
			}
			else
			{
				UtilPlayer.message(kart.GetDriver(), 		F.main("MK", F.elem(UtilEnt.getName(GetOwner().GetDriver())) + " hit you with " + F.item("Fake Item") + "."));
				UtilPlayer.message(GetOwner().GetDriver(), 	F.main("MK", "You hit " + F.elem(UtilEnt.getName(kart.GetDriver())) + " with " + F.item("Fake Item") + "."));
			}
			
			//Crash
			kart.CrashStop();
			new Crash_Explode(kart, 1.2f, false);
		}

		//Effect
		GetEntity().getWorld().playSound(GetEntity().getLocation(), Sound.EXPLODE, 2f, 0.2f);
	}

	@Override
	public void Spawn(Location loc)
	{
		Entity ent = loc.getWorld().spawn(loc, EnderCrystal.class);
		SetEntity(ent);	
		SetFired();
	}
}
