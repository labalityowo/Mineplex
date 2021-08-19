package nautilus.game.minekart.kart.control;

import mineplex.core.common.util.UtilAlg;
import nautilus.game.minekart.kart.Kart;
import nautilus.game.minekart.kart.KartUtil;
import nautilus.game.minekart.kart.condition.ConditionType;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class Drive 
{
	public static void TopSpeed(Kart kart) 
	{
		if (!kart.CanControl())
			return;

		if (!KartUtil.IsGrounded(kart))
			return;
		
		if (kart.HasCondition(ConditionType.Boost))
			return;

		double topSpeed = kart.GetKartType().GetTopSpeed();
		
		if (kart.HasCondition(ConditionType.Star))
			topSpeed *= 1.2;
		
		if (kart.GetSpeed() > topSpeed)
		{
			Vector vel = kart.GetVelocityClone();
			vel.setY(0);
			vel.normalize();
			vel.multiply(topSpeed);

			kart.SetVelocity(vel);
		}
	}

	public static void Accelerate(Kart kart)
	{
		if (!kart.CanControl())
			return;

		if (!KartUtil.IsGrounded(kart))
			return;
		
		if (!kart.GetDriver().isBlocking())
			return;
		
		ItemStack item = kart.GetDriver().getItemInHand();
		if (item == null || item.getType() != Material.STONE_SWORD)
			return;
		
		//Current Velocity
		Vector vel = kart.GetVelocity();

		//Initial Velocity
		if (vel.length() == 0)
		{
			vel = kart.GetDriver().getLocation().getDirection().setY(0);

			//Looking directly Up/Down
			if (vel.length() == 0)
				return;

			vel.normalize();
			vel.multiply(0.001);

			kart.SetVelocity(vel);
		}

		//Kart Acceleration
		Vector acc = new Vector(vel.getX(), 0, vel.getZ());
		UtilAlg.Normalize(acc);
		
		double acceleration = kart.GetKartType().GetAcceleration();
		if (kart.HasCondition(ConditionType.Star))
			acceleration *= 1.2;
		
		acc.multiply(0.001 * acceleration);
		vel.add(acc);
}

	public static void Brake(Kart kart) 
	{
		if (!kart.CanControl())
			return;

		if (!KartUtil.IsGrounded(kart))
			return;

		if (!kart.GetDriver().isBlocking())
			return;

		ItemStack item = kart.GetDriver().getItemInHand();
		if (item == null || item.getType() != Material.WOOD_SWORD)
			return;

		//Current Velocity
		Vector vel = kart.GetVelocity();

		//Drag %
		vel.multiply(0.95);

		if (vel.length() < 0.05)
			vel.multiply(0);
	}

	public static void Turn(Kart kart) 
	{
		if (!kart.CanControl())
			return;

		if (!KartUtil.IsGrounded(kart))
			return;

		//Current Velocity
		Vector vel = kart.GetVelocity();

		if (vel.length() <= 0)
			return;

		double speed = vel.length();

		double handling = kart.GetKartType().GetHandling();
		if (kart.HasCondition(ConditionType.Star))
			handling *= 1.2;
		
		//Turn
		Vector turn = kart.GetDriver().getLocation().getDirection();
		turn.setY(0);
		UtilAlg.Normalize(turn);

		turn.subtract(UtilAlg.Normalize(new Vector(vel.getX(), 0, vel.getZ())));

		turn.multiply(0.003 * handling);

		vel.add(turn);

		//Reapply Speed
		speed = (speed + (vel.length()*3)) / 4;
		UtilAlg.Normalize(vel);
		vel.multiply(speed);
	}

	public static void Move(Kart kart) 
	{
		//Current Velocity
		Vector vel = kart.GetVelocity();
		
		kart.GetDriver().setVelocity(vel);

		//Display Velocity as Exp
		kart.GetDriver().setExp(Math.min(0.999f, ((float)kart.GetSpeed()/(float)kart.GetKartType().GetTopSpeed())));
		kart.GetDriver().setLevel((int) (kart.GetSpeed() * 100));
	}
}
