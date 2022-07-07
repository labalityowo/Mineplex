package mineplex.game.clans.clans.worldevent.boss.ironwizard.abilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftIronGolem;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import mineplex.core.common.Pair;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.clans.clans.worldevent.api.BossAbility;
import mineplex.game.clans.clans.worldevent.boss.ironwizard.GolemCreature;
import net.minecraft.server.v1_8_R3.EntityIronGolem;

public class GolemRupture extends BossAbility<GolemCreature, IronGolem>
{
	private List<Pair<Location, Location>> _ruptures = new ArrayList<>();
	private Map<Location, Long> _ruptureTime = new HashMap<>();
	private List<String> _targetted = new ArrayList<>();
	private int _rupturesLeft;
	private int _tick;
	private List<Item> _items = new ArrayList<>();
	private int _ticksFinished;

	public GolemRupture(GolemCreature creature)
	{
		super(creature);

		if (creature.getHealthPercent() > 0.75)
		{
			_rupturesLeft = 2;
		}
		else if (creature.getHealthPercent() > 0.5)
		{
			_rupturesLeft = 5;
		}
		else
		{
			_rupturesLeft = 10;
		}
	}

	@Override
	public boolean canMove()
	{
		return false;
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
		return false;
	}

	@Override
	public boolean hasFinished()
	{
		return _rupturesLeft <= 0 && _ruptures.isEmpty() && --_ticksFinished <= 0;
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
		Iterator<Pair<Location, Location>> itel = _ruptures.iterator();

		while (itel.hasNext())
		{
			Pair<Location, Location> pair = itel.next();

			if (pair.getLeft().distance(pair.getRight()) > 0)
			{
				Vector vec = pair.getRight().toVector().subtract(pair.getLeft().toVector());

				if (vec.length() > 1)
				{
					vec = vec.normalize();
				}

				pair.getLeft().add(vec);
			}

			if (pair.getLeft().distance(pair.getRight()) < 0.1)
			{
				if (!_ruptureTime.containsKey(pair.getLeft()))
				{
					_ruptureTime.put(pair.getLeft(), System.currentTimeMillis());
				}
				else if (UtilTime.elapsed(_ruptureTime.get(pair.getLeft()), 150))
				{
					itel.remove();

					explodeRupture(pair.getLeft());
				}
			}
		}

		if (_tick % 10 == 0 && _rupturesLeft > 0)
		{
			_rupturesLeft--;

			Location loc = getLocation().add(UtilMath.random.nextFloat() - 0.5, 0, UtilMath.random.nextFloat() - 0.5);

			loc.setY(loc.getBlockY());

			for (int y = 0; y > -3; y--)
			{
				if (!UtilBlock.airFoliage(loc.getBlock().getRelative(0, y, 0)))
				{
					loc.setY(loc.getY() + y);
					break;
				}
			}

			Player player = getTarget();

			if (player != null)
			{
				_targetted.add(player.getName());

				Location target = player.getLocation();
				target.setY(loc.getY());

				_ruptures.add(Pair.create(loc, target));
				
				UtilEnt.CreatureLook(getEntity(), player.getLocation());
				
				EntityIronGolem golem = ((CraftIronGolem) getEntity()).getHandle();

				golem.world.broadcastEntityEffect(golem, (byte) 4);
			}
			else
			{
				_rupturesLeft = 0;
			}
		}

		for (Pair<Location, Location> pair : _ruptures)
		{
			pair.getLeft().getWorld().playSound(pair.getLeft(), Sound.DIG_GRAVEL, 2.5F, 0.9F);

			{
				UtilParticle.PlayParticle(ParticleType.BLOCK_DUST.getParticle(Material.DIRT, 0),
						pair.getLeft().clone().add(0, 1.1, 0), 1F, 0, 1F, 0, 70, ViewDist.NORMAL, UtilServer.getPlayers());
			}
		}

		_tick++;
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

			if (_targetted.contains(player.getName()))
			{
				continue;
			}

			double d = player.getLocation().distance(getLocation());

			if (d < 7)
			{
				continue;
			}

			boolean valid = true;

			for (Pair<Location, Location> loc : _ruptures)
			{
				if (loc.getRight().distance(player.getLocation()) < 1.5)
				{
					valid = false;
					break;
				}
			}

			if (!valid)
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
		HashMap<LivingEntity, Double> targets = UtilEnt.getInRadius(loc, 3.5);
		for (LivingEntity cur : targets.keySet())
		{
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

		List<Block> blocks = new ArrayList<>();

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
			{
				continue;
			}

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
			loc.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, Material.STONE.getId());

			done++;
		}

		_ticksFinished = 20;
	}
}