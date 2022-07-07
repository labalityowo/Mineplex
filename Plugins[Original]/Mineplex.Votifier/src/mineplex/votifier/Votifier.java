package mineplex.votifier;

import org.bukkit.plugin.java.JavaPlugin;

import mineplex.core.Managers;
import mineplex.core.account.CoreClientManager;
import mineplex.core.bonuses.BonusManager;
import mineplex.core.command.CommandCenter;
import mineplex.core.donation.DonationManager;
import mineplex.core.inventory.InventoryManager;
import mineplex.core.stats.StatsManager;

public class Votifier extends JavaPlugin
{
	private String WEB_CONFIG = "webServer";

	@Override
	public void onEnable()
	{
		getConfig().addDefault(WEB_CONFIG, "http://accounts.mineplex.com/");
		getConfig().set(WEB_CONFIG, getConfig().getString(WEB_CONFIG));
		saveConfig();

		String webServerAddress = getConfig().getString(WEB_CONFIG);

		CommandCenter.Initialize(this);
		CoreClientManager clientManager = new CoreClientManager(this);
		DonationManager donationManager = Managers.require(DonationManager.class);
		BonusManager bonusManager = new BonusManager(this, clientManager, donationManager);
		InventoryManager inventoryManager = new InventoryManager(this, clientManager);
		StatsManager statsManager = new StatsManager(this, clientManager);

		VotifierManager vote = new VotifierManager(this, clientManager, donationManager, bonusManager, inventoryManager, statsManager);
	}
}