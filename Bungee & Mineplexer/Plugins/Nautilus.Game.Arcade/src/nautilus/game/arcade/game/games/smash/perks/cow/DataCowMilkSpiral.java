package nautilus.game.arcade.game.games.smash.perks.cow;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;

public class DataCowMilkSpiral
{
	public long Time;
	public Player Player;
	public Vector Direction;
	
	public boolean SuperActive;
	
	public boolean DisableVelocity = false;
		
	public Location Spiral;
	public Location SpiralA = null;
	public Location SpiralB = null;

	public final Map<Player, Integer> HitPlayers;
	
	public DataCowMilkSpiral(Player player, boolean superActive)
	{
		Time = System.currentTimeMillis();
		
		Player = player;
		
		SuperActive = superActive;
		
		Direction = player.getLocation().getDirection();
		
		Spiral = player.getLocation().add(new Vector(0,1,0)).add(player.getLocation().getDirection().multiply(2));

		HitPlayers = new HashMap<>();
	}

	public boolean update()
	{
		//Propel
		if (!DisableVelocity && !Player.isSneaking() && !UtilTime.elapsed(Time, SuperActive ? 2400 : 1800))
			UtilAction.velocity(Player, Direction.clone().add(new Vector(0, 0.1, 0)).normalize().multiply(0.45));
		else
			DisableVelocity = true;

		//Move Forward
		Spiral.add(Direction.clone().multiply(0.7));

		//Spiral
		for (int i = 0; i < 2; i++)
		{
			double lead = i * ((2d * Math.PI) / 2);

			//Orbit
			double speed = 3d;
			double oX = -Math.sin(Player.getTicksLived() / speed + lead) * 1.5;
			double oZ = Math.cos(Player.getTicksLived() / speed + lead) * 1.5;

			Location newSpiral = Spiral.clone();
			newSpiral.add(UtilAlg.getLeft(Direction).multiply(oX));
			newSpiral.add(UtilAlg.getUp(Direction).multiply(oZ));

			newSpiral.getWorld().playSound(newSpiral, Sound.SPLASH, 0.2f, 0.75f);

			if (i == 0)
			{
				if (SpiralA != null)
				{
					while (UtilMath.offset(SpiralA, newSpiral) > 0.2)
					{
						SpiralA.add(UtilAlg.getTrajectory(SpiralA, newSpiral).multiply(0.2));
						UtilParticle.PlayParticle(SuperActive ? ParticleType.RED_DUST : ParticleType.FIREWORKS_SPARK, SpiralA, 0, 0, 0, 0, 1, ViewDist.LONG, UtilServer.getPlayers());
					}
				}
				else
				{
					SpiralA = newSpiral;
				}
			}


			else
			{
				if (SpiralB != null)
				{
					while (UtilMath.offset(SpiralB, newSpiral) > 0.1)
					{
						SpiralB.add(UtilAlg.getTrajectory(SpiralB, newSpiral).multiply(0.1));
						UtilParticle.PlayParticle(SuperActive ? ParticleType.RED_DUST : ParticleType.FIREWORKS_SPARK, SpiralB, 0, 0, 0, 0, 1, ViewDist.LONG, UtilServer.getPlayers());
					}
				}
				else
				{
					SpiralB = newSpiral;
				}
			}
		}

		return UtilTime.elapsed(Time, 3000);

	}
}
