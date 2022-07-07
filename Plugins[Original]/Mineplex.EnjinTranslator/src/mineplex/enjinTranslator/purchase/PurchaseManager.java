package mineplex.enjinTranslator.purchase;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import mineplex.core.MiniPlugin;
import mineplex.core.common.util.NautHashMap;
import mineplex.serverdata.database.ResultSetCallable;
import mineplex.enjinTranslator.purchase.data.PurchaseRepository;
import mineplex.enjinTranslator.purchase.data.Package;

public class PurchaseManager extends MiniPlugin
{
	private static Object _purchaseLock = new Object();

	private PurchaseRepository _repository;
	private NautHashMap<String, Package> _purchases = new NautHashMap<String, Package>();
	
	public PurchaseManager(JavaPlugin plugin)
	{
		super("Purchase Manager", plugin);

		_repository = new PurchaseRepository(plugin);

		Bukkit.getServer().getScheduler().runTaskLaterAsynchronously(getPlugin(), new Runnable()
		{
			public void run()
			{
				updatePackages();
			}
		}, 20L);
	}

	private void updatePackages()
	{
		List<Package> packages = _repository.retrievePackages();
		
		synchronized (_purchaseLock)
		{
			for (mineplex.enjinTranslator.purchase.data.Package purchasePackage : packages)
			{
				_purchases.put(purchasePackage.getName(), purchasePackage);
			}
		}
	}

	public void addAccountPurchaseToQueue(int accountId, final String packageName, int count, boolean success)
	{
		synchronized (_purchaseLock)
		{
			if (!_purchases.containsKey(packageName))
			{
				_repository.addPackage(packageName, new ResultSetCallable()
				{
					public void processResultSet(ResultSet resultSet) throws SQLException 
					{
						while (resultSet.next())
						{
							int packageId = resultSet.getInt(1);
							
							_purchases.put(packageName, new Package(packageId, packageName));
							System.out.println("Added new package : " + packageName);
						}
					}
				});
			}
			
			_repository.addAccountPurchase(accountId, _purchases.get(packageName).getId(), count, success);
		}
	}
}
