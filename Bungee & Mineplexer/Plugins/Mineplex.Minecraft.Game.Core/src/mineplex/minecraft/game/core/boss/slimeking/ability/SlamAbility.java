package mineplex.minecraft.game.core.boss.slimeking.ability;

import java.util.LinkedList;
import java.util.List;

import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.minecraft.game.core.boss.slimeking.creature.SlimeCreature;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class SlamAbility extends SlimeAbility
{
	private int _findAttempts;
	private boolean _hasTarget;
	private int _foundTicks;
	private Player _target;
	private Location _targetLocation;

	// Timings
	private final int _lockTick;
	private final int _jumpTick;
	private final int _diveTick;

	public SlamAbility(SlimeCreature slime)
	{
		this(slime, 40, 60, 80);
	}

	public SlamAbility(SlimeCreature slime, int lockTick, int jumpTick, int diveTick)
	{
		super(slime);
		_hasTarget = false;
		_findAttempts = 0;
		_foundTicks = 0;

		assert (jumpTick > lockTick && diveTick > jumpTick);
		_lockTick = lockTick;
		_jumpTick = jumpTick;
		_diveTick = diveTick;
	}

	@Override
	public void tickCustom()
	{
		if (!_hasTarget)
		{
			if (getTicks() % 20 == 0)
			{
				searchForTarget();
			}
		}
		else
		{
			int ticks = getTicks() - _foundTicks;
			if (ticks < _lockTick)
			{
				// Follow Target
				displayTarget(_target.getLocation());
			}
			else if (ticks == _lockTick)
			{
				// Lock on
				Bukkit.broadcastMessage("Target Locked");
				_targetLocation = _target.getLocation();
			}
			else if (ticks < _jumpTick)
			{
				// Target still locked
				displayTarget(_targetLocation);
			}
			else if (ticks == _jumpTick)
			{
				// Target starts jump
				Bukkit.broadcastMessage("Start Jump");
			}
			else if (ticks < _diveTick)
			{
				// Target in air
				displayTarget(_targetLocation);

				Vector direction = UtilAlg.getTrajectory2d(getSlime().getEntity().getLocation(), _targetLocation);
				direction.multiply(0.4);
				direction.setY(2 * (1 - ((getTicks() - 100.0) / 60.0)));
				getSlime().getEntity().setVelocity(direction);
			}
			else if (ticks == _diveTick)
			{
				displayTarget(_targetLocation);

				// Time to go down!
				getSlime().getEntity().setVelocity(new Vector(0, -3, 0));
				getSlime().getEntity().setFallDistance(0);
			}
			else if (ticks > _diveTick)
			{
				displayTarget(_targetLocation);

				// Check for hitting ground
				if (getSlime().getEntity().isOnGround())
				{
					// We're done here!
					setIdle(true);

					damageArea(getSlime().getEntity().getLocation());

				}
			}

		}
	}

	private void damageArea(Location location)
	{
		// TODO Deal more damage based on how close you are to the slime?
		List<Player> nearPlayers = UtilPlayer.getNearby(location, 4);
		for (Player player : nearPlayers)
		{
			player.damage(4);
			player.setVelocity(UtilAlg.getTrajectory2d(location, player.getLocation()).setY(0.2));
		}

		UtilParticle.PlayParticle(UtilParticle.ParticleType.LAVA, location, 2, 0.5F, 2, 0, 100, UtilParticle.ViewDist.LONG, UtilServer.getPlayers());
		location.getWorld().playSound(location, Sound.ANVIL_LAND, 10, 0.5F);
	}

	private void displayTarget(Location location)
	{
		UtilParticle.PlayParticle(UtilParticle.ParticleType.LAVA, location, 0, 0, 0, 0, 1, UtilParticle.ViewDist.NORMAL, UtilServer.getPlayers());
	}

	private void searchForTarget()
	{
		if (_findAttempts >= 10)
		{
			// Just give up! THERE'S NO HOPE
			setIdle(true);
			return;
		}

		Player target = UtilPlayer.getRandomTarget(getSlime().getEntity().getLocation(), 15);
		if (target != null)
		{
			_target = target;
			_hasTarget = true;
			_foundTicks = getTicks();
			Bukkit.broadcastMessage("Target placed on " + _target);
		}

		_findAttempts++;
	}
}
