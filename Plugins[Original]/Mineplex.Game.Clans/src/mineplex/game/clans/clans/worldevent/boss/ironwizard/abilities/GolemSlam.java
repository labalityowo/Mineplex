package mineplex.game.clans.clans.worldevent.boss.ironwizard.abilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.clans.clans.worldevent.api.BossAbility;
import mineplex.game.clans.clans.worldevent.boss.ironwizard.GolemCreature;

public class GolemSlam extends BossAbility<GolemCreature, IronGolem>
{
	private List<Item> _items = new ArrayList<>();
	private int _ticksFinished;
	private int _stage;
	private Vector _target;
	private int _ticksJumped;

	public GolemSlam(GolemCreature creature)
	{
		super(creature);

		Player target = getTarget();

		if (target != null)
		{
			_target = UtilAlg.calculateVelocity(getLocation().toVector(),
					target.getLocation().toVector().setY(getLocation().getY()), 2, getEntity());
		}
	}

	@Override
	public boolean canMove()
	{
		return !UtilEnt.isGrounded(getEntity()) && _stage == 1;
	}

	@Override
	public boolean hasFinished()
	{
		return _stage == 2 && --_ticksFinished <= 0;
	}

	@Override
	public void setFinished()
	{
		for (Item item : _items)
		{
			item.remove();
		}
	}

	@Override
	public void tick()
	{
		Entity entity = getEntity();

		if (_stage == 0)
		{
			UtilEnt.CreatureLook(getEntity(), getLocation().add(_target));

			entity.getWorld().playSound(entity.getLocation(), Sound.IRONGOLEM_THROW, 4, 0);

			entity.setVelocity(_target);
			_stage++;
		}
		else if (_stage == 1)
		{
			_ticksJumped++;

			if (_ticksJumped > 4 && getLocation().subtract(0, 0.2, 0).getBlock().getType() != Material.AIR)
			{
				explodeRupture(getLocation());

				_stage = 2;
			}
		}
	}

	@EventHandler
	public void HopperPickup(InventoryPickupItemEvent event)
	{
		if (_items.contains(event.getItem()))
		{
			event.setCancelled(true);
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void ItemDestroy(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		if (_items.isEmpty())
		{
			return;
		}

		Iterator<Item> itemIterator = _items.iterator();

		while (itemIterator.hasNext())
		{
			Item item = itemIterator.next();

			if (item.isDead() || !item.isValid())
			{
				item.remove();
				itemIterator.remove();
			}
			else if (UtilEnt.isGrounded(item) || item.getTicksLived() > 60)
			{
				item.getWorld().playEffect(item.getLocation(), Effect.STEP_SOUND, item.getItemStack().getTypeId());
				item.remove();
				itemIterator.remove();
			}
		}
	}

	@Override
	public boolean inProgress()
	{
		return true;
	}

	@Override
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

			if (d < 10)
			{
				continue;
			}

			if (target == null || dist > d)
			{
				target = player;
				dist = d;
			}
		}

		return target;
	}

	@SuppressWarnings("deprecation")
	private void explodeRupture(Location loc)
	{
		loc.add(0, 1.1, 0);
		loc.setX(loc.getBlockX() + 0.5);
		loc.setZ(loc.getBlockZ() + 0.5);

		// Fling
		Map<LivingEntity, Double> targets = UtilEnt.getInRadius(loc, 3.5);
		for (LivingEntity cur : targets.keySet())
		{
			if (cur.equals(getEntity()))
			{
				continue;
			}

			// Velocity
			UtilAction.velocity(cur,
					UtilAlg.getTrajectory2d(loc.toVector().add(new Vector(0.5, 0, 0.5)), cur.getLocation().toVector()),
					0.8 + 0.8 * targets.get(cur), true, 0, 0.4 + 1.0 * targets.get(cur), 1.4, true);

			// Condition
			getBoss().getEvent().getCondition().Factory().Falling("Rupture", cur, getEntity(), 10, false, true);

			// Damage Event
			getBoss().getEvent().getDamageManager().NewDamageEvent(cur, getEntity(), null, DamageCause.CUSTOM,
					8 * getBoss().getDifficulty(), false, true, false, "Iron Wizard", "Rupture");
		}

		List<Block> blocks = new ArrayList<Block>();

		for (int x = -3; x <= 3; x++)
		{
			for (int z = -3; z <= 3; z++)
			{
				for (int y = 0; y <= 1; y++)
				{
					for (int i = 0; i < 2; i++)
					{
						if (Math.sqrt(x * x + z * z + y * y) <= 3)
						{
							blocks.add(loc.clone().add(x, y, z).getBlock());
						}
					}
				}
			}
		}

		Collections.shuffle(blocks);

		// Blocks
		int done = 0;
		Iterator<Block> itel = blocks.iterator();

		while (done < 30 && itel.hasNext())
		{
			Block block = itel.next();

			Vector vec = new Vector(Math.random() - 0.5, Math.random() - 0.5, Math.random() - 0.5).normalize();

			if (!UtilBlock.airFoliage(block))
				continue;

			// Add Directional
			vec.add(UtilAlg.getTrajectory(loc.getBlock().getLocation(), block.getLocation().add(0.5, 0, 0.5)));

			// Add Up
			vec.add(new Vector(0, 1.6, 0));

			vec.normalize();

			// Scale
			vec.multiply(0.1 + 0.3 * Math.random() + 0.6);

			// Block!
			Item item = loc.getWorld().dropItem(block.getLocation().add(0.5, 0, 0.5), new ItemStack(Material.DIRT, 0));
			item.setVelocity(vec);
			item.setPickupDelay(50000);
			_items.add(item);

			// Effect
			loc.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, Material.DIRT.getId());

			done++;
		}

		_ticksFinished = 20;
	}
}