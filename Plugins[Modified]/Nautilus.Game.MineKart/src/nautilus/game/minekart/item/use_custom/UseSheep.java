package nautilus.game.minekart.item.use_custom;

import org.bukkit.Sound;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilEnt;
import nautilus.game.minekart.item.KartItemManager;
import nautilus.game.minekart.item.use_default.ItemUse;
import nautilus.game.minekart.kart.Kart;
import nautilus.game.minekart.track.ents.Sheepile;

public class UseSheep extends ItemUse
{
	@Override
	public void Use(KartItemManager manager, Kart kart) 
	{
		kart.SetItemStored(null);

		kart.GetGP().Announce(F.main("MK", F.elem(UtilEnt.getName(kart.GetDriver())) + " used " + F.item("Super Sheep") + "."));
		
		//Spider
		kart.GetGP().GetTrack().GetCreatures().add(
				new Sheepile(kart.GetGP().GetTrack(), kart.GetDriver().getLocation(), kart));
		
		//Sound
		kart.GetDriver().getWorld().playSound(kart.GetDriver().getLocation(), Sound.SHEEP_IDLE, 2f, 2f);
		kart.GetDriver().getWorld().playSound(kart.GetDriver().getLocation(), Sound.SHEEP_IDLE, 2f, 2f);	
	}
}
