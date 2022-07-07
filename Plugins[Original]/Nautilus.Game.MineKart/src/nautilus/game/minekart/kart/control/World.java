package nautilus.game.minekart.kart.control;

import mineplex.core.common.util.UtilAlg;
import nautilus.game.minekart.kart.Kart;
import nautilus.game.minekart.kart.KartUtil;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

public class World 
{
	public static void Gravity(Kart kart) 
	{
		//Current Velocity
		Vector vel = kart.GetVelocity();
		if (kart.GetCrash() != null)
			vel = kart.GetCrash().GetVelocity();

		//Landed
		if (KartUtil.IsGrounded(kart))
		{
			if (vel.getY() < 0)
				vel.setY(0);

			return;
		}

		//Downward Gravity
		double down = Math.max(-1.0, vel.getY() - 0.1);
		vel.setY(down);
	}

	public static void AirDrag(Kart kart) 
	{
		//Current Velocity
		Vector vel = kart.GetVelocity();
		if (kart.GetCrash() != null)
			vel = kart.GetCrash().GetVelocity();

		if (vel.length() <= 0)
			return;

		//Drag Horizontal
		if (KartUtil.IsGrounded(kart))
		{
			double drag = Math.log(vel.length() + 1) / 50d;
			drag *= 1 / kart.GetKartType().GetTopSpeed();			//Increase for lower top speed karts
			drag *= (kart.GetKartType().GetAcceleration() / 21d);	//Reduce for lower accel karts

			//Variable Drag
			vel.multiply(1 - drag);
		}
		//Drag Vertical
		else
		{
			vel.setY(vel.getY() * 0.98);
		}	
	}

	public static void FireDrag(Kart kart) 
	{
		if (kart.GetDriver().getFireTicks() <= 0)
			return;

		//Current Velocity
		Vector vel = kart.GetVelocity();
		if (kart.GetCrash() != null)
			return;

		if (vel.length() <= 0)
			return;

		//Drag Horizontal
		double drag = 0.008;

		//Variable Drag
		vel.multiply(1 - drag);	
	}

	public static void RoadDrag(Kart kart) 
	{
		if (!KartUtil.IsGrounded(kart))
			return;
		
		if (kart.GetGP() == null)
			return;
		
		if (kart.GetGP().GetTrack() == null)
			return;

		Block block = kart.GetDriver().getLocation().getBlock().getRelative(BlockFace.DOWN);

		//Current Velocity
		Vector vel = kart.GetVelocity();
		if (kart.GetCrash() != null)
			vel = kart.GetCrash().GetVelocity();

		if (vel.length() <= 0)
			return;

		//Constant Drag
		Vector dragVec = new Vector(vel.getX(), 0, vel.getZ());
		UtilAlg.Normalize(dragVec);
		dragVec.multiply(0.003);
		vel.subtract(dragVec);

		//Off Road
		if (!kart.GetGP().GetTrack().GetTrackBlocks().contains(block.getTypeId()))
			if (block.getType() == Material.GRASS || 
					block.getType() == Material.SAND)	
			{
				//Drag Horizontal
				double drag = 0.025;
	
				//Variable Drag
				vel.multiply(1 - drag);	
	
				//Effect
				if (kart.GetSpeed() > 0.2)
					block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, block.getTypeId());
			}
		
		//Stop Completely
		if (!kart.GetDriver().isBlocking())
			if (vel.length() < 0.02)
				vel.multiply(0);
	}

	public static void BlockDrag(Kart kart) 
	{
		if (!KartUtil.IsGrounded(kart))
			return;

		Block block = kart.GetDriver().getLocation().getBlock();

		//Current Velocity
		Vector vel = kart.GetVelocity();
		if (kart.GetCrash() != null)
			vel = kart.GetCrash().GetVelocity();

		if (vel.length() <= 0)
			return;

		double drag = 0;

		if 		(block.getType() == Material.LONG_GRASS)		drag = 0.02;
		else if (block.getType() == Material.WATER)				drag = 0.03;
		else if (block.getType() == Material.STATIONARY_WATER)	drag = 0.03;

		if (drag <= 0)
			return;

		//Slow
		vel.multiply(1 - drag);

		//Effect
		if (vel.length() > 0.2)
			block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, block.getTypeId());
	}
}
