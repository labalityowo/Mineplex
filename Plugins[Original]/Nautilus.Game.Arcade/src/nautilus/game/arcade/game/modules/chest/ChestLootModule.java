package nautilus.game.arcade.game.modules.chest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.MapUtil;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.mission.MissionTrackerType;
import mineplex.core.titles.tracks.standard.LuckyTrack;
import mineplex.core.titles.tracks.standard.UnluckyTrack;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.events.ChestRefillEvent;
import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.Game.GameState;
import nautilus.game.arcade.game.modules.Module;

public class ChestLootModule extends Module
{

	private final Map<ChestType, Set<ChestMetadata>> _chests;

	private long _destroyAfterOpened;
	private boolean _autoRotateChests = true, _spawnNearby, _preGenerateLoot;
	private int _spawnNearbyRadius = 8;

	public ChestLootModule()
	{
		_chests = new HashMap<>();
	}

	public ChestLootModule registerChestType(String name, List<Location> chestLocations, ChestLootPool... pools)
	{
		return registerChestType(name, chestLocations, 1, pools);
	}

	public ChestLootModule registerChestType(String name, List<Location> chestLocations, double spawnChance, ChestLootPool... pools)
	{
		_chests.put(new ChestType(name, chestLocations, spawnChance, pools), new HashSet<>());
		return this;
	}

	public ChestLootModule destroyAfterOpened(int seconds)
	{
		_destroyAfterOpened = TimeUnit.SECONDS.toMillis(seconds);
		return this;
	}

	public ChestLootModule autoRotateChests(boolean autoRotate)
	{
		_autoRotateChests = autoRotate;
		return this;
	}

	public ChestLootModule spawnNearbyDataPoints()
	{
		_spawnNearby = true;
		return this;
	}

	public ChestLootModule spawnNearbyDataPoints(int radius)
	{
		_spawnNearby = true;
		_spawnNearbyRadius = radius;
		return this;
	}

	public ChestLootModule setPreGenerateLoot(boolean preGenerateLoot)
	{
		_preGenerateLoot = preGenerateLoot;
		return this;
	}

	public void addChestLocation(String typeName, Location location)
	{
		for (Entry<ChestType, Set<ChestMetadata>> entry : _chests.entrySet())
		{
			if (!entry.getKey().Name.equals(typeName))
			{
				continue;
			}

			entry.getValue().add(new ChestMetadata(location.getBlock(), entry.getKey()));
			return;
		}
	}

	public void refill()
	{
		_chests.forEach((type, metadataSet) -> metadataSet.forEach(metadata -> metadata.Opened = false));

		List<Location> chests = new ArrayList<>();
		_chests.values().forEach(set -> set.forEach(chestMetadata -> chests.add(chestMetadata.Chest.getLocation())));

		UtilServer.CallEvent(new ChestRefillEvent(chests));
	}

	public void refill(String typeName)
	{
		_chests.forEach((type, metadataSet) ->
		{
			if (!type.Name.equals(typeName))
			{
				return;
			}

			metadataSet.forEach(metadata -> metadata.Opened = false);
			UtilServer.CallEvent(new ChestRefillEvent(metadataSet.stream()
					.map(chestMetadata -> chestMetadata.Chest.getLocation())
					.collect(Collectors.toList())));
		});
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void populateChests(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Prepare)
		{
			return;
		}

		for (Entry<ChestType, Set<ChestMetadata>> entry : _chests.entrySet())
		{
			ChestType chestType = entry.getKey();

			if (chestType.ChestSpawns == null)
			{
				continue;
			}

			Set<ChestMetadata> metadataSet = entry.getValue();

			for (Location location : chestType.ChestSpawns)
			{
				if (chestType.SpawnChance == 1 || Math.random() < chestType.SpawnChance)
				{
					Block block = location.getBlock();

					if (_spawnNearby)
					{
						Location nearby = getNearbyLocation(location);

						if (nearby == null)
						{
							continue;
						}

						block = nearby.getBlock();
					}

					block.setType(Material.CHEST);

					if (_autoRotateChests)
					{
						List<BlockFace> faces = new ArrayList<>(UtilBlock.horizontals.size());

						for (BlockFace face : UtilBlock.horizontals)
						{
							if (UtilBlock.airFoliage(block.getRelative(face)))
							{
								faces.add(face);
							}
						}

						block.setData(getData(UtilAlg.Random(faces)));
					}

					ChestMetadata metadata = new ChestMetadata(block, chestType);
					metadataSet.add(metadata);

					if (_preGenerateLoot)
					{
						metadata.populateChest((Chest) block.getState());
					}
				}
				else
				{
					MapUtil.QuickChangeBlockAt(location, Material.AIR);
				}
			}

			_chests.put(chestType, metadataSet);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void openChest(PlayerInteractEvent event)
	{
		Block block = event.getClickedBlock();

		if (event.isCancelled() || !UtilEvent.isAction(event, ActionType.R_BLOCK) || block == null || !(block.getState() instanceof Chest))
		{
			return;
		}

		ChestMetadata metadata = getFromBlock(block);

		if (metadata == null || metadata.Opened)
		{
			return;
		}

		Chest chest = (Chest) block.getState();
		Player player = event.getPlayer();
		ArcadeManager manager = getGame().getArcadeManager();
		getGame().AddStat(player, "ChestsOpened", 1, false, false);
		getGame().getArcadeManager().getMissionsManager().incrementProgress(player, 1, MissionTrackerType.GAME_CHEST_OPEN, getGame().GetType().getDisplay(), metadata.Type.Name);

		if (manager.IsRewardStats())
		{
			manager.getTrackManager().getTrack(LuckyTrack.class).handleLoot(player, chest.getBlockInventory());
			manager.getTrackManager().getTrack(UnluckyTrack.class).handleLoot(player, chest.getBlockInventory());
		}

		metadata.Opened = true;
		metadata.OpenedAt = System.currentTimeMillis();

		if (!_preGenerateLoot)
		{
			metadata.populateChest(chest);
		}
	}

	@EventHandler
	public void destroyOpenedChests(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC || _destroyAfterOpened == 0)
		{
			return;
		}

		for (Set<ChestMetadata> metadataSet : _chests.values())
		{
			metadataSet.removeIf(metadata ->
			{
				if (metadata.Opened && UtilTime.elapsed(metadata.OpenedAt, _destroyAfterOpened))
				{
					Block block = metadata.Chest;
					Location location = block.getLocation();
					location.getWorld().playEffect(location.add(0.5, 0.5, 0.5), Effect.STEP_SOUND, block.getType());
					if (block.getType() == Material.CHEST)
					{
						((Chest) block.getState()).getBlockInventory().clear();
					}
					MapUtil.QuickChangeBlockAt(location, Material.AIR);
					return true;
				}

				return false;
			});
		}
	}

	public ItemStack getRandomItem(String chestTypeName)
	{
		for (ChestType chestType1 : _chests.keySet())
		{
			if (!chestType1.Name.equals(chestTypeName))
			{
				continue;
			}

			ChestLootPool pool = UtilAlg.Random(chestType1.Pools);

			if (pool == null)
			{
				return null;
			}

			return pool.getRandomItem();
		}

		return null;
	}

	private ChestMetadata getFromBlock(Block block)
	{
		for (Set<ChestMetadata> metadataSet : _chests.values())
		{
			for (ChestMetadata metadata : metadataSet)
			{
				if (metadata.Chest.getLocation().getBlock().equals(block))
				{
					return metadata;
				}
			}
		}

		return null;
	}

	private byte getData(BlockFace face)
	{
		if (face == null)
		{
			return 0;
		}

		switch (face)
		{
			case NORTH:
				return 0;
			case SOUTH:
				return 3;
			case WEST:
				return 4;
			case EAST:
				return 5;
		}

		return 0;
	}

	private Location getNearbyLocation(Location center)
	{
		int attempts = 0;
		while (attempts++ < 20)
		{
			Location newLocation = UtilAlg.getRandomLocation(center, _spawnNearbyRadius, 1, _spawnNearbyRadius);

			if (isSuitable(newLocation.getBlock()))
			{
				return newLocation;
			}
		}

		return null;
	}

	private boolean isSuitable(Block block)
	{
		Block up = block.getRelative(BlockFace.UP);
		Block down = block.getRelative(BlockFace.DOWN);

		return block.getType() == Material.AIR && up.getType() == Material.AIR && down.getType() != Material.AIR && !UtilBlock.liquid(down) && !UtilBlock.liquid(up) && !UtilBlock.liquid(block);
	}

	private class ChestMetadata
	{

		Block Chest;
		ChestType Type;
		long OpenedAt;
		boolean Opened;

		ChestMetadata(Block chest, ChestType type)
		{
			Chest = chest;
			Type = type;
		}

		void populateChest(Chest chest)
		{
			Inventory inventory = chest.getBlockInventory();
			inventory.clear();
			List<Integer> slots = new ArrayList<>(chest.getBlockInventory().getSize());

			for (int i = 0; i < inventory.getSize(); i++)
			{
				slots.add(i);
			}

			for (ChestLootPool pool : Type.Pools)
			{
				if (pool.getProbability() == 1 || Math.random() < pool.getProbability())
				{
					pool.populateChest(chest, slots);
				}
			}
		}
	}

	private class ChestType
	{
		String Name;
		double SpawnChance;
		List<ChestLootPool> Pools;
		List<Location> ChestSpawns;

		ChestType(String name, List<Location> chestLocations, double spawnChance, ChestLootPool... pools)
		{
			Name = name;
			SpawnChance = spawnChance;
			Pools = Arrays.asList(pools);
			ChestSpawns = chestLocations;
		}
	}
}
