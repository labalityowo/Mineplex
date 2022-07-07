package mineplex.gemhunters.persistence;

import java.lang.reflect.Constructor;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.google.gson.Gson;

import mineplex.core.account.CoreClient;
import mineplex.core.common.util.UtilServer;
import mineplex.gemhunters.quest.QuestPlayerData;
import mineplex.serverdata.Region;
import mineplex.serverdata.database.DBPool;
import mineplex.serverdata.database.RepositoryBase;
import mineplex.serverdata.database.column.ColumnInt;
import mineplex.serverdata.database.column.ColumnTimestamp;
import mineplex.serverdata.database.column.ColumnVarChar;

public class PersistenceRepository extends RepositoryBase
{

	private static final String GET_DATA = "SELECT * FROM gemHunters WHERE accountId=? AND region=?;";
	private static final String INSERT_DATA = "INSERT INTO gemHunters (accountId, region, gems, health, maxHealth, hunger, x, y, z, yaw, pitch, quests, slots, items, armour, saveTime, cashOutTime) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);";
	private static final String UPDATE_DATA = "UPDATE gemHunters SET gems=?,health=?,maxHealth=?,hunger=?,x=?,y=?,z=?,yaw=?,pitch=?,quests=?,slots=?,items=?,armour=?,saveTime=?,cashOutTime=? WHERE accountId=? AND region=?;";
	private static final String DELETE_DATA = "DELETE FROM gemHunters WHERE accountId=?;";
	private static final Gson GSON;
	private static final ItemStack AIR = new ItemStack(Material.AIR);

	static
	{
		GSON = new Gson();
	}

	private final List<Integer> _exists;

	public PersistenceRepository()
	{
		super(DBPool.getAccount());

		_exists = new ArrayList<>();
	}

	public void getPersistenceData(Consumer<PersistenceData> response, CoreClient client)
	{
		int accountId = client.getAccountId();
		Region region = UtilServer.getRegion();

		executeQuery(GET_DATA, resultSet ->
		{
			if (resultSet.next())
			{
				int gems = resultSet.getInt("gems");
				int health = resultSet.getInt("health");
				int maxHealth = resultSet.getInt("maxHealth");
				int hunger = resultSet.getInt("hunger");
				int x = resultSet.getInt("x");
				int y = resultSet.getInt("y");
				int z = resultSet.getInt("z");
				int yaw = resultSet.getInt("yaw");
				int pitch = resultSet.getInt("pitch");
				int slots = resultSet.getInt("slots");
				String quests = resultSet.getString("quests");
				QuestPlayerData questData = GSON.fromJson(quests, QuestPlayerData.class);

				String items = resultSet.getString("items");
				List<Map<String, Object>> itemsMap = GSON.fromJson(items, List.class);
				List<ItemStack> itemsList = new ArrayList<>(itemsMap.size());

				for (Map<String, Object> map : itemsMap)
				{
					ItemStack itemStack = CraftItemStack.deserialize(map);

					if (map.containsKey("meta"))
					{
						itemStack.setItemMeta(deserialiseMeta(map.get("meta")));
					}

					itemsList.add(itemStack);
				}

				String armour = resultSet.getString("armour");
				List<Map<String, Object>> armourMap = GSON.fromJson(armour, List.class);
				List<ItemStack> armourList = new ArrayList<>(armourMap.size());

				for (Map<String, Object> map : armourMap)
				{
					ItemStack itemStack = CraftItemStack.deserialize(map);

					if (map.containsKey("meta"))
					{
						itemStack.setItemMeta(deserialiseMeta(map.get("meta")));
					}

					armourList.add(CraftItemStack.deserialize(map));
				}

				Timestamp saveTime = resultSet.getTimestamp("saveTime");

				if (saveTime == null)
				{
					saveTime = new Timestamp(System.currentTimeMillis());
				}

				int cashOutTime = resultSet.getInt("cashOutTime");

				_exists.add(accountId);
				Location location = new Location(Bukkit.getWorlds().get(0), x, y, z, yaw, pitch);

				PersistenceData data = new PersistenceData(region, gems, location, questData, health, maxHealth, hunger, slots, itemsList.toArray(new ItemStack[0]), armourList.toArray(new ItemStack[0]), saveTime.getTime(), cashOutTime);
				response.accept(data);
			}
		}, new ColumnInt("accountId", accountId), new ColumnVarChar("region", 2, region.toString()));
	}

	public void savePersistence(CoreClient client, PersistenceData data)
	{
		int accountId = client.getAccountId();

		Region region = data.getRegion();
		int gems = data.getGems();
		int health = data.getHealth();
		int maxHealth = data.getMaxHealth();
		int hunger = data.getHunger();
		int x = data.getLocation().getBlockX();
		int y = data.getLocation().getBlockY();
		int z = data.getLocation().getBlockZ();
		int yaw = (int) data.getLocation().getYaw();
		int pitch = (int) data.getLocation().getPitch();
		int slots = data.getSlots();
		ItemStack[] items = data.getItems();
		ItemStack[] armour = data.getArmour();
		List<Map<String, Object>> itemsMap = new ArrayList<>(items.length);
		List<Map<String, Object>> armourMap = new ArrayList<>(armour.length);
		Timestamp saveTime = new Timestamp(data.getSaveTime());
		int cashOutTime = data.getCashOutTime();

		for (ItemStack itemStack : items)
		{
			if (itemStack == null || itemStack.getType() == Material.MAP || itemStack.getType() == Material.STAINED_GLASS_PANE)
			{
				itemStack = AIR;
			}

			itemsMap.add(itemStack.serialize());
		}

		for (ItemStack itemStack : armour)
		{
			if (itemStack == null)
			{
				continue;
			}

			armourMap.add(itemStack.serialize());
		}

		if (exists(client))
		{
			executeUpdate(UPDATE_DATA,
					new ColumnInt("gems", gems),
					new ColumnInt("health", health),
					new ColumnInt("maxHealth", maxHealth),
					new ColumnInt("hunger", hunger),
					new ColumnInt("x", x),
					new ColumnInt("y", y),
					new ColumnInt("z", z),
					new ColumnInt("yaw", yaw),
					new ColumnInt("pitch", pitch),
					new ColumnVarChar("quests", 500, GSON.toJson(data.getQuestData())),
					new ColumnInt("slots", slots),
					new ColumnVarChar("items", 10000, GSON.toJson(itemsMap)),
					new ColumnVarChar("armour", 1000, GSON.toJson(armourMap)),
					new ColumnTimestamp("saveTime", saveTime),
					new ColumnInt("cashOutTime", cashOutTime),
					new ColumnInt("accountId", accountId),
					new ColumnVarChar("region", 2, region.toString())
			);
		}
		else
		{
			executeInsert(INSERT_DATA, null,
					new ColumnInt("accountId", accountId),
					new ColumnVarChar("region", 2, region.toString()),
					new ColumnInt("gems", gems),
					new ColumnInt("health", health),
					new ColumnInt("maxHealth", maxHealth),
					new ColumnInt("hunger", hunger),
					new ColumnInt("x", x),
					new ColumnInt("y", y),
					new ColumnInt("z", z),
					new ColumnInt("yaw", yaw),
					new ColumnInt("pitch", pitch),
					new ColumnVarChar("quests", 500, GSON.toJson(data.getQuestData())),
					new ColumnInt("slots", slots),
					new ColumnVarChar("items", 10000, GSON.toJson(itemsMap)),
					new ColumnVarChar("armour", 1000, GSON.toJson(armourMap)),
					new ColumnTimestamp("saveTime", saveTime),
					new ColumnInt("cashOutTime", cashOutTime)
			);
		}

		_exists.remove(Integer.valueOf(accountId));
	}

	public void deletePersistence(CoreClient client)
	{
		int accountId = client.getAccountId();

		executeUpdate(DELETE_DATA, new ColumnInt("accountId", accountId));
		_exists.remove(Integer.valueOf(accountId));
	}

	public boolean exists(CoreClient client)
	{
		return _exists.contains(client.getAccountId());
	}

	private ItemMeta deserialiseMeta(Object map)
	{
		if (!(map instanceof Map))
		{
			return null;
		}

		try
		{
			Class<?> clazz = Class.forName("org.bukkit.craftbukkit.v1_8_R3.inventory.CraftMetaItem");
			Constructor<?> constructor = clazz.getDeclaredConstructor(Map.class);

			constructor.setAccessible(true);

			ItemMeta meta = (ItemMeta) constructor.newInstance(map);
			meta.setDisplayName((String) ((Map) map).get("displayName"));

			return meta;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return null;
	}
}
