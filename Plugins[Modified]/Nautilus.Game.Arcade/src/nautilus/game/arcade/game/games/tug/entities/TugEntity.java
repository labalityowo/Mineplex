package nautilus.game.arcade.game.games.tug.entities;

import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilTime;

import nautilus.game.arcade.game.games.tug.TugOfWool;
import nautilus.game.arcade.game.games.tug.TugTeam;

public abstract class TugEntity<T extends LivingEntity>
{

	private static final int TARGET_RANGE_SQUARED = 64;
	private static final int EAT_RANGE_SQUARED = 2;

	protected final TugOfWool _host;
	protected final TugTeam _team;
	protected final T _entity;
	private final int _attackRange, _attackRate;

	private Location _targetLocation;
	private LivingEntity _targetEntity;
	private long _lastAttack;

	TugEntity(TugOfWool host, TugTeam team, Location spawn, int attackRange, int attackRate)
	{
		_host = host;
		_team = team;
		_entity = spawn(spawn);
		_attackRange = attackRange;
		_attackRate = attackRate;

		UtilEnt.vegetate(_entity);
		UtilEnt.setTickWhenFarAway(_entity, true);
	}

	public abstract T spawn(Location location);

	public abstract void attack(LivingEntity other);

	public boolean attemptAttack(LivingEntity other)
	{
		if (!UtilTime.elapsed(_lastAttack, _attackRate) || UtilMath.offsetSquared(_entity, other) > _attackRange * _attackRange)
		{
			return false;
		}

		_lastAttack = System.currentTimeMillis();
		attack(other);
		return true;
	}

	public boolean attemptEat()
	{
		Location entityLocation = _entity.getLocation();

		if (_targetLocation == null || UtilMath.offsetSquared(entityLocation, _targetLocation) > EAT_RANGE_SQUARED)
		{
			return false;
		}

		eat(_targetLocation.getBlock());
		return true;
	}

	public void eat(Block block)
	{
		Location entityLocation = _entity.getLocation();

		UtilParticle.PlayParticleToAll(ParticleType.HEART, entityLocation.add(0, 1, 0), 1, 1, 1, 0, 6, ViewDist.NORMAL);
		_targetLocation.getWorld().playSound(entityLocation, Sound.EAT, 1.5F, (float) Math.random());
		_targetLocation.getWorld().playEffect(_targetLocation, Effect.STEP_SOUND, block.getType());
		block.setType(Material.AIR);
		_entity.remove();
	}

	public boolean attemptTarget(LivingEntity other)
	{
		if (UtilMath.offsetSquared(_entity, other) < TARGET_RANGE_SQUARED)
		{
			setTargetEntity(other);
			return true;
		}

		return false;
	}

	public void updateName()
	{
		if (!_entity.isCustomNameVisible())
		{
			return;
		}

		ChatColor colour = _team.getGameTeam().GetColor();
		String name = colour.toString() + (int) Math.ceil(_entity.getHealth()) + C.Reset + "/" + colour.toString() + (int) Math.ceil(_entity.getMaxHealth());

		if (name.equals(_entity.getCustomName()))
		{
			return;
		}

		_entity.setCustomName(name);
	}

	public void move()
	{
		Location location;

		if (_targetEntity != null)
		{
			location = _targetEntity.getLocation();
		}
		else if (_targetLocation != null)
		{
			location = _targetLocation;
		}
		else
		{
			return;
		}

		Vector direction = UtilAlg.getTrajectory(_entity.getLocation(), location)
				.multiply(3);
		UtilEnt.CreatureMoveFast(_entity, location.clone().add(direction), getSpeed());
	}

	public void attackNearby()
	{
		UtilEnt.getInRadius(_entity.getLocation(), _attackRange).forEach((entity, scale) ->
		{
			if (entity.equals(_entity) || entity instanceof Player || _team.isEntity(entity))
			{
				return;
			}

			attack(entity);
		});
	}

	public float getSpeed()
	{
		return _host.getSpeed();
	}

	public T getEntity()
	{
		return _entity;
	}

	public void setTargetLocation(Location targetLocation)
	{
		_targetLocation = targetLocation;
	}

	public Location getTargetLocation()
	{
		return _targetLocation;
	}

	public void setTargetEntity(LivingEntity targetEntity)
	{
		_targetEntity = targetEntity;
	}

	public LivingEntity getTargetEntity()
	{
		return _targetEntity;
	}
}
