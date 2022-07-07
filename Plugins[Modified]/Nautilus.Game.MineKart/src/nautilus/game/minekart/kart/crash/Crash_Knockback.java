package nautilus.game.minekart.kart.crash;

import mineplex.core.common.util.UtilAlg;
import nautilus.game.minekart.kart.Kart;
import nautilus.game.minekart.kart.KartState;

import org.bukkit.Location;
import org.bukkit.util.Vector;

public class Crash_Knockback extends Crash
{

	public Crash_Knockback(Kart kart, Location other, double power) 
	{
		super(kart, new Vector(0,0,0), 1000, true, true);
		
		power = Math.min(power, 1.5);
		
		//Effect
		kart.GetDriver().getWorld().playSound(kart.GetDriver().getLocation(), kart.GetKartType().GetSoundCrash(), 2f, 1f);

		//Apply Knockback
		Vector knock = UtilAlg.getTrajectory(other, kart.GetDriver().getLocation());
		knock.multiply(0.6 * power);
		knock.add(new Vector(0, power * 0.8, 0));
		SetVelocity(knock);
		
		//Remove Karts Velocity
		kart.CrashStop();
		
		kart.SetKartState(KartState.Crash);
	}
}
