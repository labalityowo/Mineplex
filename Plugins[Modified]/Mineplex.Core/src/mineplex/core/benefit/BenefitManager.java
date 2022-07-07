package mineplex.core.benefit;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import mineplex.core.MiniDbClientPlugin;
import mineplex.core.account.CoreClientManager;
import mineplex.core.benefit.benefits.*;
import mineplex.core.common.util.Callback;
import mineplex.core.inventory.InventoryManager;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class BenefitManager extends MiniDbClientPlugin<BenefitData>
{
	private BenefitManagerRepository _repository;
	
	private List<BenefitBase> _benefits = new ArrayList<BenefitBase>();
	 
	public BenefitManager(JavaPlugin plugin, CoreClientManager clientManager, InventoryManager inventoryManager)
	{
		super("Benefit Manager", plugin, clientManager);

		_repository = new BenefitManagerRepository(plugin);
		
		//_benefits.add(new Christmas2014(plugin, _repository, inventoryManager));
		//_benefits.add(new Thanksgiving2014(plugin, _repository, inventoryManager));
		//_benefits.add(new Players43k(this, _repository, inventoryManager));
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void giveBenefit(final PlayerJoinEvent event)
	{
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(getPlugin(), new Runnable()
		{
			public void run()
			{
				if (Get(event.getPlayer()).Loaded)
				{
					for (final BenefitBase benefit : _benefits)
					{
						if (!Get(event.getPlayer()).Benefits.contains(benefit.getName()))
						{
							benefit.recordBenefit(event.getPlayer(), new Callback<Boolean>()
							{
								public void run(Boolean success)
								{
									if (success)
									{
										benefit.rewardPlayer(event.getPlayer());
									}
									else
										System.out.println("Benefit reward failed for " + event.getPlayer().getName());
								}
							});
						}
					}
				}
			}
		}, 100L);
	}

	@Override
	protected BenefitData addPlayer(UUID uuid)
	{
		return new BenefitData();
	}

	@Override
	public void processLoginResultSet(String playerName, UUID uuid, int accountId, ResultSet resultSet) throws SQLException
	{
		Set(uuid, _repository.retrievePlayerBenefitData(resultSet));
	}

	@Override
	public String getQuery(int accountId, String uuid, String name)
	{
		return "SELECT benefit FROM rankBenefits WHERE rankBenefits.accountId = '" + accountId + "';";
	}
}
