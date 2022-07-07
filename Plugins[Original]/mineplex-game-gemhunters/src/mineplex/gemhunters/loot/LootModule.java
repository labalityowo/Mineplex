package mineplex.gemhunters.loot;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import mineplex.core.common.util.F;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import mineplex.core.MiniPlugin;
import mineplex.core.ReflectivelyCreateMiniPlugin;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.google.GoogleSheetsManager;
import mineplex.core.stats.StatsManager;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.gemhunters.economy.EconomyModule;
import mineplex.gemhunters.economy.event.PlayerCashOutCompleteEvent;
import mineplex.gemhunters.loot.command.SpawnChestCommand;
import mineplex.gemhunters.loot.command.UpdateLootCommand;
import mineplex.gemhunters.loot.deserialisers.ChestPropertiesDeserialiser;
import mineplex.gemhunters.loot.deserialisers.LootItemDeserialiser;
import mineplex.gemhunters.loot.event.PlayerChestOpenEvent;
import mineplex.gemhunters.loot.rewards.LootChestReward;
import mineplex.gemhunters.loot.rewards.LootGadgetReward;
import mineplex.gemhunters.loot.rewards.LootItemReward;
import mineplex.gemhunters.loot.rewards.LootRankReward;
import mineplex.gemhunters.loot.rewards.LootShardReward;
import mineplex.gemhunters.safezone.SafezoneModule;
import mineplex.gemhunters.spawn.event.PlayerTeleportIntoMapEvent;
import mineplex.gemhunters.world.WorldDataModule;

@ReflectivelyCreateMiniPlugin
public class LootModule extends MiniPlugin
{
	public enum Perm implements Permission
	{
		SPAWN_CHEST_COMMAND,
		UPDATE_LOOT_COMMAND,
	}

	private static final String SHEET_FILE_NAME = "GEM_HUNTERS_CHESTS";
	private static final String CHEST_MASTER_SHEET_NAME = "CHEST_MASTER";
	private static final long CHEST_DESPAWN_TIME_OPENED = TimeUnit.SECONDS.toMillis(15);
	private static final float CHESTS_ON_START_FACTOR = 0.333F;
	private static final int MAX_SEARCH_ATTEMPTS = 40;
	private static final int MAX_CHEST_CHECK_DISTANCE_SQUARED = 4;
	private static final LootItemDeserialiser DESERIALISER = new LootItemDeserialiser();
	private static final ChestPropertiesDeserialiser CHEST_DESERIALISER = new ChestPropertiesDeserialiser();
	private static final ItemStack[] SPAWN_ITEMS =
	{
		new ItemStack(Material.WOOD_SWORD),
		new ItemStack(Material.APPLE, 3),
	};
	private static final String GEM_METADATA = "GEM";
	
	private final EconomyModule _economy;
	private final GoogleSheetsManager _sheets;
	private final SafezoneModule _safezone;
	private final StatsManager _stats;
	private final WorldDataModule _worldData;

	private final Map<String, Set<LootItem>> _chestLoot;
	private final Map<String, ChestProperties> _chestProperties;
	private final Set<SpawnedChest> _spawnedChest;
	private final Set<LootItemReward> _itemRewards;
	private final Set<UUID> _shownPlayers;
	
	private LootModule()
	{
		super("Loot");

		_economy = require(EconomyModule.class);
		_sheets = require(GoogleSheetsManager.class);
		_safezone = require(SafezoneModule.class);
		_stats = require(StatsManager.class);
		_worldData = require(WorldDataModule.class);
		_chestLoot = new HashMap<>();
		_chestProperties = new HashMap<>();
		_spawnedChest = new HashSet<>();
		_itemRewards = new HashSet<>();
		_shownPlayers = new HashSet<>();
		
		runSyncLater(() ->
		{
			updateChestLoot(); 
			
			// Spawn some chests
			for (String key : _chestProperties.keySet())
			{
				int max = _chestProperties.get(key).getMaxActive();
				
				for (int i = 0; i < max * CHESTS_ON_START_FACTOR; i++)
				{
					addSpawnedChest(key, true);					
				}
			}
		}, 20);
		
		generatePermissions();
	}
	
	private void generatePermissions()
	{

		PermissionGroup.ADMIN.setPermission(Perm.SPAWN_CHEST_COMMAND, true, true);
		PermissionGroup.ADMIN.setPermission(Perm.UPDATE_LOOT_COMMAND, true, true);
	}

	@Override
	public void addCommands()
	{
		addCommand(new UpdateLootCommand(this));
		addCommand(new SpawnChestCommand(this));
	}

	@EventHandler
	public void updateSpawnChests(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
		{
			return;
		}

		// Despawn opened chests
		Iterator<SpawnedChest> iterator = _spawnedChest.iterator();

		while (iterator.hasNext())
		{
			SpawnedChest chest = iterator.next();
			ChestProperties properties = chest.getProperties();

			if (chest.isOpened() && UtilTime.elapsed(chest.getOpenedAt(), CHEST_DESPAWN_TIME_OPENED) || UtilTime.elapsed(chest.getSpawnedAt(), properties.getExpireRate()))
			{
				if (chest.getID() != -1)
				{
					properties.getSpawnIndexes().put(chest.getID(), properties.getSpawnIndexes().get(chest.getID()) - 1);
				}

				Block block = chest.getLocation().getBlock();

				if (block.getState() instanceof Chest)
				{
					((Chest) block.getState()).getBlockInventory().clear();
				}

				block.getWorld().playEffect(chest.getLocation(), Effect.STEP_SOUND, block.getType());
				block.setType(Material.AIR);
				iterator.remove();
			}
		}

		// Spawn new chests
		for (String key : _chestProperties.keySet())
		{
			addSpawnedChest(key, false);
		}
	}

	public boolean isSuitable(Block block)
	{
		Block up = block.getRelative(BlockFace.UP);
		Block down = block.getRelative(BlockFace.DOWN);

		if (block.getType() != Material.AIR || up.getType() != Material.AIR || down.getType() == Material.AIR || UtilBlock.liquid(down) || UtilBlock.liquid(up) || UtilBlock.liquid(block) || _safezone.isInSafeZone(block.getLocation()))
		{
			return false;
		}
		
		return true;
	}

	public void updateChestLoot()
	{
		log("Updating chest loot");
		Map<String, List<List<String>>> map = _sheets.getSheetData(SHEET_FILE_NAME);

		for (String key : map.keySet())
		{
			if (key.equals(CHEST_MASTER_SHEET_NAME))
			{
				int row = 0;

				for (List<String> rows : map.get(key))
				{
					row++;
					try
					{
						ChestProperties properties = CHEST_DESERIALISER.deserialise(rows.toArray(new String[0]));
						_chestProperties.put(properties.getDataKey(), properties);
					}
					catch (Exception e)
					{
					}
				}

				continue;
			}

			Set<LootItem> items = new HashSet<>();
			int row = 0;

			for (List<String> rows : map.get(key))
			{
				row++;
				try
				{
					items.add(DESERIALISER.deserialise(rows.toArray(new String[0])));
				}
				catch (Exception e)
				{
				}
			}

			_chestLoot.put(key, items);
		}

		log("Finished updating chest loot");
	}

	public void addSpawnedChest(String key, boolean force)
	{
		if (key.equals("PURPLE") && Bukkit.getOnlinePlayers().size() < 10)
		{
			return;
		}
		
		List<Location> locations = _worldData.getDataLocation(key);
		ChestProperties properties = _chestProperties.get(key);

		if (!force && !UtilTime.elapsed(properties.getLastSpawn(), properties.getSpawnRate()))
		{
			return;
		}
		
		properties.setLastSpawn();

		// Only spawn more chests if we need to
		int max = properties.getMaxActive();
		int spawned = 0;

		for (SpawnedChest chest : _spawnedChest)
		{
			if (chest.getProperties().getDataKey().equals(key))
			{
				spawned++;
			}
		}

		// If there are too many chests of this type we can ignore it
		if (spawned > max)
		{
			return;
		}
		
		if (locations.isEmpty())
		{
			return;
		}

		Map<Integer, Integer> spawnedIndexes = properties.getSpawnIndexes();
		Location randomLocation = null;
		boolean found = false;
		int attempts = 0;
		int index = -1;
		
		while (index == -1 || !found && attempts < MAX_SEARCH_ATTEMPTS)
		{
			attempts++;
			index = UtilMath.r(locations.size());

			if (spawnedIndexes.getOrDefault(index, 0) >= properties.getMaxChestPerLocation())
			{
				continue;
			}
		}

		if (index == -1)
		{
			return;
		}

		spawnedIndexes.put(index, spawnedIndexes.getOrDefault(index, 0) + 1);
		randomLocation = locations.get(index);

		int placeRadius = properties.getSpawnRadius();
		Location chestToPlace = UtilAlg.getRandomLocation(randomLocation, placeRadius, 0, placeRadius);
		Block block = chestToPlace.getBlock();

		attempts = 0;
		boolean suitable = false;

		while (!suitable && attempts < MAX_SEARCH_ATTEMPTS)
		{
			chestToPlace = UtilAlg.getRandomLocation(randomLocation, placeRadius, 0, placeRadius);
			block = chestToPlace.getBlock();
			suitable = isSuitable(block);
			attempts++;
		}

		//Bukkit.broadcastMessage("Spawned at " + UtilWorld.blockToStrClean(block) + " with key=" + key + " and index=" + index + " and max=" + spawned + "/" + max + " and suitable=" + suitable);
		
		if (!suitable)
		{
			return;
		}

		_spawnedChest.add(new SpawnedChest(chestToPlace, properties, index));
		block.setType(properties.getBlockMaterial());
	}

	public void addSpawnedChest(Location location, String colour)
	{
		_spawnedChest.add(new SpawnedChest(location, _chestProperties.get(colour), -1));
	}

	public void fillChest(Player player, Block block, String key)
	{
		Set<Integer> used = new HashSet<>();
		Set<LootItem> items = _chestLoot.get(key);
		ChestProperties properties = _chestProperties.get(key);

		Inventory inventory = null;

		if (block.getType() == Material.ENDER_CHEST)
		{
			inventory = player.getEnderChest();
		}
		else
		{
			BlockState state = block.getState();
			Chest chest = (Chest) state;
			inventory = chest.getBlockInventory();
		}

		inventory.clear();

		for (int i = 0; i < UtilMath.rRange(properties.getMinAmount(), properties.getMaxAmount()); i++)
		{
			LootItem lootItem = getRandomItem(items);
			ItemStack itemStack = lootItem.getItemStack();
			int index = getFreeIndex(inventory.getSize(), used);

			inventory.setItem(index, itemStack);
		}
	}

	public LootItem getRandomItem(Set<LootItem> items)
	{
		double totalWeight = 0;

		for (LootItem item : items)
		{
			totalWeight += item.getProbability();
		}

		double select = Math.random() * totalWeight;

		for (LootItem item : items)
		{
			if ((select -= item.getProbability()) <= 0)
			{
				return item;
			}
		}

		return null;
	}

	private int getFreeIndex(int endIndex, Set<Integer> used)
	{
		int index = -1;

		while (index == -1 || used.contains(index))
		{
			index = UtilMath.r(endIndex);
		}

		used.add(index);

		return index;
	}

	public LootItem fromItemStack(ItemStack itemStack)
	{
		if (itemStack == null)
		{
			return null;
		}
		
		for (Set<LootItem> items : _chestLoot.values())
		{
			for (LootItem item : items)
			{
				if (item.getItemStack().isSimilar(itemStack))
				{
					return item;
				}
			}
		}

		return null;
	}

	public boolean hasChestBeenOpened(Location location)
	{
		for (SpawnedChest chest : _spawnedChest)
		{
			if (chest.getLocation().distanceSquared(location) < MAX_CHEST_CHECK_DISTANCE_SQUARED && chest.isOpened())
			{
				return true;
			}
		}

		return false;
	}

	@EventHandler
	public void chestOpen(PlayerInteractEvent event)
	{
		if (event.isCancelled() || !UtilEvent.isAction(event, ActionType.R_BLOCK))
		{
			return;
		}

		Player player = event.getPlayer();
		Block block = event.getClickedBlock();

		if (block.getType() != Material.CHEST && block.getType() != Material.ENDER_CHEST)
		{
			return;
		}

		if (hasChestBeenOpened(block.getLocation()))
		{
			return;
		}

		String key = null;

		for (SpawnedChest chest : _spawnedChest)
		{
			if (UtilMath.offsetSquared(chest.getLocation(), block.getLocation()) < MAX_CHEST_CHECK_DISTANCE_SQUARED)
			{
				key = chest.getProperties().getDataKey();
				chest.setOpened();
				break;
			}
		}

		if (key == null)
		{
			event.setCancelled(true);
			return;
		}

		PlayerChestOpenEvent openEvent = new PlayerChestOpenEvent(player, block, _chestProperties.get(key));
		UtilServer.CallEvent(openEvent);
		
		if (openEvent.isCancelled())
		{
			event.setCancelled(true);
			return;
		}

		_stats.incrementStat(player, "Gem Hunters.ChestsOpened", 1);
		fillChest(player, block, key);
	}

	@EventHandler
	public void inventoryClick(InventoryClickEvent event)
	{
		if (event.getClickedInventory() == null)
		{
			return;
		}

		ItemStack itemStack = event.getCurrentItem();

		if (itemStack == null)
		{
			return;
		}

		handleRewardItem((Player) event.getWhoClicked(), itemStack);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void pickupItem(PlayerPickupItemEvent event)
	{
		if (event.getItem() == null || event.isCancelled())
		{
			return;
		}

		handleRewardItem(event.getPlayer(), event.getItem().getItemStack());
	}

	@EventHandler
	public void dropItem(PlayerDropItemEvent event)
	{
		if (event.getItemDrop() == null || event.isCancelled())
		{
			return;
		}

		if (!_safezone.isInSafeZone(event.getPlayer()))
		{
			return;
		}

		ItemStack dropped = event.getItemDrop().getItemStack();

		for (LootItemReward storedReward : _itemRewards)
		{
			if (storedReward.getItemStack().isSimilar(dropped))
			{
				event.setCancelled(true);
				event.getItemDrop().remove();
				event.getPlayer().sendMessage(F.main(_moduleName, "You can't drop special items in Safezones."));
				return;
			}
		}
	}

	public void handleRewardItem(Player player, ItemStack itemStack)
	{
		LootItem lootItem = fromItemStack(itemStack);
		LootItemReward reward = null;

		for (LootItemReward storedReward : _itemRewards)
		{
			if (storedReward.getItemStack().isSimilar(itemStack))
			{
				reward = storedReward;
			}
		}

		if (reward == null && lootItem != null && lootItem.getMetadata() != null)
		{
			String[] metadataSplit = lootItem.getMetadata().split(" ");
			String key = metadataSplit[0];
			String[] values = new String[metadataSplit.length - 1];

			System.arraycopy(metadataSplit, 1, values, 0, metadataSplit.length - 1);

			switch (key)
			{
				case "RANK_UPGRADE":
					reward = new LootRankReward(itemStack);
					break;
				case "SHARD":
					reward = new LootShardReward(Integer.parseInt(values[0]) * 1000, itemStack, Integer.parseInt(values[1]));
					break;
				case "CHEST":
					reward = new LootChestReward(Integer.parseInt(values[0]) * 1000, itemStack, values[1], Integer.parseInt(values[2]));
					break;
				case "GADGET":
					String gadget = "";

					for (int i = 1; i < values.length; i++)
					{
						gadget += values[i] + " ";
					}

					reward = new LootGadgetReward(Integer.parseInt(values[0]) * 1000, itemStack, gadget.trim());
					break;
				default:
					return;
			}

			_itemRewards.add(reward);
		}

		if (reward != null)
		{
			reward.collectItem(player);
		}
	}
	
	public void addItemReward(LootItemReward reward)
	{
		_itemRewards.add(reward);
	}
	
	@EventHandler
	public void gemClick(PlayerInteractEvent event)
	{
		if (!UtilEvent.isAction(event, ActionType.R))
		{
			return;
		}

		Player player = event.getPlayer();
		ItemStack itemStack = player.getItemInHand();

		if (itemStack == null)
		{
			return;
		}

		LootItem lootItem = fromItemStack(itemStack);

		if (lootItem == null || lootItem.getMetadata() == null || !lootItem.getMetadata().startsWith(GEM_METADATA))
		{
			return;
		}
		
		player.setItemInHand(UtilInv.decrement(itemStack));
		
		int amount = Integer.parseInt(lootItem.getMetadata().split(" ")[1]);
		
		_economy.addToStore(player, "Gem Item", amount);
	}
	
	@EventHandler
	public void mapUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
		{
			return;
		}

		_shownPlayers.clear();

		for (Player player : Bukkit.getOnlinePlayers())
		{
			UUID key = player.getUniqueId();

			for (LootItemReward itemReward : _itemRewards)
			{
				if (itemReward.getPlayer() == null)
				{
					continue;
				}

				if (itemReward.getPlayer().equals(player))
				{
					_shownPlayers.add(key);
					break;
				}
			}
		}
	}

	@EventHandler
	public void mapTeleport(PlayerTeleportIntoMapEvent event)
	{
		event.getPlayer().getInventory().addItem(SPAWN_ITEMS);
	}

	@EventHandler
	public void cashOutComplete(PlayerCashOutCompleteEvent event)
	{
		Player player = event.getPlayer();
		Iterator<LootItemReward> iterator = _itemRewards.iterator();

		while (iterator.hasNext())
		{
			LootItemReward reward = iterator.next();

			if (reward.getPlayer() != null && player.equals(reward.getPlayer()))
			{
				reward.success();
				iterator.remove();

				for (Entity entity : reward.getPlayer().getWorld().getEntities())
				{
					if (entity instanceof Item)
					{
						Item item = (Item) entity;

						if (item.getItemStack().getType() == reward.getItemStack().getType())
						{
							entity.remove();
						}
					}
				}
			}
		}
	}

	@EventHandler
	public void playerDeath(PlayerDeathEvent event)
	{
		for (LootItemReward reward : _itemRewards)
		{
			if (reward.getPlayer() == null)
			{
				continue;
			}
			if (reward.getPlayer().equals(event.getEntity()))
			{
				reward.death(event);
			}
		}
	}

	public final Set<UUID> getShownPlayers()
	{
		return _shownPlayers;
	}
	
	public final Set<LootItem> getChestItems(String key)
	{
		return _chestLoot.get(key);
	}
}
