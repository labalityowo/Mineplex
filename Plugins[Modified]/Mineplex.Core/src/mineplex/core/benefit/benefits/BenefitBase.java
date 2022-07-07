package mineplex.core.benefit.benefits;

import mineplex.core.benefit.BenefitManager;
import mineplex.core.benefit.BenefitManagerRepository;
import mineplex.core.common.util.Callback;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class BenefitBase
{
	private BenefitManager _plugin;
	private String _name;
	private BenefitManagerRepository _repository;
	
	protected BenefitBase(BenefitManager plugin, String name, BenefitManagerRepository repository)
	{
		_plugin = plugin;
		_name = name;
		_repository = repository;
	}
	
	public JavaPlugin getPlugin()
	{
		return _plugin.getPlugin();
	}
	
	public BenefitManagerRepository getRepository()
	{
		return _repository;
	}
	
	public abstract void rewardPlayer(Player player);
	
	public void recordBenefit(final Player player, final Callback<Boolean> callback)
	{
		Bukkit.getServer().getScheduler().runTaskAsynchronously(_plugin.getPlugin(), new Runnable() 
		{
			public void run()
			{
				boolean success = _repository.addBenefit(_plugin.getClientManager().Get(player).getAccountId(), _name);
				
				callback.run(success);
			}
		});
	}
	
	protected void removeBenefit(final Player player)
	{
		_repository.removeBenefit(_plugin.getClientManager().Get(player).getAccountId(), _name);
	}

	public String getName()
	{
		return _name;
	}
}
