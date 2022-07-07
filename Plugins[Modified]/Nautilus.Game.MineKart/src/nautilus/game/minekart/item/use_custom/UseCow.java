package nautilus.game.minekart.item.use_custom;

import org.bukkit.Location;

import nautilus.game.minekart.item.KartItemManager;
import nautilus.game.minekart.item.use_default.ItemUse;
import nautilus.game.minekart.kart.Kart;
import nautilus.game.minekart.kart.KartUtil;
import nautilus.game.minekart.track.ents.Cow;

public class UseCow extends ItemUse
{
	@Override
	public void Use(KartItemManager manager, Kart kart) 
	{
		kart.SetItemStored(null);

		//Mid
		kart.GetGP().GetTrack().GetCreatures().add(new Cow(kart.GetGP().GetTrack(), KartUtil.GetLook(kart), kart, kart.GetDriver().getLocation().getDirection()));
		
		Location left1 = KartUtil.GetLook(kart).add(KartUtil.GetSide(kart).multiply(1.5));
		kart.GetGP().GetTrack().GetCreatures().add(new Cow(kart.GetGP().GetTrack(), left1, kart, kart.GetDriver().getLocation().getDirection()));
		
		Location left2 = KartUtil.GetLook(kart).add(KartUtil.GetSide(kart).multiply(3));
		kart.GetGP().GetTrack().GetCreatures().add(new Cow(kart.GetGP().GetTrack(), left2, kart, kart.GetDriver().getLocation().getDirection()));
		
		Location left3 = KartUtil.GetLook(kart).add(KartUtil.GetSide(kart).multiply(4.5));
		kart.GetGP().GetTrack().GetCreatures().add(new Cow(kart.GetGP().GetTrack(), left3, kart, kart.GetDriver().getLocation().getDirection()));
		
		Location right1 = KartUtil.GetLook(kart).subtract(KartUtil.GetSide(kart).multiply(1.5));
		kart.GetGP().GetTrack().GetCreatures().add(new Cow(kart.GetGP().GetTrack(), right1, kart, kart.GetDriver().getLocation().getDirection()));
		
		Location right2 = KartUtil.GetLook(kart).subtract(KartUtil.GetSide(kart).multiply(3));
		kart.GetGP().GetTrack().GetCreatures().add(new Cow(kart.GetGP().GetTrack(), right2, kart, kart.GetDriver().getLocation().getDirection()));
		
		Location right3 = KartUtil.GetLook(kart).subtract(KartUtil.GetSide(kart).multiply(4.5));
		kart.GetGP().GetTrack().GetCreatures().add(new Cow(kart.GetGP().GetTrack(), right3, kart, kart.GetDriver().getLocation().getDirection()));
	}
}
