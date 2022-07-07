package mineplex.minecraft.game.core.boss.ironwizard.abilities;

import java.util.ArrayList;

import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.minecraft.game.core.boss.BossAbility;
import mineplex.minecraft.game.core.boss.ironwizard.GolemCreature;
import net.minecraft.server.v1_8_R3.EntityIronGolem;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftIronGolem;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.util.Vector;

/**
 * Rumble is where the golem picks a target then starts playing a animation for a second where its obviously preparing to use it.
 * Copy this from Wizards
 */
public class GolemRumble extends BossAbility<GolemCreature, IronGolem>
{
	private Location _loc;
	private int _ticks;
	private int _travelled;
	private Vector _vec;
	private int _width = 1;
	private Location _target;

	public GolemRumble(GolemCreature creature)
	{
		super(creature);

		Player target = getTarget();

		if (target != null)
		{
			_target = target.getLocation();

			UtilEnt.CreatureLook(getEntity(), _target);
		}
	}

	@Override
	public boolean canMove()
	{
		return _ticks >= 20;
	}

	@Override
	public boolean hasFinished()
	{
		return _travelled > 35;
	}

	@Override
	public void setFinished()
	{
	}

	@Override
	public void tick()
	{
		if (_ticks++ < 14)
		{
			IronGolem entity = getEntity();
			EntityIronGolem golem = ((CraftIronGolem) entity).getHandle();

			golem.world.broadcastEntityEffect(golem, (byte) 4);

			if (_ticks % 2 == 0)
			{
				entity.getWorld().playSound(entity.getLocation(), Sound.IRONGOLEM_THROW, 3, 2F);
			}
		}
		else if (_ticks % 2 == 0)
		{
			int oldWidth = _width;

			if ((_width <= 3 || _ticks % 4 == 0) && _width <= 6)
			{
				_width++;
			}

			Location newLoc;
			boolean validBlock = false;
			ArrayList<Block> current = new ArrayList<Block>();

			if (_vec == null)
			{
				_vec = _target.subtract(getLocation()).toVector().setY(0).normalize();// .multiply(0.5);
				_loc = getLocation().subtract(0, 1, 0).getBlock().getLocation().add(0, 0.99, 0);
				newLoc = _loc;
				current.add(_loc.getBlock());

				validBlock = true;
			}
			else
			{ // Move rumble
				newLoc = _loc.clone().add(_vec);

				// Check if the rumble needs to go up or drop a block or two
				for (int y : new int[]
					{
							0, 1, -1
					})
				{
					for (int a = 1; a <= 2; a++)
					{
						Block b = newLoc.clone().add(_vec.clone().multiply(a)).getBlock().getRelative(0, y, 0);

						if (UtilBlock.solid(b) && !UtilBlock.solid(b.getRelative(0, 1, 0)))
						{
							validBlock = true;
							newLoc.add(0, y, 0);

							break;
						}
					}

					if (validBlock)
					{
						break;
					}
				}

				for (int width = -_width; width <= _width; width++)
				{
					if (Math.abs(width) <= oldWidth)
					{
						Block b = _loc.clone().add(UtilAlg.getRight(_vec).multiply(width)).getBlock();

						if (!current.contains(b))
						{
							current.add(b);
						}
					}

					if (validBlock)
					{
						Block b = newLoc.clone().add(UtilAlg.getRight(_vec).multiply(width)).getBlock();

						if (!current.contains(b))
						{
							current.add(b);

							b.getWorld().playEffect(b.getLocation(), Effect.STEP_SOUND, b.getTypeId());
						}
					}
				}
			}

			UtilEnt.CreatureLook(getEntity(), _loc);

			for (Entity entity : getEntity().getWorld().getEntities())
			{
				if (entity instanceof Damageable && !UtilPlayer.isSpectator(entity) && entity != getEntity())
				{
					Block b = entity.getLocation().getBlock();
					boolean canDamage = false;

					for (int y = -1; y <= 0; y++)
					{
						if (current.contains(b.getRelative(0, y, 0)))
						{
							canDamage = true;
							break;
						}
					}

					if (!canDamage)
					{
						continue;
					}

					if (canDamage(entity))
					{
						getBoss().getEvent().getDamageManager().NewDamageEvent((LivingEntity) entity, getEntity(), null,
								DamageCause.CONTACT, 8 * getBoss().getDifficulty(), false, true, false, "Iron Wizard Rumble",
								"Iron Wizard Rumble");
					}

					UtilAction.velocity(entity, _vec.clone(), 1.5, true, 0, 0.2, 1, true);

					if (entity instanceof Player)
					{
						getBoss().getEvent().getCondition().Factory().Slow("Rumble", (LivingEntity) entity, getEntity(), 3, 1,
								false, false, false, false);
					}
				}
			}

			if (_travelled++ > 35 || !validBlock)
			{
				_travelled = 100;
			}

			_loc = newLoc;
		}
	}

	public Player getTarget()
	{
		Player target = null;
		double dist = 0;

		for (Player player : UtilPlayer.getNearby(getLocation(), 30, true))
		{
			if (!player.hasLineOfSight(getEntity()))
			{
				continue;
			}

			double d = player.getLocation().distance(getLocation());

			if (d > 2 && (target == null || dist > d))
			{
				target = player;
				dist = d;
			}
		}

		return target;
	}

	@Override
	public boolean inProgress()
	{
		return _ticks < 14;
	}
}
