package mineplex.core.boosters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import mineplex.core.MiniPlugin;
import mineplex.core.account.CoreClientManager;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.boosters.command.BoosterCommand;
import mineplex.core.boosters.event.BoosterActivateEvent;
import mineplex.core.boosters.event.BoosterExpireEvent;
import mineplex.core.boosters.event.BoosterItemGiveEvent;
import mineplex.core.boosters.event.BoosterUpdateEvent;
import mineplex.core.boosters.gui.BoosterShop;
import mineplex.core.boosters.redis.BoosterUpdateRepository;
import mineplex.core.boosters.tips.BoosterThankManager;
import mineplex.core.common.util.C;
import mineplex.core.common.util.Callback;
import mineplex.core.common.util.UtilGear;
import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilServer;
import mineplex.core.donation.DonationManager;
import mineplex.core.inventory.InventoryManager;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.core.thank.ThankManager;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

/**
 * BoosterManager handles the majority of logic for creating and getting Boosters. Every BoosterManager stores a cache
 * for all boosters across all servers. We pull all boosters from the API when the server boots up. To keep them in sync,
 * instead of consistently polling the API I have decided to go with a redis pub/sub solution to ensuring all boosters
 * across all servers are up to date. Whenever the Booster API receives a call to add or modify boosters, it will publish
 * an updated version of all boosters over redis.
 *
 * Boosters are enabled on live servers using "Booster Groups". A {@link mineplex.serverdata.data.ServerGroup} can specify
 * which BoosterGroup applies to it. If there is no BoosterGroup, then it means the server does not use boosters. To add
 * a BoosterGroup, you must add to the "boostergroups" set on redis (the same way the servergroups set works), otherwise
 * the API will return an error saying that BoosterGroup does not exist. Currently BoosterGroups are no more than a String
 * key for Boosters. In the future we may want to look into implementing BoosterGroup specific data such as default
 * booster length and multiplier.
 *
 * @author Shaun Bennett
 */
public class BoosterManager extends MiniPlugin
{
	// The InventoryManager item name for boosters. This is required to activate a booster on servers
	public static final String BOOSTER_ITEM = "Game Booster";
	// Item in arcade lobbies that opens the booster gui
	public static final ItemStack INTERFACE_ITEM = ItemStackFactory.Instance.CreateStack(Material.EMERALD, (byte)0, 1, ChatColor.RESET + C.cGreen + "Game Amplifiers");
	// Slot for the booster gui item
	public static final int INTERFACE_SLOT = 7;

	public enum Perm implements Permission
	{
		ADD_BOOSTER_COMMAND,
		BOOSTER_COMMAND,
		BOOSTER_GUI_COMMAND,
		RELOAD_BOOSTERS_COMMAND,
		THANK_COMMAND,
	}

	private BoosterRepository _repository;
	private CoreClientManager _clientManager;
	private DonationManager _donationManager;
	private InventoryManager _inventoryManager;
	private BoosterThankManager _boosterThankManager;

	private BoosterShop _shop;
	private String _boosterGroup;

	private boolean _giveInterfaceItem;

	private long _cacheLastUpdated;
	private Map<String, List<Booster>> _boosterCache = new HashMap<>();

	public BoosterManager(JavaPlugin plugin, String boosterGroup, CoreClientManager clientManager, DonationManager donationManager, InventoryManager inventoryManager, ThankManager thankManager)
	{
		super("Booster Manager", plugin);

		_repository = new BoosterRepository();
		_boosterGroup = boosterGroup;
		_clientManager = clientManager;
		_donationManager = donationManager;
		_inventoryManager = inventoryManager;

		_boosterThankManager = new BoosterThankManager(plugin, clientManager, thankManager);
		_shop = new BoosterShop(this, clientManager, donationManager);

		try
		{
			Map<String, List<Booster>> boosters = _repository.getBoosters();
			if (boosters != null) _boosterCache = boosters;
		}
		catch (Exception e)
		{
			System.out.println("Failed to load boosters on server start.");
			e.printStackTrace();
		}

		_giveInterfaceItem = canActivateBoosters();

		new BoosterUpdateRepository(plugin);
		
		generatePermissions();
	}
	
	private void generatePermissions()
	{
		PermissionGroup.PLAYER.setPermission(Perm.BOOSTER_COMMAND, true, true);
		PermissionGroup.PLAYER.setPermission(Perm.THANK_COMMAND, true, true);
		PermissionGroup.DEV.setPermission(Perm.ADD_BOOSTER_COMMAND, true, true);
		PermissionGroup.DEV.setPermission(Perm.RELOAD_BOOSTERS_COMMAND, true, true);
		PermissionGroup.DEV.setPermission(Perm.BOOSTER_GUI_COMMAND, true, true);
	}

	@Override
	public void addCommands()
	{
		addCommand(new BoosterCommand(this));
	}

	/**
	 * Make an API call to grab all Boosters
	 */
	@Deprecated
	public void getBoostersAsync(Callback<Map<String, List<Booster>>> callback)
	{
		runAsync(() -> {
			try
			{
				long time = System.currentTimeMillis();
				Map<String, List<Booster>> boosters = _repository.getBoosters();
				long timeTaken = System.currentTimeMillis() - time;
				runSync(() -> {
					handleBoosterUpdate(boosters);
					if (callback != null) callback.run(boosters);
				});
			}
			catch (Exception e)
			{
				System.err.println("Failed to grab boosters;");
				e.printStackTrace();
			}
		});
	}

	/**
	 * Make an API call to grab all boosters for a specific booster group
	 * @param boosterGroup
	 * @param callback
	 */
	@Deprecated
	public void getBoostersAsync(String boosterGroup, Callback<List<Booster>> callback)
	{
		runAsync(() -> {
			try
			{
				List<Booster> boosters = _repository.getBoosters(boosterGroup);
				if (callback != null) runSync(() -> callback.run(boosters));
			}
			catch (Exception e)
			{
				System.err.println("Failed to grab boosters for boostergroup: " + boosterGroup);
				e.printStackTrace();
			}
		});
	}

	/**
	 * Process the new boosterMap whenever a BoosterUpdateEvent is sent. This will compare itself to the current
	 * cached BoosterMap and call events when it finds a booster was activated or deactivated
	 * @param boosterMap The new booster map
	 */
	private void handleBoosterUpdate(Map<String, List<Booster>> boosterMap)
	{
		_boosterCache.entrySet().stream()
				.filter(entry -> entry.getValue().size() > 0)
				.filter(entry -> boosterMap.get(entry.getKey()) == null)
				.forEach(entry -> callNextTick(new BoosterExpireEvent(entry.getKey(), entry.getValue().get(0))));

		for (Map.Entry<String, List<Booster>> entry : boosterMap.entrySet())
		{
			List<Booster> current = _boosterCache.get(entry.getKey());
			if (entry.getValue() != null && !entry.getValue().isEmpty())
			{
				if (current == null || current.isEmpty())
				{
					// New booster was added
					callNextTick(new BoosterActivateEvent(entry.getKey(), entry.getValue().get(0)));
				} else if (!current.get(0).equals(entry.getValue().get(0)))
				{
					// First booster was deactivated, new booster replaced it
					callNextTick(new BoosterExpireEvent(entry.getKey(), current.get(0)));
					callNextTick(new BoosterActivateEvent(entry.getKey(), entry.getValue().get(0)));
				}
			}
		}

		_cacheLastUpdated = System.currentTimeMillis();
		_boosterCache = boosterMap;
	}

	private void tickBoosterCache()
	{
		List<Event> events = new ArrayList<>(3);
		for (Map.Entry<String, List<Booster>> entry : _boosterCache.entrySet())
		{
			Iterator<Booster> iterator = entry.getValue().iterator();
			boolean removedOne = false;
			while (iterator.hasNext())
			{
				Booster booster = iterator.next();
				if (!booster.isActive())
				{
					iterator.remove();
					removedOne = true;
					events.add(new BoosterExpireEvent(entry.getKey(), booster));
				}
				else
				{
					if (removedOne) events.add(new BoosterActivateEvent(entry.getKey(), booster));
					break;
				}
			}
		}

		events.forEach(Bukkit.getPluginManager()::callEvent);
	}

	@EventHandler
	public void tickBoosters(UpdateEvent event)
	{
		if (event.getType() == UpdateType.MIN_10)
		{
			// sync with API every 10 minutes, incase pubsub fails
			getBoostersAsync(null);
		}
		else if (event.getType() == UpdateType.SEC)
		{
			tickBoosterCache();
		}
	}

	/**
	 * Return all boosters for the active booster group
	 * @return list of boosters, or null if there is no active booster group
	 */
	public List<Booster> getBoosters()
	{
		if (_boosterGroup == null || _boosterGroup.length() == 0)
		{
			return null;
		}
		else
		{
			List<Booster> boosters = _boosterCache.get(_boosterGroup);
			return boosters == null ? Collections.emptyList() : boosters;
		}
	}

	public String getBoosterGroup()
	{
		return _boosterGroup;
	}

	public long getBoostTime()
	{
		return getBoostTime(_boosterGroup);
	}

	public long getBoostTime(String boosterGroup)
	{
		long time = 0;
		List<Booster> boosters = _boosterCache.get(boosterGroup);
		if (boosters != null && boosters.size() > 0)
		{
			for (Booster booster : boosters)
			{
				time += booster.getTimeRemaining();
			}
		}

		return time;
	}

	public Booster getActiveBooster()
	{
		return getActiveBooster(_boosterGroup);
	}

	public Booster getActiveBooster(String boosterGroup)
	{
		List<Booster> boosters = _boosterCache.get(boosterGroup);
		if (boosters != null)
		{
			for (Booster booster : boosters)
			{
				if (booster.getEndTime().after(new Date()))
					return booster;
			}
		}

		return null;
	}

	public void activateBooster(Player player, Callback<BoosterApiResponse> callback)
	{
		activateBooster(_boosterGroup, player, callback);
	}

	public void activateBooster(String serverGroup, Player player, Callback<BoosterApiResponse> callback)
	{
		String playerName = player.getName();
		UUID uuid = player.getUniqueId();
		int accountId = _clientManager.getAccountId(player);
//		PropertyMap propertyMap = ((CraftPlayer) player).getHandle().getProfile().getProperties();

		runAsync(() -> {
			BoosterApiResponse response = _repository.addBooster(serverGroup, playerName, uuid, accountId, 3600);
			callback.run(response);
		});
	}

	public void chargeBooster(Player player, Callback<Boolean> callback)
	{
		_inventoryManager.addItemToInventory(callback, player, BOOSTER_ITEM, -1);
	}

	public void refundBooster(Player player, Callback<Boolean> callback)
	{
		_inventoryManager.addItemToInventory(callback, player, BOOSTER_ITEM, 1);
	}

	public void openShop(Player player)
	{
		_shop.attemptShopOpen(player);
	}

	/**
	 * Booster updates are sent from {@link mineplex.core.boosters.redis.BoosterUpdateListener}
	 */
	@EventHandler
	public void onBoosterUpdate(BoosterUpdateEvent event)
	{
		handleBoosterUpdate(event.getBoosterMap());
	}

	public BoosterThankManager getBoosterThankManager()
	{
		return _boosterThankManager;
	}

	/**
	 * Returns the number of unactivated game boosters a player owns
	 * @param player
	 * @return The amount of unactivated game boosters the player owns
	 */
	public int getAvailableBoosterCount(Player player)
	{
		return _inventoryManager.Get(player).getItemCount(BOOSTER_ITEM);
	}

	/**
	 * Can players activate boosters on this server?
	 * @return true if players are able to activate a booster on this server
	 */
	public boolean canActivateBoosters()
	{
		return _boosterGroup != null && _boosterGroup.length() > 0;
	}

	public void giveInterfaceItem(Player player)
	{
		if (_giveInterfaceItem && !UtilGear.isMat(player.getInventory().getItem(INTERFACE_SLOT), Material.EMERALD))
		{
			BoosterItemGiveEvent event = new BoosterItemGiveEvent(player);
			UtilServer.CallEvent(event);
			if (event.isCancelled())
				return;

			player.getInventory().setItem(INTERFACE_SLOT, INTERFACE_ITEM);

			UtilInv.Update(player);
		}
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event)
	{
		giveInterfaceItem(event.getPlayer());
	}

	@EventHandler
	public void onInteract(PlayerInteractEvent event)
	{
		if (INTERFACE_ITEM.equals(event.getPlayer().getItemInHand()))
		{
			openShop(event.getPlayer());
		}
	}

	public Map<String, List<Booster>> getBoosterCache()
	{
		return _boosterCache;
	}

	private void callNextTick(Event event)
	{
		runSync(() -> getPluginManager().callEvent(event));
	}
}