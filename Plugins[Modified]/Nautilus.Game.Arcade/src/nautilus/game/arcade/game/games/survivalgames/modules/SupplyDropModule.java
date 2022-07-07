package nautilus.game.arcade.game.games.survivalgames.modules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEnderDragon;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import mineplex.core.blockrestore.BlockRestore;
import mineplex.core.common.Pair;
import mineplex.core.common.util.C;
import mineplex.core.common.util.MapUtil;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilFirework;
import mineplex.core.common.util.UtilItem;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.mission.MissionTrackerType;
import mineplex.core.treasure.util.TreasureUtil;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.Game.GameState;
import nautilus.game.arcade.game.games.survivalgames.SurvivalGamesNew;
import nautilus.game.arcade.game.modules.Module;

public class SupplyDropModule extends Module
{

	public static final int TIME = 14000;
	private static final long DURATION = TimeUnit.SECONDS.toMillis(60);
	private static final int HEIGHT = 40;
	private static final int RADIUS = 8;
	private static final int POINTS = 80;
	private static final int MAX_DROPS = 3;
	private static final FireworkEffect FIREWORK_EFFECT = FireworkEffect.builder()
			.with(Type.BURST)
			.withColor(Color.YELLOW)
			.build();

	private static final int[][] LOOT_CONFIGURATIONS =
			{
					{
							1, 1, 2
					},
					{
							1, 1, 3
					},
					{
							1, 2, 2
					},
					{
							2, 2
					},
					{
							2, 3
					}
			};
	private final Map<Integer, List<ItemStack>> _lootTable;
	private final Inventory _chestInventory;
	private SupplyDrop _supplyDrop;
	private Block _lastBlock;
	private boolean _firstOpening;
	private int _totalDrops;

	public SupplyDropModule()
	{
		_lootTable = new HashMap<>(3);
		_chestInventory = Bukkit.createInventory(null, 27, "Supply Drop");
	}

	@Override
	public void cleanup()
	{
		_chestInventory.clear();
		_supplyDrop = null;
		_lastBlock = null;
	}

	@EventHandler
	public void prepare(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Prepare)
		{
			return;
		}

		((SurvivalGamesNew) getGame()).setupSupplyDropLoot(_lootTable);
	}

	@EventHandler
	public void update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK || !getGame().IsLive())
		{
			return;
		}

		if (_supplyDrop == null)
		{
			if (_totalDrops < MAX_DROPS && getGame().WorldTimeSet > TIME && getGame().WorldTimeSet < TIME + 1000)
			{
				Location location = getRandom();

				if (location == null)
				{
					return;
				}

				BlockRestore restore = getGame().getArcadeManager().GetBlockRestore();
				long duration = DURATION + TimeUnit.SECONDS.toMillis(10);
				for (Pair<Location, Pair<Material, Byte>> pair : UtilBlock.getBeaconBlocks(location, (byte) 4))
				{
					restore.add(pair.getLeft().getBlock(), pair.getRight().getLeft().getId(), pair.getRight().getRight(), duration);
				}

				getGame().CreatureAllowOverride = true;

				LivingEntity entity = location.getWorld().spawn(location.add(0, HEIGHT, 0), EnderDragon.class);

				entity.setRemoveWhenFarAway(false);
				UtilEnt.setTickWhenFarAway(entity, true);
				UtilEnt.vegetate(entity, true);
				UtilEnt.ghost(entity, true, false);

				getGame().CreatureAllowOverride = false;

				getGame().Announce(C.cRedB + "A Supply Drop has appeared at " + C.cWhite + "(" + location.getBlockX() + ", " + location.getBlockZ() + ").");

				_supplyDrop = new SupplyDrop(location, entity);
				_totalDrops++;
			}
		}
		else if (!_supplyDrop.Dropped)
		{
			if (UtilTime.elapsed(_supplyDrop.Start, DURATION))
			{
				_supplyDrop.Dropped = true;
				((CraftEnderDragon) _supplyDrop.Dragon).getHandle().setTargetBlock(256, 100, 256);
				byte data = (byte) (UtilMath.r(4) + 2);

				getGame().getArcadeManager().runSyncTimer(new BukkitRunnable()
				{
					Block block = _supplyDrop.Drop.getBlock();
					boolean done = false;

					@Override
					public void run()
					{
						MapUtil.QuickChangeBlockAt(block.getLocation(), Material.AIR);
						block = block.getRelative(BlockFace.DOWN);

						if (UtilBlock.solid(block))
						{
							block = block.getRelative(BlockFace.UP);
							done = true;
							_supplyDrop.Dragon.remove();
							_firstOpening = true;
							cancel();
						}

						UtilFirework.playFirework(block.getLocation().add(0.5, 0.5, 0.5), FIREWORK_EFFECT);
						MapUtil.QuickChangeBlockAt(block.getLocation(), Material.ENDER_CHEST, data);

						if (done)
						{
							_supplyDrop.ChestSpawned = true;
							_lastBlock = block;
							populateChest();
						}
					}
				}, 0, 5);
			}
			else if (!_supplyDrop.ChestSpawned)
			{
				Location center = _supplyDrop.Drop;
				double theta = Math.PI * 2 / POINTS * _supplyDrop.Point;
				double x = RADIUS * Math.cos(theta), z = RADIUS * Math.sin(theta);
				float yaw = 360 / POINTS * _supplyDrop.Point - 90;

				((CraftEntity) _supplyDrop.Dragon).getHandle().setPositionRotation(center.getX() + x, center.getY(), center.getZ() + z, yaw, 0);

				_supplyDrop.Point++;
				_supplyDrop.Point %= POINTS;
				_supplyDrop.Drop.setYaw(yaw);
			}
		}
	}

	@EventHandler
	public void openChest(PlayerInteractEvent event)
	{
		if (!UtilEvent.isAction(event, ActionType.R_BLOCK))
		{
			return;
		}

		Player player = event.getPlayer();
		Block block = event.getClickedBlock();

		if (UtilPlayer.isSpectator(player) || !block.equals(_lastBlock))
		{
			return;
		}

		event.setCancelled(true);
		TreasureUtil.playChestOpen(block.getLocation(), true);
		player.openInventory(_chestInventory);

		if (_firstOpening)
		{
			_supplyDrop = null;
			_firstOpening = false;
			getGame().AddStat(player, "SupplyDropsOpened", 1, false, false);
			getGame().getArcadeManager().getMissionsManager().incrementProgress(player, 1, MissionTrackerType.SG_SUPPLY_DROP_OPEN, getGame().GetType().getDisplay(), null);
		}
	}

	@EventHandler
	public void updateParticles(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTEST || _supplyDrop == null || !_supplyDrop.ChestSpawned)
		{
			return;
		}

		UtilParticle.PlayParticleToAll(ParticleType.SPELL, _lastBlock.getLocation().add(0.5, 0.5, 0.5), 1, 1, 1, 0, 5, ViewDist.NORMAL);
	}

	private void populateChest()
	{
		List<Integer> slots = new ArrayList<>(_chestInventory.getSize());

		for (int i = 0; i < _chestInventory.getSize(); i++)
		{
			slots.add(i);
		}

		Map<Integer, List<ItemStack>> lootTable = new HashMap<>(_lootTable.size());

		_lootTable.forEach((integer, itemStacks) -> lootTable.put(integer, new ArrayList<>(itemStacks)));
		_chestInventory.clear();

		int[] configuration = UtilMath.randomElement(LOOT_CONFIGURATIONS);

		for (int i : configuration)
		{
			List<ItemStack> itemStacks = lootTable.get(i);
			ItemStack itemStack = UtilAlg.Random(itemStacks);
			UtilItem.makeUnbreakable(itemStack);
			itemStacks.remove(itemStack);
			int slot = UtilMath.r(slots.size());
			slots.remove(slot);

			_chestInventory.setItem(slot, itemStack);
		}
	}

	private Location getRandom()
	{
		double size = getGame().WorldData.World.getWorldBorder().getSize() / 3;
		Location center = getGame().GetSpectatorLocation();

		int attempts = 0;

		attemptsLoop:
		while (attempts++ < 50)
		{
			Location location = UtilAlg.getRandomLocation(center, size, 0, size);
			Block block = location.getBlock();

			while (block.getLocation().getY() > 0 && !UtilBlock.solid(block))
			{
				if (block.isLiquid())
				{
					continue attemptsLoop;
				}

				block = block.getRelative(BlockFace.DOWN);
			}

			for (BlockFace face : UtilBlock.horizontals)
			{
				if (!UtilBlock.solid(block.getRelative(face)))
				{
					continue attemptsLoop;
				}
			}

			block = block.getRelative(BlockFace.UP);
			Block bottom = block;

			while (block.getLocation().getY() < getGame().WorldData.MaxY)
			{
				if (UtilBlock.solid(block))
				{
					continue attemptsLoop;
				}

				block = block.getRelative(BlockFace.UP);
			}

			return bottom.getLocation().add(0.5, 0, 0.5);
		}

		return null;
	}

	private class SupplyDrop
	{
		Location Drop;
		boolean Dropped, ChestSpawned;
		long Start;
		Entity Dragon;
		int Point;

		SupplyDrop(Location drop, Entity dragon)
		{
			Drop = drop;
			Start = System.currentTimeMillis();
			Dragon = dragon;
		}
	}

	public Location getCurrentDrop()
	{
		return _supplyDrop == null ? null : _supplyDrop.Drop;
	}
}
