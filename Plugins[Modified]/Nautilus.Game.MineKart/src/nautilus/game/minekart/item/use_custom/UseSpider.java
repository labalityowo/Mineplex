package nautilus.game.minekart.item.use_custom;

import org.bukkit.Sound;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilEnt;
import nautilus.game.minekart.item.KartItemManager;
import nautilus.game.minekart.item.use_default.ItemUse;
import nautilus.game.minekart.kart.Kart;
import nautilus.game.minekart.track.ents.Spiderling;

public class UseSpider extends ItemUse
{
	@Override
	public void Use(KartItemManager manager, Kart kart) 
	{
		kart.SetItemStored(null);

		kart.GetGP().Announce(F.main("MK", F.elem(UtilEnt.getName(kart.GetDriver())) + " used " + F.item("Spiderlings") + "."));
		
		final Kart fKart = kart;
		
		int i = 0;
		for (Kart other : kart.GetGP().GetKarts())
		{
			if (other.equals(kart))
				continue;
			
			final Kart fOther = other;
			
			manager.GetPlugin().getServer().getScheduler().scheduleSyncDelayedTask(manager.GetPlugin(), new Runnable()
			{
				public void run()
				{
					//Spider
					fKart.GetGP().GetTrack().GetCreatures().add(
							new Spiderling(fKart.GetGP().GetTrack(), fKart.GetDriver().getLocation(), fKart, fOther));
					
					//Sound
					fKart.GetDriver().getWorld().playSound(fKart.GetDriver().getLocation(), Sound.SPIDER_IDLE, 2f, 2f);
				}
			}, i * 3);
			
			i++;
		}
	}
}
