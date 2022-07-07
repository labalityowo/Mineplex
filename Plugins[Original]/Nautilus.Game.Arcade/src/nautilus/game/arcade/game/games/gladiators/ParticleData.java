package nautilus.game.arcade.game.games.gladiators;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;

/**
 * Created by William (WilliamTiger).
 * 08/12/15
 */
public class ParticleData
{
	public Player Player;
	public Location CurrentLocation;
	public Vector Velocity;
	public Location Target;

	public ParticleData(Player player, Location target)
	{
		Player = player;

		Velocity = player.getLocation().getDirection();
		if (Velocity.getY() < 0)
			Velocity.setY(0);
		Velocity.normalize();

		CurrentLocation = player.getLocation().add(0, 1, 0);
		Target = target;
	}

	public boolean update()
	{
		//Turn
		Velocity.add(UtilAlg.getTrajectory(CurrentLocation, Target).multiply(0.15));

		//Normalize Speed
		UtilAlg.Normalize(Velocity);

		//Move
		CurrentLocation.add(Velocity.clone().multiply(0.5));

		//Particle
		UtilParticle.PlayParticle(UtilParticle.ParticleType.HAPPY_VILLAGER, CurrentLocation, 0.03f, 0.03f, 0.03f, 0, 3,
				UtilParticle.ViewDist.LONG, Player);

		//Sound
		CurrentLocation.getWorld().playSound(CurrentLocation, Sound.FIZZ, 0.2f, 3f);

		return UtilMath.offset(CurrentLocation, Target) < 4;
	}
}