package nautilus.game.arcade.game.games.moba.ai.goal;

import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilMath;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

public class MobaDirectAIMethod implements MobaAIMethod
{

	@Override
	public boolean updateMovement(LivingEntity entity, Location goal, float speed)
	{
		Location entityLocation = entity.getLocation();

		// Speed is number of ticks to travel 1 block
		float magnitude = speed / 20F;

		// Get the direct vector between the entity and the goal
		Vector direction = UtilAlg.getTrajectory(entityLocation, goal);

		// From the direction, get the yaw of this direction
		float directionYaw = UtilAlg.GetYaw(direction);
		entityLocation.setYaw(directionYaw);

		// If reached the goal
		if (UtilMath.offsetSquared(entityLocation, goal) < 4)
		{
			return false;
		}
		else
		{
			// Modify the direction's magnitude to be at the same rate as the speed
			direction.multiply(magnitude);

			// Add the modified direction to the original entity location
			entityLocation.add(direction);
		}

		// Move the entity to its new location
		entity.teleport(entityLocation);
		return true;
	}
}
