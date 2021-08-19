package nautilus.game.arcade.game.modules;

import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.game.GameTeam;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public class SpawnShieldModule extends Module
{

	private final Set<Shield> _shields;

	private int _damage = 5000;
	private int _damageRadiusSquared = 25;
	private int _velocityRadiusSquared = 9;
	private boolean _displayParticles = true;

	public SpawnShieldModule()
	{
		_shields = new HashSet<>();
	}

	public SpawnShieldModule registerShield(GameTeam team, List<Location> barriers)
	{
		return registerShield(team::HasPlayer, barriers, UtilAlg.getAverageLocation(team.GetSpawns()));
	}

	public SpawnShieldModule registerShield(Predicate<Player> shouldNotAffect, List<Location> barriers, Location center)
	{
		_shields.add(new Shield(shouldNotAffect, barriers, center));
		return this;
	}

	public SpawnShieldModule setDamage(int damage)
	{
		_damage = damage;
		return this;
	}

	public SpawnShieldModule setDamageRadius(int radius)
	{
		_damageRadiusSquared = radius * radius;
		return this;
	}

	public SpawnShieldModule setVelocityRadius(int radius)
	{
		_velocityRadiusSquared = radius * radius;
		return this;
	}

	public SpawnShieldModule setDisplayParticles(boolean display)
	{
		_displayParticles = display;
		return this;
	}

	@Override
	public void setup()
	{
		for (Shield shield : _shields)
		{
			for (Location location : shield.Barriers)
			{
				location.getBlock().setType(Material.AIR);
			}
		}
	}

	@EventHandler
	public void updateShield(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTER)
		{
			return;
		}

		for (Shield shield : _shields)
		{
			for (Player player : getGame().GetPlayers(true))
			{
				if (UtilPlayer.isSpectator(player) || shield.ShouldNotAffect.test(player))
				{
					continue;
				}

				if (UtilMath.offsetSquared(player.getLocation(), shield.Center) < _damageRadiusSquared)
				{
					getGame().Manager.GetDamage().NewDamageEvent(player, null, null, DamageCause.VOID, _damage, false, true, true, getGame().GetName(), "Spawn Shield");
				}
				else
				{
					for (Location location : shield.Barriers)
					{
						if (UtilMath.offsetSquared(player.getLocation(), location) < _velocityRadiusSquared)
						{
							Entity passenger = player.getPassenger();

							UtilAction.velocity(passenger == null ? player : passenger, UtilAlg.getTrajectory(location, player.getLocation()).setY(0.4));
							break;
						}
					}
				}
			}
		}
	}

	@EventHandler
	public void updateParticles(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC || !_displayParticles)
		{
			return;
		}

		for (Shield shield : _shields)
		{
			for (Player player : getGame().GetPlayers(true))
			{
				if (UtilPlayer.isSpectator(player) || shield.ShouldNotAffect.test(player))
				{
					continue;
				}

				for (Location location : shield.Barriers)
				{
					UtilParticle.PlayParticle(ParticleType.BARRIER, location.clone().add(0, 0.5, 0), 0, 0, 0, 0.1F, 1, ViewDist.SHORT, player);
				}
			}
		}
	}

	@Override
	public void cleanup()
	{
		_shields.clear();
	}

	@EventHandler
	public void onCustomDamage(CustomDamageEvent event)
	{
		Player player = event.GetDamageePlayer();

		if (player == null)
		{
			return;
		}

		for (Shield shield : _shields)
		{
			if (shield.ShouldNotAffect.test(player) && UtilMath.offsetSquared(player.getLocation(), shield.Center) < _damageRadiusSquared)
			{
				event.SetCancelled("Spawn Shield");
				return;
			}
		}
	}

	private class Shield
	{

		Predicate<Player> ShouldNotAffect;
		List<Location> Barriers;
		Location Center;

		Shield(Predicate<Player> shouldNotAffect, List<Location> barriers, Location center)
		{
			ShouldNotAffect = shouldNotAffect;
			Barriers = barriers;
			Center = center;
		}
	}

}
