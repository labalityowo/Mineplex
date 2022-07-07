package nautilus.game.minekart.kart.control;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import nautilus.game.minekart.kart.Kart;
import nautilus.game.minekart.kart.KartState;
import nautilus.game.minekart.kart.KartUtil;
import nautilus.game.minekart.kart.condition.ConditionType;
import nautilus.game.minekart.kart.crash.Crash_Bump;
import nautilus.game.minekart.kart.crash.Crash_Knockback;
import nautilus.game.minekart.kart.crash.Crash_Spin;

import org.bukkit.Effect;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

public class Collision 
{
	public static void CollideBlock(Kart kart) 
	{
		//Current Velocity
		Vector vel = kart.GetVelocity();
		if (kart.GetCrash() != null)
			if (kart.GetCrash().GetVelocity() != null)
				vel = kart.GetCrash().GetVelocity();

		Block block;

		double dist = 0.5;
		
		boolean done = false;
		
		block = kart.GetDriver().getLocation().add(dist, 0, 0).getBlock();
		if (vel.getX() > 0 && UtilBlock.solid(block)) 		{CollideBlock(kart, block, vel, true); done = true;}

		block = kart.GetDriver().getLocation().add(-dist, 0, 0).getBlock();
		if (vel.getX() < 0 && UtilBlock.solid(block))		{CollideBlock(kart, block, vel, true); done = true;}

		block = kart.GetDriver().getLocation().add(0, 0, dist).getBlock();
		if (vel.getZ() > 0 && UtilBlock.solid(block))		{CollideBlock(kart, block, vel, false); done = true;}

		block = kart.GetDriver().getLocation().add(0, 0, -dist).getBlock();
		if (vel.getZ() < 0 && UtilBlock.solid(block))		{CollideBlock(kart, block, vel, false); done = true;}
		
		if (done)
			return;
		
		block = kart.GetDriver().getLocation().add(dist, 1, 0).getBlock();
		if (vel.getX() > 0 && UtilBlock.solid(block)) 		CollideBlock(kart, block, vel, true); 

		block = kart.GetDriver().getLocation().add(-dist, 1, 0).getBlock();
		if (vel.getX() < 0 && UtilBlock.solid(block))		CollideBlock(kart, block, vel, true); 

		block = kart.GetDriver().getLocation().add(0, 1, dist).getBlock();
		if (vel.getZ() > 0 && UtilBlock.solid(block))		CollideBlock(kart, block, vel, false); 

		block = kart.GetDriver().getLocation().add(0, 1, -dist).getBlock();
		if (vel.getZ() < 0 && UtilBlock.solid(block))		CollideBlock(kart, block, vel, false); 
	}

	public static void CollideBlock(Kart kart, Block block, Vector vel, boolean X) 
	{
		//Get Collision Power
		double 	power = vel.getX();
		if (!X)	power = vel.getZ();

		power = Math.abs(power);

		//Set Velocity
		if (X)	vel.setX(0);
		else 	vel.setZ(0);

		if (power < 0.4)
			return;
		
		//Effects
		block.getWorld().playSound(kart.GetDriver().getLocation(), Sound.IRONGOLEM_WALK, (float)power * 3, 1f);
		block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, block.getTypeId());
		
		//Effect Stability
		if (KartUtil.Stability(kart, (int) (power * 20)))
		{
			new Crash_Knockback(kart, block.getLocation().add(0.5, 0.5, 0.5), power);
		}
		else
		{
			new Crash_Bump(kart, block.getLocation().add(0.5, 0.5, 0.5), power);
		}		
	}
	
	public static void CollidePlayer(Kart kart)
	{
		if (kart.HasCondition(ConditionType.Ghost) || kart.HasCondition(ConditionType.Star))
			return;
		
		for (Kart other : kart.GetGP().GetKarts())
		{		
			if (other.equals(kart))
				continue;
			
			if (other.HasCondition(ConditionType.Ghost) || other.HasCondition(ConditionType.Star))
				continue;
			
			if (kart.GetKartState() != KartState.Drive && other.GetKartState() != KartState.Drive)
				continue;
			
			if (!kart.GetDriver().getWorld().equals(other.GetDriver().getWorld()))
				continue;
			
			if (UtilMath.offset(kart.GetDriver(), other.GetDriver()) > 1)
				continue;
					
			double collisionVel = 0;
			
			//Collisions
			{
				//X-Pos
				if (kart.GetVelocity().getX() > 0 && other.GetDriver().getLocation().getX() > kart.GetDriver().getLocation().getX())
				{
					double vel = kart.GetVelocity().getX() - other.GetVelocity().getX();
					
					if (vel > 0)
						collisionVel += vel;
				}
				//X-Neg
				else if (kart.GetVelocity().getX() < 0 && other.GetDriver().getLocation().getX() < kart.GetDriver().getLocation().getX())
				{
					double vel = kart.GetVelocity().getX() - other.GetVelocity().getX();
					
					if (vel < 0)
						collisionVel -= vel;
				}
				
				//Z-Pos
				if (kart.GetVelocity().getZ() > 0 && other.GetDriver().getLocation().getZ() > kart.GetDriver().getLocation().getZ())
				{
					double vel = kart.GetVelocity().getZ() - other.GetVelocity().getZ();
				
					if (vel > 0)
						collisionVel += vel;
				}
				//Z-Neg
				else if (kart.GetVelocity().getZ() < 0 && other.GetDriver().getLocation().getZ() < kart.GetDriver().getLocation().getZ())
				{
					double vel = kart.GetVelocity().getZ() - other.GetVelocity().getZ();

					if (vel < 0)
						collisionVel -= vel;
				}
			}

			if (collisionVel <= 0)
				return;
			
			collisionVel *= 2;
			
			//Effects
			kart.GetDriver().getWorld().playSound(kart.GetDriver().getLocation(), Sound.IRONGOLEM_HIT, (float)collisionVel, 1f);
			
			double powScale = 0.05;
			
			//Hit Kart
			double relPower = ((collisionVel * other.GetKartType().GetStability()) / (kart.GetKartType().GetStability() / 10));
			if (KartUtil.Stability(kart, (int)relPower))
			{
				//Inform
				UtilPlayer.message(kart.GetDriver(), 	F.main("MK", F.elem(UtilEnt.getName(other.GetDriver())) + " knocked you out."));
				UtilPlayer.message(other.GetDriver(), 	F.main("MK", "You knocked out " + F.elem(UtilEnt.getName(kart.GetDriver())) + "."));
				
				//Crash
				if (collisionVel > 2)	new Crash_Knockback(kart, other.GetDriver().getLocation(), relPower * powScale);
				else					new Crash_Spin(kart, other.GetDriver().getLocation(), relPower * powScale);	
			}
			else
			{
				new Crash_Bump(kart, other.GetDriver().getLocation(), relPower * powScale);
			}

			//Hit Other
			relPower = ((collisionVel * kart.GetKartType().GetStability()) / (other.GetKartType().GetStability() / 10));
			if (KartUtil.Stability(other, (int)relPower))
			{
				//Inform
				UtilPlayer.message(other.GetDriver(), 	F.main("MK", F.elem(UtilEnt.getName(kart.GetDriver())) + " knocked you out."));
				UtilPlayer.message(kart.GetDriver(), 	F.main("MK", "You knocked out " + F.elem(UtilEnt.getName(other.GetDriver())) + "."));
				
				//Crash
				if (collisionVel > 2)	new Crash_Knockback(other, kart.GetDriver().getLocation(), relPower * powScale);
				else					new Crash_Spin(other, kart.GetDriver().getLocation(), relPower * powScale);
			}
			else
			{
				new Crash_Bump(other, kart.GetDriver().getLocation(), relPower * powScale);
			}
		}
	}
}
