package nautilus.game.minekart.track.ents;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTime;
import nautilus.game.minekart.kart.Kart;
import nautilus.game.minekart.kart.KartState;
import nautilus.game.minekart.kart.crash.Crash_Explode;
import nautilus.game.minekart.track.Track;
import nautilus.game.minekart.track.TrackEntity;

public class Mole extends TrackEntity
{
	public Mole(Track track, Location loc) 
	{
		super(track, null, "Monty Mole", 5, 1, 1500, loc);
	}
	
	@Override
	public void CheckCollision(Kart kart) 
	{		
		if (kart.GetKartState() == KartState.Crash)
			return;
		
		if (UtilMath.offset(kart.GetDriver().getLocation(), GetLocation()) > GetCollideRange())
			return;
		
		if (GetLocation().getBlock().getType() != Material.RED_MUSHROOM)
			return;
		
		Collide(kart);
	}
	
	@Override
	public void Collide(Kart kart) 
	{
		if (!kart.IsInvulnerable(false))
		{
			UtilPlayer.message(kart.GetDriver(), 		F.main("MK", "You hit " + F.item(GetName()) + "."));

			//Crash
			new Crash_Explode(kart, 1f, true);
		}

		//Effect
		GetLocation().getWorld().playSound(GetLocation(), Sound.EXPLODE, 2f, 1.5f);
		
		//Remove Mole
		GetLocation().getBlock().setType(Material.AIR);
		SetSpawnTimer(System.currentTimeMillis());
	}
	
	@Override
	public boolean Update()
	{
		//Up
		if (GetLocation().getBlock().getType() != Material.RED_MUSHROOM)
		{
			if (Math.random() > 0.99 && UtilTime.elapsed(GetSpawnTimer(), GetSpawnRate()))
			{	
				GetLocation().getBlock().setType(Material.RED_MUSHROOM);
				SetSpawnTimer(System.currentTimeMillis());
				
				//Sound
				GetLocation().getWorld().playSound(GetLocation(), Sound.BAT_HURT, 0.4f, 1.5f);
				GetLocation().getWorld().playEffect(GetLocation(), Effect.STEP_SOUND, 3);
			}
		}
		//Down
		else 
		{
			if (UtilTime.elapsed(GetSpawnTimer(), 1500))
			{	
				GetLocation().getBlock().setType(Material.AIR);
				SetSpawnTimer(System.currentTimeMillis());
			}
		}
		
		return false;
	}
}
