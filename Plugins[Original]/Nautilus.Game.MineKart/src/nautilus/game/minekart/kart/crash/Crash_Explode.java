package nautilus.game.minekart.kart.crash;

import nautilus.game.minekart.kart.Kart;
import nautilus.game.minekart.kart.KartState;

import org.bukkit.util.Vector;

public class Crash_Explode extends Crash
{
	public Crash_Explode(Kart kart, double power, boolean resetVelocity) 
	{
		super(kart, new Vector(0,0,0), (long) (1400 * power), true, true);
		
		//Effect
		kart.GetDriver().getWorld().playSound(kart.GetDriver().getLocation(), kart.GetKartType().GetSoundCrash(), 2f, 1f);

		//Apply Upwards
		SetVelocity(kart.GetVelocityClone().add(new Vector(0, power * 1, 0)));
		
		//Remove Karts Velocity
		if (resetVelocity)
			kart.CrashStop();
		
		kart.SetKartState(KartState.Crash);
	}
}
