package mineplex.enjinTranslator.purchase.data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import mineplex.core.database.MinecraftRepository;
import org.bukkit.plugin.java.JavaPlugin;

import mineplex.serverdata.database.DBPool;
import mineplex.serverdata.database.RepositoryBase;
import mineplex.serverdata.database.ResultSetCallable;
import mineplex.serverdata.database.column.ColumnBoolean;
import mineplex.serverdata.database.column.ColumnInt;
import mineplex.serverdata.database.column.ColumnVarChar;

public class PurchaseRepository extends RepositoryBase
{
	private static String INSERT_ACCOUNT_PURCHASE = "INSERT INTO accountPurchases (accountId, packageId, amount, date, success) VALUES (?, ?, ?, now(), ?);";
	
	private static String INSERT_PACKAGE = "INSERT INTO packages (packageName) VALUES (?);";
	private static String RETRIEVE_PACKAGES = "SELECT id, packageName FROM packages;";
	
	public PurchaseRepository(JavaPlugin plugin) 
	{
		super(DBPool.getAccount());
	}
	
	public void addPackage(String name, ResultSetCallable callable)
	{
		executeInsert(INSERT_PACKAGE, callable, new ColumnVarChar("packageName", 100, name));
	}
	
	public List<Package> retrievePackages()
	{
		final List<Package> packages = new ArrayList<Package>();
		
		executeQuery(RETRIEVE_PACKAGES, new ResultSetCallable()
		{
			public void processResultSet(ResultSet resultSet) throws SQLException
			{
				while (resultSet.next())
				{
					packages.add(new Package(resultSet.getInt(1), resultSet.getString(2)));
				}
			}
		});
		
		return packages;
	}
	
	public boolean addAccountPurchase(int accountId, int packageId, int count, boolean success)
	{
		return executeInsert(INSERT_ACCOUNT_PURCHASE, null, new ColumnInt("accountId", accountId), new ColumnInt("packageId", packageId), new ColumnInt("count", count), new ColumnBoolean("success", success)) > 0;
	}
}
