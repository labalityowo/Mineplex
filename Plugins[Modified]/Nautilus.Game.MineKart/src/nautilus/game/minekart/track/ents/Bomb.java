package nautilus.game.minekart.track.ents;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import nautilus.game.minekart.kart.Kart;
import nautilus.game.minekart.kart.crash.Crash_Explode;
import nautilus.game.minekart.track.TrackEntity;
import nautilus.game.minekart.track.Track;

public class Bomb extends TrackEntity
{
	public Bomb(Track track, Location loc) 
	{
		super(track, EntityType.CREEPER, "Bomb", 5, 1, 30000, loc);
	}

	@Override
	public void Collide(Kart kart) 
	{
		this.SetSpawnTimer(System.currentTimeMillis());
		this.GetEntity().remove();
		
		if (!kart.IsInvulnerable(true))
		{

			UtilPlayer.message(kart.GetDriver(), 		F.main("MK", "You hit " + F.item(GetName()) + "."));

			//Crash
			kart.CrashStop();
			new Crash_Explode(kart, 1.2f, false);
		}

		//Effect
		GetEntity().getWorld().playSound(GetEntity().getLocation(), Sound.EXPLODE, 2f, 0.2f);
	}
}
