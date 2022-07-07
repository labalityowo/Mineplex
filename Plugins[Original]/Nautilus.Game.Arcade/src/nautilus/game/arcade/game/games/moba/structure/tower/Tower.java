package nautilus.game.arcade.game.games.moba.structure.tower;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.disguise.disguises.DisguiseGuardian;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.games.moba.Moba;
import nautilus.game.arcade.game.games.moba.util.MobaUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import java.util.concurrent.TimeUnit;

public class Tower
{

	private static final int DAMAGE = 3;
	private static final double HEALING = 2.6;
	private static final int TARGET_RANGE = 10;
	public static final int TARGET_RANGE_SQUARED = TARGET_RANGE * TARGET_RANGE;
	private static final int MIN_INFORM_TIME = (int) TimeUnit.SECONDS.toMillis(30);

	private final Moba _host;

	private final Location _location;
	private final GameTeam _team;
	private float _fallbackYaw;

	private double _health;
	private int _maxHealth;
	private boolean _firstTower;
	private boolean _dead;
	private long _lastInform;
	private double _damage;

	private ArmorStand _stand;
	private DisguiseGuardian _guardian;
	private EnderCrystal _crystal;
	private LivingEntity _target;

	public Tower(Moba host, Location location, GameTeam team, int health, boolean firstTower)
	{
		_host = host;
		_location = location;
		_team = team;
		_health = health;
		_maxHealth = health;
		_firstTower = firstTower;
		_lastInform = System.currentTimeMillis();
		_damage = DAMAGE;
	}

	public void setup()
	{
		_fallbackYaw = UtilAlg.GetYaw(UtilAlg.getTrajectory(_location, _host.GetSpectatorLocation()));
		_location.setYaw(_fallbackYaw);
		_stand = _location.getWorld().spawn(_location, ArmorStand.class);
		_stand.setGravity(false);
		_stand.setMaxHealth(_maxHealth);
		_stand.setHealth(_health);

		_guardian = new DisguiseGuardian(_stand);
		_host.getArcadeManager().GetDisguise().disguise(_guardian);

		//_guardian.setElder(!_firstTower);

		_guardian.setCustomNameVisible(true);

		_crystal = _location.getWorld().spawn(getTowerBase(), EnderCrystal.class);
		_crystal.setCustomNameVisible(true);

		updateDisplay();
	}

	public void updateTarget()
	{
		if (_target == null)
		{
			// Reset damage
			_damage = DAMAGE;

			// Target just entities
			LivingEntity target = MobaUtil.getBestEntityTarget(_host, _team, _stand, _crystal.getLocation(), TARGET_RANGE, false);

			if (target == null)
			{
				// Try targeting players
				target = MobaUtil.getBestEntityTarget(_host, _team, _stand, _crystal.getLocation(), TARGET_RANGE, true);
			}

			_target = target;
			setLaserTarget(target);
		}
		else
		{
			double dist = UtilMath.offsetSquared(_crystal.getLocation(), _target.getEyeLocation());

			if (dist > TARGET_RANGE_SQUARED || UtilPlayer.isSpectator(_target) || _target.isDead() || !_target.isValid())
			{
				_target = null;
				setLaserTarget(null);
			}
		}
	}

	public void updateDamage()
	{
		if (_target == null || _dead)
		{
			return;
		}

		_target.getWorld().playSound(_target.getLocation(), Sound.EXPLODE, 1, 0.2F);
		UtilParticle.PlayParticleToAll(ParticleType.LARGE_EXPLODE, _target.getLocation().add(0, 1.5, 0), 0, 0, 0, 0.2F, 1, ViewDist.LONG);
		_host.getArcadeManager().GetDamage().NewDamageEvent(_target, null, null, DamageCause.CUSTOM, _damage++, false, true, false, "Tower", "Tower");
	}

	public void updateHealing()
	{
		if (_dead)
		{
			return;
		}

		for (Player player : _team.GetPlayers(true))
		{
			if (UtilPlayer.isSpectator(player) || UtilMath.offsetSquared(player, _crystal) > TARGET_RANGE_SQUARED)
			{
				continue;
			}

			MobaUtil.heal(player, null, HEALING);
		}
	}

	private void setLaserTarget(LivingEntity target)
	{
		if (target == null)
		{
			_guardian.setTarget(0);

			Location standLocation = _stand.getLocation();
			_guardian.getEntity().setLocation(standLocation.getX(), standLocation.getY(), standLocation.getZ(), _fallbackYaw, 0);
		}
		else
		{
			_guardian.setTarget(target.getEntityId());
		}

		_host.getArcadeManager().GetDisguise().updateDisguise(_guardian);
	}

	public void damage(double damage)
	{
		_health -= damage;

		if (_health <= 0)
		{
			UtilServer.CallEvent(new TowerDestroyEvent(this));

			// Boom!
			explode();

			for (Player player : Bukkit.getOnlinePlayers())
			{
				player.playSound(player.getLocation(), Sound.BLAZE_BREATH, 1, 0.4F);
			}

			_host.Announce(F.main("Game", _team.GetFormattedName() + C.mBody + " has lost a tower!"), false);

			// Nullify everything and remove all entities
			_target = null;
			setLaserTarget(null);
			_dead = true;
			_stand.remove();
			_crystal.remove();
		}
		else
		{
			_stand.setHealth(_health);
			updateDisplay();
		}
	}

	private void updateDisplay()
	{
		String out = MobaUtil.getHealthBar(_stand, 40);

		_guardian.setName(out);
		_crystal.setCustomName(out);

		if (UtilTime.elapsed(_lastInform, MIN_INFORM_TIME))
		{
			_lastInform = System.currentTimeMillis();

			for (Player player : _team.GetPlayers(true))
			{
				player.playSound(player.getLocation(), Sound.ANVIL_LAND, 1, 0.5F);
				player.sendMessage(F.main("Game", "Your Tower is under attack!"));
			}
		}
	}

	private void explode()
	{
		_host.getArcadeManager().GetExplosion().BlockExplosion(UtilBlock.getBlocksInRadius(_crystal.getLocation().add(0, 4, 0), 4), _location, false);
		_location.getWorld().playSound(_location, Sound.EXPLODE, 2, 0.6F);
		UtilParticle.PlayParticleToAll(ParticleType.HUGE_EXPLOSION, _location, 0, 0, 0, 0.1F, 1, ViewDist.LONG);
	}

	private Location getTowerBase()
	{
		Block last = _location.getBlock();
		boolean firstAir = false;

		while (true)
		{
			last = last.getRelative(BlockFace.DOWN);

			if (!firstAir && last.getType() == Material.AIR)
			{
				firstAir = true;
			}
			else if (firstAir && last.getType() != Material.AIR)
			{
				break;
			}
		}

		return last.getLocation().add(0.5, 0.5, 0.5);
	}

	public Location getLocation()
	{
		return _location;
	}

	public GameTeam getOwner()
	{
		return _team;
	}

	public ArmorStand getStand()
	{
		return _stand;
	}

	public EnderCrystal getCrystal()
	{
		return _crystal;
	}

	public boolean isFirstTower()
	{
		return _firstTower;
	}

	public boolean isDead()
	{
		return _dead;
	}

	public double getHealth()
	{
		return _health;
	}

	public double getMaxHealth()
	{
		return _maxHealth;
	}

}
