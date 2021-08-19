package mineplex.core.inventory.data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import mineplex.core.database.MinecraftRepository;
import org.bukkit.plugin.java.JavaPlugin;

import mineplex.core.common.util.NautHashMap;
import mineplex.serverdata.database.DBPool;
import mineplex.serverdata.database.RepositoryBase;
import mineplex.serverdata.database.ResultSetCallable;
import mineplex.serverdata.database.column.ColumnInt;
import mineplex.serverdata.database.column.ColumnVarChar;
import mineplex.core.inventory.ClientInventory;
import mineplex.core.inventory.ClientItem;

public class InventoryRepository extends RepositoryBase
{
	private static String CREATE_INVENTORY_TABLE = "CREATE TABLE IF NOT EXISTS items (id INT NOT NULL AUTO_INCREMENT, name VARCHAR(100), rarity INT, PRIMARY KEY (id), INDEX mameIndex (name));";
	private static String CREATE_INVENTORY_RELATION_TABLE = "CREATE TABLE IF NOT EXISTS accountInventory (id INT NOT NULL AUTO_INCREMENT, accountId INT NOT NULL, itemId INT NOT NULL, count INT NOT NULL, PRIMARY KEY (id), FOREIGN KEY (accountId) REFERENCES accounts(id), FOREIGN KEY (itemId) REFERENCES items(id), UNIQUE INDEX accountItemIndex (accountId, itemId));";
	
	private static String INSERT_ITEM = "INSERT INTO items (name) VALUES (?);";
	private static String RETRIEVE_ITEMS = "SELECT items.id, items.name FROM items;";
		
	private static String INSERT_CLIENT_INVENTORY = "INSERT INTO accountInventory (accountId, itemId, count) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE count=count + VALUES(count);";
	private static String UPDATE_CLIENT_INVENTORY = "UPDATE accountInventory SET count = count + ? WHERE accountId = ? AND itemId = ?;";

	public InventoryRepository(JavaPlugin plugin)
	{
		super(DBPool.getAccount());
	}
	
	public void addItem(String name)
	{
		executeUpdate(INSERT_ITEM, new ColumnVarChar("name", 100, name));
	}
	
	public List<Item> retrieveItems()
	{
		final List<Item> items = new ArrayList<Item>();
		
		executeQuery(RETRIEVE_ITEMS, new ResultSetCallable()
		{
			public void processResultSet(ResultSet resultSet) throws SQLException
			{
				while (resultSet.next())
				{
					items.add(new Item(resultSet.getInt(1), resultSet.getString(2)));
				}
			}
		});
		
		return items;
	}
	
	public boolean incrementClientInventoryItem(int accountId, int itemId, int count)
	{
		//System.out.println("Updating " + accountId + "'s " + itemId + " with " + count);
		if (executeUpdate(UPDATE_CLIENT_INVENTORY, new ColumnInt("count", count), new ColumnInt("id", accountId), new ColumnInt("itemid", itemId)) < 1)
		{
			//System.out.println("Inserting " + accountId + "'s " + itemId + " with " + count);
			return executeUpdate(INSERT_CLIENT_INVENTORY, new ColumnInt("id", accountId), new ColumnInt("itemid", itemId), new ColumnInt("count", count)) > 0;
		}
		else
			return true;
	}
	
	public ClientInventory loadClientInformation(ResultSet resultSet, NautHashMap<Integer, String> itemIdMap) throws SQLException
	{
		final ClientInventory clientInventory = new ClientInventory();

		while (resultSet.next())
		{
			clientInventory.addItem(new ClientItem(new Item(resultSet.getInt(1), itemIdMap.get(resultSet.getInt(1))), resultSet.getInt(2)));
		}
		
		return clientInventory;
	}
}
