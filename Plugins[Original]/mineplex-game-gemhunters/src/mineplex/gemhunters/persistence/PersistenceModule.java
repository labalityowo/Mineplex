package mineplex.gemhunters.persistence;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import mineplex.core.common.util.UtilServer;
import mineplex.gemhunters.death.DeathModule;
import mineplex.serverdata.Region;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import mineplex.core.MiniPlugin;
import mineplex.core.ReflectivelyCreateMiniPlugin;
import mineplex.core.account.CoreClient;
import mineplex.core.account.CoreClientManager;
import mineplex.core.common.util.F;
import mineplex.core.portal.events.ServerTransferEvent;
import mineplex.core.recharge.Recharge;
import mineplex.core.recharge.RechargeData;
import mineplex.gemhunters.death.event.QuitNPCDespawnEvent;
import mineplex.gemhunters.economy.CashOutModule;
import mineplex.gemhunters.economy.EconomyModule;
import mineplex.gemhunters.loot.InventoryModule;
import mineplex.gemhunters.quest.QuestModule;
import mineplex.gemhunters.quest.QuestPlayerData;

@ReflectivelyCreateMiniPlugin
public class PersistenceModule extends MiniPlugin
{

	private final CoreClientManager _client;
	private final CashOutModule _cashOut;
	private final DeathModule _death;
	private final EconomyModule _economy;
	private final QuestModule _quest;
	private final InventoryModule _inventory;

	private final PersistenceRepository _repository;

	private final List<String> _denyJoining;

	public PersistenceModule()
	{
		super("Persistence");

		_client = require(CoreClientManager.class);
		_cashOut = require(CashOutModule.class);
		_death = require(DeathModule.class);
		_quest = require(QuestModule.class);
		_economy = require(EconomyModule.class);
		_inventory = require(InventoryModule.class);

		_repository = new PersistenceRepository();

		_denyJoining = Collections.synchronizedList(new ArrayList<>());
	}

	@EventHandler
	public void preLogin(AsyncPlayerPreLoginEvent event)
	{
		if (_denyJoining.contains(event.getName()))
		{
			event.disallow(Result.KICK_OTHER, "Please wait a few seconds before connecting again.");
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void playerQuit(PlayerQuitEvent event)
	{
		Player player = event.getPlayer();
		CoreClient client = _client.Get(player);

		if (_death.isRespawning(player))
		{
			return;
		}
		else if (_cashOut.isAboutToCashOut(player))
		{
			runAsync(() ->
			{
				_denyJoining.add(player.getName());
				_repository.deletePersistence(client);
				_denyJoining.remove(player.getName());
			});
			return;
		}

		Region region = UtilServer.getRegion();
		int gems = _economy.Get(player);
		Location location = player.getLocation();
		QuestPlayerData quest = _quest.Get(player);
		int health = (int) player.getHealth();
		int maxHealth = (int) player.getMaxHealth();
		int hunger = player.getFoodLevel();
		int slots = _inventory.getSlots(player);
		ItemStack[] items = player.getInventory().getContents();
		ItemStack[] armour = player.getInventory().getArmorContents();
		long saveTime = System.currentTimeMillis();
		int cashOutTime;
		RechargeData rechargeData = Recharge.Instance.Get(player).get("Cash Out");

		if (rechargeData == null)
		{
			cashOutTime = 0;
		}
		else
		{
			cashOutTime = (int) rechargeData.GetRemaining();
		}

		PersistenceData data = new PersistenceData(region, gems, location, quest, health, maxHealth, hunger, slots, items, armour, saveTime, cashOutTime);

		runAsync(() ->
		{
			_denyJoining.add(player.getName());
			_repository.savePersistence(client, data);
			_denyJoining.remove(player.getName());
		});
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void playerDeath(PlayerDeathEvent event)
	{
		Player player = event.getEntity();
		CoreClient client = _client.Get(player);

		runAsync(() ->
		{
			_denyJoining.add(player.getName());
			_repository.deletePersistence(client);
			_denyJoining.remove(player.getName());
		});
	}

	@EventHandler
	public void npcDespawn(QuitNPCDespawnEvent event)
	{
		if (!event.isPluginRemove())
		{
			runAsync(() ->
			{
				String name = event.getNpc().getName();
				_denyJoining.add(name);
				_client.getOrLoadClient(event.getNpc().getName(), _repository::deletePersistence);
				_denyJoining.remove(name);
			});
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void serverTransfer(ServerTransferEvent event)
	{
		if (event.getServer().startsWith("GH-"))
		{
			event.getPlayer().sendMessage(F.main("Portal", "Sorry, in order to switch servers please use /hub."));
			event.setCancelled(true);
		}
	}

	public final PersistenceRepository getRepository()
	{
		return _repository;
	}
}
