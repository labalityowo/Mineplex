package mineplex.minecraft.game.core.boss.ironwizard.abilities;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.UUID;

import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilServer;
import mineplex.minecraft.game.core.boss.BossAbility;
import mineplex.minecraft.game.core.boss.ironwizard.GolemCreature;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.util.Vector;

public class GolemEarthquake extends BossAbility<GolemCreature, IronGolem>
{
	private Location _center;
	private float _range;
	private int _tick;
	private ArrayList<UUID> _damaged = new ArrayList<UUID>();
	private boolean _earthquake;

	public GolemEarthquake(GolemCreature creature)
	{
		super(creature);
		_center = getLocation();
	}

	@Override
	public boolean canMove()
	{
		return !UtilEnt.isGrounded(getEntity()) && _tick > 1;
	}

	@Override
	public Player getTarget()
	{
		return getTarget(7);
	}

	@Override
	public boolean hasFinished()
	{
		return _range > 19;
	}

	@Override
	public void setFinished()
	{
	}

	@Override
	public void tick()
	{
		Entity entity = getEntity();

		if (_tick == 0)
		{
			entity.getWorld().playSound(entity.getLocation(), Sound.IRONGOLEM_THROW, 4, 0);

			entity.setVelocity(new Vector(0, 1, 0));
		}
		else if (!_earthquake)
		{
			_earthquake = _tick > 10 && UtilEnt.isGrounded(entity);
		}

		if (_earthquake)
		{
			_range += 0.7;

			for (float range = _range - 2; range <= _range; range++)
			{
				if (range <= 0)
				{
					continue;
				}

				for (int x = -1; x <= 1; x++)
				{
					for (int z = -1; z <= 1; z++)
					{
						if ((x != 0) == (z != 0))
						{
							continue;
						}

						UtilParticle.PlayParticle(ParticleType.BLOCK_DUST.getParticle(Material.DIRT, 0),
								_center.clone().add(x * range, 0.1, z * range), (x != 0) ? 0 : (range / 2), 0.1F,
								(z != 0) ? 0 : (range / 2), 0, (int) (range * 4), UtilParticle.ViewDist.NORMAL,
								UtilServer.getPlayers());
					}
				}
			}

			_center.getWorld().playSound(_center, Sound.DIG_STONE, 2, 0.8F);

			HashSet<Player> toDamage = new HashSet<Player>();

			Location cornerA = _center.clone().add(-_range, -1, -_range);
			Location cornerB = _center.clone().add(_range, 1, _range);
			Location cornerA1 = _center.clone().add(-(_range - 1.5), -1, -(_range - 1.5));
			Location cornerB1 = _center.clone().add(_range - 1.5, 1, _range - 1.5);

			for (Player player : Bukkit.getOnlinePlayers())
			{
				Location pLoc = player.getLocation();

				if (_damaged.contains(player.getUniqueId()))
				{
					continue;
				}

				if (!UtilAlg.inBoundingBox(pLoc, cornerA, cornerB))
				{
					continue;
				}

				if (UtilAlg.inBoundingBox(pLoc, cornerA1, cornerB1))
				{
					continue;
				}

				toDamage.add(player);
			}

			for (Player player : toDamage)
			{
				_damaged.add(player.getUniqueId());

				getBoss().getEvent().getDamageManager().NewDamageEvent((LivingEntity) player, getEntity(), null,
						DamageCause.CONTACT, 14 * getBoss().getDifficulty(), false, true, false, "Iron Wizard Earthquake",
						"Iron Wizard Earthquake");

				getBoss().getEvent().getCondition().Factory().Slow("Earthquake", (LivingEntity) player, getEntity(), 3, 1, false,
						false, false, false);

				// Velocity
				UtilAction.velocity(player, UtilAlg.getTrajectory2d(getLocation().toVector(), player.getLocation().toVector()),
						1.8, true, 0, 0.5, 0.5, true);

				// Condition
				getBoss().getEvent().getCondition().Factory().Falling("Earthquake", player, getEntity(), 10, false, true);
			}
		}

		_tick++;
	}

	@Override
	public boolean inProgress()
	{
		return !_earthquake;
	}
}
