package nautilus.game.minekart.track.ents;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilPlayer;
import nautilus.game.minekart.kart.Kart;
import nautilus.game.minekart.kart.KartState;
import nautilus.game.minekart.kart.crash.Crash_Spin;
import nautilus.game.minekart.track.Track;
import nautilus.game.minekart.track.TrackEntity;

public class Silverfish extends TrackEntity
{
	public Silverfish(Track track, Location loc) 
	{
		super(track, EntityType.SILVERFISH, "Silverfish", 3, 1, 30000, loc);
	}

	@Override
	public void Collide(Kart kart) 
	{
		if (kart.GetKartState() == KartState.Crash)
			return;
		
		if (!kart.IsInvulnerable(false))
		{

			UtilPlayer.message(kart.GetDriver(), 		F.main("MK", "You hit " + F.item(GetName()) + "."));

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
