package nautilus.game.minekart.item.world_items_default;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Slime;
import org.bukkit.util.Vector;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilPlayer;
import nautilus.game.minekart.item.KartItemEntity;
import nautilus.game.minekart.item.KartItemManager;
import nautilus.game.minekart.item.control.Collision;
import nautilus.game.minekart.item.control.Movement;
import nautilus.game.minekart.kart.Kart;
import nautilus.game.minekart.kart.crash.Crash_Explode;

public class GreenShell extends KartItemEntity
{
	public GreenShell(KartItemManager manager, Kart kart, Location loc)
	{
		super(manager, kart, loc, Material.EMERALD_BLOCK, (byte)0);

		Vector vel = UtilAlg.Normalize(kart.GetDriver().getLocation().getDirection().setY(0));
		vel.multiply(1.4);
		vel.setY(-0.5);

		SetVelocity(vel);

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
				UtilPlayer.message(kart.GetDriver(), 		F.main("MK", "You hit yourself with " + F.item("Green Shell") + "."));
			}
			else
			{
				UtilPlayer.message(kart.GetDriver(), 		F.main("MK", F.elem(UtilEnt.getName(GetOwner().GetDriver())) + " hit you with " + F.item("Green Shell") + "."));
				UtilPlayer.message(GetOwner().GetDriver(), 	F.main("MK", "You hit " + F.elem(UtilEnt.getName(kart.GetDriver())) + " with " + F.item("Green Shell") + "."));
			}
			
			//Crash
			new Crash_Explode(kart, 1f, true);
		}

		//Effect
		GetEntity().getWorld().playSound(GetEntity().getLocation(), Sound.EXPLODE, 2f, 1.5f);
	}	

	@Override
	public void Spawn(Location loc)
	{
		Slime slime = (Slime) loc.getWorld().spawnEntity(loc.add(0, 0.5, 0), EntityType.SLIME);
		slime.setSize(1);
		SetEntity(slime);	
		SetFired();
	}

	public boolean TickUpdate()
	{
		if (GetHost() != null)
			return false;

		Movement.Move(this);

		if (Collision.CollideBlock(this))
			GetEntity().getWorld().playSound(GetEntity().getLocation(), Sound.CHICKEN_EGG_POP, 2f, 1f);

		return false;
	}
}
