package mineplex.gemhunters.shop;

import mineplex.core.MiniPlugin;
import mineplex.core.ReflectivelyCreateMiniPlugin;
import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilTime;
import mineplex.core.google.GoogleSheetsManager;
import mineplex.core.google.SheetObjectDeserialiser;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.gemhunters.loot.deserialisers.LootItemDeserialiser;
import mineplex.gemhunters.safezone.SafezoneModule;
import mineplex.gemhunters.shop.deserialisers.VillagerPropertiesDeserialiser;
import mineplex.gemhunters.util.SlackRewardBot;
import mineplex.gemhunters.world.WorldDataModule;
import org.bukkit.Location;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;

import java.util.*;

@ReflectivelyCreateMiniPlugin
public class ShopModule extends MiniPlugin
{

	private static final String SHEET_FILE_NAME = "GEM_HUNTERS_SHOP";
	private static final String VILLAGER_MASTER_SHEET_NAME = "VILLAGER_MASTER";
	private static final VillagerPropertiesDeserialiser VILLAGER_PROPERTIES_DESERIALISER = new VillagerPropertiesDeserialiser();
	private static final LootItemDeserialiser DESERIALISER = new LootItemDeserialiser();
	private static final SheetObjectDeserialiser<Integer> COST_DESERIALISER = values -> Integer.parseInt(values[10]);

	private static final int MINIMUM_ITEMS = 1;
	private static final int MAXIMUM_ITEMS = 5;
	
	private static final String[] NAMES = {
		"Andrew", "Jon", "Bob", "Sam", "Ronan", "Alex", "Joe", "Emma", "Giovani", "Dean", "Josh", "Geoffrey", "Parker", "Spencer", "Luke", "Peter", "William", "Connor"
	};
	
	private final GoogleSheetsManager _sheets;
	private final SafezoneModule _safezone;
	private final WorldDataModule _worldData;

	private final Map<String, Set<TradeableItem>> _trades;
	private final Map<String, VillagerProperties> _properties;

	private final List<TraderNPC> _npcs;
	private final Map<String, Set<Integer>> _spawnedIndexes;

	private ShopModule()
	{
		super("Shop");

		_sheets = require(GoogleSheetsManager.class);
		_safezone = require(SafezoneModule.class);
		_worldData = require(WorldDataModule.class);

		_trades = new HashMap<>();
		_properties = new HashMap<>();

		_npcs = new ArrayList<>();
		_spawnedIndexes = new HashMap<>();

		runSyncLater(this::updateVillagerTrades, 20);
	}

	public void updateVillagerTrades()
	{
		log("Updating villager trades");
		Map<String, List<List<String>>> map = _sheets.getSheetData(SHEET_FILE_NAME);

		for (String key : map.keySet())
		{
			//TODO this is super temporary
			if (key.equals("PINK"))
			{
				continue;
			}
				
			if (key.equals(VILLAGER_MASTER_SHEET_NAME))
			{
				int row = 0;

				for (List<String> rows : map.get(key))
				{
					row++;
					try
					{
						VillagerProperties properties = VILLAGER_PROPERTIES_DESERIALISER.deserialise(rows.toArray(new String[0]));
						_properties.put(properties.getDataKey(), properties);
					}
					catch (Exception e)
					{
					}
				}
				continue;
			}

			Set<TradeableItem> items = new HashSet<>();
			int row = 0;

			for (List<String> rows : map.get(key))
			{
				row++;
				try
				{
					String[] values = rows.toArray(new String[0]);
					items.add(new TradeableItem(DESERIALISER.deserialise(values), COST_DESERIALISER.deserialise(values)));
				}
				catch (Exception e)
				{
				}
			}

			_trades.put(key, items);
		}

		log("Finished updating villager trades");
	}

	@EventHandler
	public void updateSpawnedVillagers(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
		{
			return;
		}

		Iterator<TraderNPC> iterator = _npcs.iterator();
		
		while (iterator.hasNext())
		{
			TraderNPC npc = iterator.next();
			int expireTime = npc.getProperties().getExpireRate();
			
			if (expireTime > 0 && UtilTime.elapsed(npc.getSpawnedAt(), expireTime))
			{
				npc.getEntity().remove();
				iterator.remove();
			}
		}
		
		for (String key : _properties.keySet())
		{
			List<Location> locations = _worldData.getSpawnLocation(capitalise(key));
			VillagerProperties properties = _properties.get(key);

			if (!UtilTime.elapsed(properties.getLastSpawn(), properties.getSpawnRate()))
			{
				continue;
			}

			properties.setLastSpawn();

			// Only spawn more chests if we need to
			int max = properties.getMax();
			int spawned = 0;

			for (TraderNPC npc : _npcs)
			{
				if (npc.getProperties().getDataKey().equals(key))
				{
					spawned++;
				}
			}

			// If there are too many chests of this type we can ignore it
			if (spawned > max)
			{
				continue;
			}

			Set<Integer> usedIndexes = _spawnedIndexes.get(key);

			if (usedIndexes == null)
			{
				_spawnedIndexes.put(key, new HashSet<>());
				usedIndexes = _spawnedIndexes.get(key);
			}

			if (locations.size() == usedIndexes.size())
			{
				continue;
			}

			int index = getFreeIndex(locations.size(), usedIndexes);

			if (index == -1)
			{
				return;
			}

			Location randomLocation = locations.get(index);

			randomLocation.setYaw(UtilMath.r(360));

			usedIndexes.add(index);

			String name = NAMES[UtilMath.r(NAMES.length)];

			name = (properties.isSelling() ? C.cGold + "Buy" : C.cGreen + "Sell") + C.cGray + " - " + C.cWhite + name;

			//DebugModule.getInstance().d("Trader at " + UtilWorld.locToStrClean(randomLocation) + " with key=" + key + " and index=" + index + " and max=" + spawned + "/" + max);
			if (properties.isSelling())
			{
				_npcs.add(new TraderNPC(_plugin, randomLocation, Villager.class, name, _safezone.isInSafeZone(randomLocation), properties, getRandomItemSet(_trades.get(key))));
			}
			else
			{
				new SellingNPC(_plugin, randomLocation, Villager.class, name, _safezone.isInSafeZone(randomLocation), _trades.get(key));
			}
		}
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
	
	private Set<TradeableItem> getRandomItemSet(Set<TradeableItem> items)
	{
		int size = UtilMath.rRange(MINIMUM_ITEMS, MAXIMUM_ITEMS);
		Set<TradeableItem> items2 = new HashSet<>(size);

		for (int i = 0; i < size; i++)
		{
			items2.add(UtilAlg.Random(items));
		}

		return items2;
	}
	
	private String capitalise(String s)
	{
		String right = s.toLowerCase().substring(1);
		char left = Character.toUpperCase(s.charAt(0));
	
		return left + right;
	}

}
