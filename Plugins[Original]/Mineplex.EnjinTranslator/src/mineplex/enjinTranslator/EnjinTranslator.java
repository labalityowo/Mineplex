package mineplex.enjinTranslator;

import mineplex.core.common.Constants;
import mineplex.core.account.CoreClientManager;
import mineplex.core.command.CommandCenter;
import mineplex.core.donation.DonationManager;
import mineplex.core.inventory.InventoryManager;
import mineplex.core.updater.Updater;
import org.bukkit.plugin.java.JavaPlugin;

import static mineplex.core.Managers.require;

public class EnjinTranslator extends JavaPlugin
{
	@Override
	public void onEnable()
	{
		getConfig().addDefault(Constants.WEB_CONFIG_KEY, Constants.WEB_ADDRESS);
		getConfig().set(Constants.WEB_CONFIG_KEY, getConfig().getString(Constants.WEB_CONFIG_KEY));
		saveConfig();

		//Static Modules
		CommandCenter.Initialize(this);
		
		//Core Modules
		CoreClientManager clientManager = new CoreClientManager(this);
		CommandCenter.Instance.setClientManager(clientManager);
		
		DonationManager donationManager = require(DonationManager.class);
		
		//Main Modules
		new Enjin(this, clientManager, donationManager, new InventoryManager(this, clientManager));
		
		require(Updater.class);
	}
	
	public String GetWebServerAddress()
	{

		return getConfig().getString(Constants.WEB_CONFIG_KEY);
	}
}
