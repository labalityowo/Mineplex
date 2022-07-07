package nautilus.game.minekart.kart.crash;

import mineplex.core.common.util.UtilAlg;
import nautilus.game.minekart.kart.Kart;
import nautilus.game.minekart.kart.KartState;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.util.Vector;

public class Crash_Bump extends Crash
{
	public Crash_Bump(Kart kart, Location other, double power) 
	{
		super(kart, new Vector(0,0,0), (long) (800 * power), true, false);
		
		//Effect
		kart.GetDriver().getWorld().playSound(kart.GetDriver().getLocation(), Sound.IRONGOLEM_HIT, (float)power, 1f);

		//Apply Knockback
		Vector knock = UtilAlg.getTrajectory(other, kart.GetDriver().getLocation());
		knock.multiply(0.4 * power);
		knock.add(new Vector(0, 0.4 * power, 0));
		SetVelocity(knock);
		
		double powerTrim = Math.min(power, 1) / 2;
		
		//Lose some velocity
		kart.GetVelocity().multiply(1 - powerTrim);
		
		kart.SetKartState(KartState.Crash);
	}
}
