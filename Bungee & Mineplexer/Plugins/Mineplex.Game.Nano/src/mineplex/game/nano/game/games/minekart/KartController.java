package mineplex.game.nano.game.games.minekart;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.world.MineplexWorld;

public class KartController
{

	public enum DriftDirection
	{
		LEFT, RIGHT
	}

	static void applyAirResistance(Kart kart)
	{
		Vector velocity = kart.getVelocity();

		if (kart.getFrontWaysInput() == 0 && velocity.length() < 0.1)
		{
			velocity.multiply(0);
		}
		else
		{
			velocity.multiply(1 - (Math.log(velocity.length() + 1) / 50));
		}
	}

	static void collideBlock(Kart kart)
	{
		if (kart.isCrashed())
		{
			return;
		}

		Location location = kart.getVehicle().getLocation();
		Block block = location.getBlock();
		Block down = block.getRelative(BlockFace.DOWN);
		Vector velocity = kart.getVelocity();
		boolean collision = block.getType() != Material.AIR;

		if (down.getType() == Material.GOLD_BLOCK)
		{
			velocity.multiply(1.1);
			kart.setBoost();
		}
		else if (down.getType() == Material.SAND)
		{
			if (velocity.length() > 0.2)
			{
				velocity.multiply(0.95);
			}
		}
		else if (block.getType() == Material.SOUL_SAND)
		{
			if (velocity.length() > 0.3)
			{
				velocity.multiply(0.95);
			}

			collision = false;
		}

		if (collision)
		{
			double length = velocity.length();

			if (block.getRelative(BlockFace.UP).getType() != Material.AIR)
			{
				if (length > 0.4)
				{
					velocity.multiply(-0.6);
					velocity.setY(0.3);
				}
				else
				{
					velocity = UtilAlg.getTrajectory(kart.getYaw(), 0).multiply(-0.4);
					velocity.setY(0.3);

					kart.setVelocity(velocity);
				}

				kart.setCrashed(true);
			}
			else
			{
				Vector offset = kart.getOffset();
				double blockY = location.getY() % 1;

				if (UtilBlock.isSlab(block) && blockY < 0.5)
				{
					offset.add(new Vector(0, 0.51, 0));
				}
				else
				{
					offset.add(new Vector(0, 1.01 - blockY, 0));
				}
			}
		}
	}

	static boolean collideOutOfBounds(Kart kart, MineplexWorld world)
	{
		return !UtilAlg.inBoundingBox(kart.getVehicle().getLocation(), world.getMin(), world.getMax());
	}

	static void accelerate(Kart kart)
	{
		if (kart.isCrashed() || kart.getFrontWaysInput() <= 0)
		{
			return;
		}

		Vector velocity = kart.getVelocity();

		if (velocity.lengthSquared() == 0)
		{
			kart.setVelocity(UtilAlg.getTrajectory(kart.getYaw(), 0).multiply(0.001 * kart.getFrontWaysInput()));
		}

		Vector acceleration = new Vector(velocity.getX(), 0, velocity.getZ());

		if (acceleration.lengthSquared() > 0)
		{
			acceleration.normalize();
		}

		velocity.add(acceleration.multiply(0.01));
	}

	static void brake(Kart kart)
	{
		if (kart.getFrontWaysInput() >= 0)
		{
			return;
		}

		kart.getVelocity().multiply(0.95);
	}

	static void turn(Kart kart)
	{
		Vector velocity = kart.getVelocity();
		double speed = velocity.length();

		if (speed < 0.05)
		{
			return;
		}

		float velocityInverse = (float) (2 - speed);
		float newYaw = kart.getYaw() - (4 * kart.getSidewaysInput() * velocityInverse);

		kart.setYaw(newYaw);

		Vector turn = UtilAlg.getTrajectory(newYaw, 0);

		turn.subtract(new Vector(velocity.getX(), 0, velocity.getZ()).normalize()).multiply(0.05);

		velocity.add(turn);

		speed = (speed + (velocity.length() * 3)) / 4;

		velocity.normalize().multiply(speed);
	}

	static void drift(Kart kart)
	{
		if (kart.isBoosting())
		{
			UtilParticle.PlayParticleToAll(ParticleType.FLAME, kart.getParticleLocation(), null, 0.1F, 2, ViewDist.NORMAL);
		}

		if (!kart.canBoost())
		{
			return;
		}

		if (kart.getDriftDirection() != null && kart.getVelocity().length() > 0.25)
		{
			float power = kart.getDriftPower() + (0.05F * (kart.getDriftDirection() == DriftDirection.LEFT ? kart.getSidewaysInput() : -kart.getSidewaysInput()));

			if (power > 0.3)
			{
				UtilParticle.PlayParticleToAll(ParticleType.FIREWORKS_SPARK, kart.getParticleLocation(), null, 0.1F, power > 0.6 ? 2 : 1, ViewDist.NORMAL);
			}

			kart.setDriftPower(power);
		}
		else if (kart.getDriftPower() > 0.3)
		{
			Vector facing = UtilAlg.getTrajectory(kart.getYaw(), 0).multiply(0.5 + (kart.getDriftPower() * 0.6));
			kart.setVelocity(facing);
			kart.setDriftLast();
		}
		else
		{
			kart.setDriftPower(0);
		}
	}

	static void applyTopSpeed(Kart kart, int position)
	{
		if (kart.isCrashed())
		{
			return;
		}

		Vector velocity = kart.getVelocity();
		double length = velocity.length();
		double max = kart.isBoosting() ? 1.5 : 0.7 + (position / 100F);

		if (length > max)
		{
			velocity.multiply(0.98);
		}
	}
}
