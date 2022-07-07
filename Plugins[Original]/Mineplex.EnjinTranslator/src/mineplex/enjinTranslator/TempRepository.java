package mineplex.enjinTranslator;

import mineplex.core.database.MinecraftRepository;
import org.bukkit.plugin.java.JavaPlugin;

import mineplex.serverdata.database.DBPool;
import mineplex.serverdata.database.RepositoryBase;
import mineplex.serverdata.database.column.ColumnInt;
import mineplex.serverdata.database.column.ColumnVarChar;

public class TempRepository extends RepositoryBase
{
	private static String INSERT_CLIENT_INVENTORY = "INSERT INTO accountInventory (accountId, itemId, count) SELECT accounts.id, 5, ? FROM accounts WHERE accounts.name = ? ON DUPLICATE KEY UPDATE count=count + VALUES(count);";
	
	public TempRepository(JavaPlugin plugin)
	{
		super(DBPool.getAccount());
	}
	
	public void addGemBooster(String name, int amount)
	{
		executeUpdate(INSERT_CLIENT_INVENTORY,  new ColumnInt("count", amount), new ColumnVarChar("name", 100, name));
	}
}
