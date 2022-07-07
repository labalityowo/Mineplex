package nautilus.game.minekart.kart.control;

import mineplex.core.common.util.UtilAlg;
import nautilus.game.minekart.kart.Kart;
import nautilus.game.minekart.kart.Kart.DriftDirection;
import nautilus.game.minekart.kart.condition.ConditionData;
import nautilus.game.minekart.kart.condition.ConditionType;
import nautilus.game.minekart.kart.KartUtil;

import org.bukkit.Effect;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.util.Vector;

public class DriveDrift 
{
	public static void Hop(Kart kart, PlayerToggleSneakEvent event) 
	{
		if (kart == null)
			return;

		if (!kart.CanControl())
			return;

		if (event.getPlayer().isSneaking())
		{
			Boost(kart);
			kart.ClearDrift();
			return;
		}

		if (!KartUtil.IsGrounded(kart))
			return;

		//Save Drift Values
		kart.SetDrift();

		/*
		
		//Current Velocity
		Vector vel = kart.GetVelocity();
		double speed = vel.length();

		//Turn
		if (vel.length() > 0)
		{
			Vector turn = kart.GetDriver().getLocation().getDirection();
			turn.setY(0);
			UtilAlg.Normalize(turn);

			turn.subtract(UtilAlg.Normalize(new Vector(vel.getX(), 0, vel.getZ())));

			turn.multiply(0.1);

			vel.add(turn);
			
			UtilAlg.Normalize(vel);
			vel.multiply(speed);
		}

		//Hop
		Vector hop = kart.GetDriver().getLocation().getDirection();
		hop.setY(0);
		UtilAlg.Normalize(hop);
		hop.multiply(0.03);
		hop.setY(0.12);

		if (hop.length() > 0)
			vel.add(hop);
		*/
	}
	
	public static void Drift(Kart kart) 
	{
		if (!kart.CanControl())
			return;
		
		if (!kart.GetDriver().isSneaking())
		{
			kart.ClearDrift();
			return;
		}

		if (!KartUtil.IsGrounded(kart))
			return;

		if (kart.GetDrift() == DriftDirection.None)
			return;

		//Current Velocity
		Vector vel = kart.GetVelocity();
		double speed = kart.GetVelocity().length();

		if (speed < 0.5)
		{
			kart.ClearDrift();
			return;
		}
		
		//Drift
		vel.add(kart.GetDriftVector().multiply(0.030));

		//Turn
		Vector turn = kart.GetDriver().getLocation().getDirection();
		turn.setY(0);
		UtilAlg.Normalize(turn);

		turn.subtract(UtilAlg.Normalize(new Vector(vel.getX(), 0, vel.getZ())));

		turn.multiply(0.0015 * kart.GetKartType().GetHandling());

		vel.add(turn);

		//Reapply Speed
		UtilAlg.Normalize(vel);
		vel.multiply(speed);
		
		//Effect
		long driftTime = kart.GetDriftTime();
		int effectId = 42;
		if (driftTime > 4000)	effectId = 57;
		else if (driftTime > 2500)	effectId = 41;

		kart.GetDriver().getWorld().playEffect(kart.GetDriver().getLocation().add(0, -1, 0), Effect.STEP_SOUND, effectId);
	}
	
	public static void Boost(Kart kart) 
	{
		if (kart.GetDrift() == DriftDirection.None)
			return;

		if (kart.GetDriftTime() < 4000)
			return;
		
		kart.AddCondition(new ConditionData(ConditionType.Boost, 2000));

		//Effect
		kart.GetDriver().getWorld().playEffect(kart.GetDriver().getLocation(), Effect.STEP_SOUND, 57);
		kart.GetDriver().getWorld().playSound(kart.GetDriver().getLocation(), kart.GetKartType().GetSoundMain(), 2f, 1f);
	}
}
