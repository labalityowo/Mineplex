package nautilus.game.minekart.kart.crash;

import mineplex.core.common.util.UtilAlg;
import nautilus.game.minekart.kart.Kart;
import nautilus.game.minekart.kart.KartState;

import org.bukkit.Location;
import org.bukkit.util.Vector;

public class Crash_Spin extends Crash
{
	public Crash_Spin(Kart kart, Location other, double power) 
	{
		super(kart, new Vector(0,0,0), (long) (1500 * power), true, true);
		
		power = Math.min(power, 1.5);
		
		//Effect
		kart.GetDriver().getWorld().playSound(kart.GetDriver().getLocation(), kart.GetKartType().GetSoundCrash(), 2f, 1f);

		//Apply Knockback
		Vector knock = UtilAlg.getTrajectory(other, kart.GetDriver().getLocation());
		knock.setY(0);
		UtilAlg.Normalize(knock);
		knock.multiply(0.6 * power);
		SetVelocity(knock);
		
		//Remove Karts Velocity
		kart.CrashStop();
		
		kart.SetKartState(KartState.Crash);
	}
	
	public Crash_Spin(Kart kart, double power) 
	{
		super(kart, new Vector(0,0,0), (long) (2000 * power), true, true);
		
		power = Math.min(power, 1.5);
		
		//Effect
		kart.GetDriver().getWorld().playSound(kart.GetDriver().getLocation(), kart.GetKartType().GetSoundCrash(), 2f, 1f);

		//Apply Knockback
		SetVelocity(kart.GetVelocity());
		
		//Remove Karts Velocity
		kart.CrashStop();
		
		kart.SetKartState(KartState.Crash);
	}
}
