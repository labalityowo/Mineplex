package mineplex.staffServer;

import mineplex.core.account.CoreClientManager;
import mineplex.core.account.command.RanksCommand;
import mineplex.core.command.CommandCenter;
import mineplex.core.common.Constants;
import mineplex.core.disguise.DisguiseManager;
import mineplex.core.elo.EloManager;
import mineplex.core.inventory.InventoryManager;
import mineplex.core.memory.MemoryFix;
import mineplex.core.packethandler.PacketHandler;
import mineplex.core.portal.Portal;
import mineplex.core.recharge.Recharge;
import mineplex.core.stats.StatsManager;
import mineplex.core.updater.Updater;
import net.minecraft.server.v1_8_R3.MinecraftServer;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.spigotmc.SpigotConfig;

import static mineplex.core.Managers.require;

public class StaffServer extends JavaPlugin
{
	@Override
	public void onEnable()
	{
		getConfig().addDefault(Constants.WEB_CONFIG_KEY, Constants.WEB_ADDRESS);
		getConfig().set(Constants.WEB_CONFIG_KEY, getConfig().getString(Constants.WEB_CONFIG_KEY));
		saveConfig();

		//Static Modules
		CommandCenter.Initialize(this); 
		CoreClientManager clientManager = new CoreClientManager(this);
		CommandCenter.Instance.setClientManager(clientManager);
		Recharge.Initialize(this);

		clientManager.addCommand(new RanksCommand(clientManager));

		//Punish punish = new Punish(this, clientManager);
		//new NpcManager(this, new Creature(this));
		//ServerStatusManager serverStatusManager = new ServerStatusManager(this, clientManager, new LagMeter(this, clientManager));
		//PreferencesManager preferenceManager = new PreferencesManager(this, null, clientManager);

		Portal portal = new Portal();
		EloManager eloManager = new EloManager(this, clientManager);
		StatsManager statsManager = new StatsManager(this, clientManager);
		InventoryManager inventoryManager = new InventoryManager(this, clientManager);
		new MemoryFix(this);
		//new FileUpdater(GenericServer.HUB);

		require(PacketHandler.class);
		require(DisguiseManager.class);

		require(Updater.class);
		
		MinecraftServer.getServer().getPropertyManager().setProperty("debug", false);
		SpigotConfig.debug = false;
		
		Bukkit.getWorlds().get(0).setSpawnLocation(0, 102, 0);

		//require(ProfileCacheManager.class);
	}
}
