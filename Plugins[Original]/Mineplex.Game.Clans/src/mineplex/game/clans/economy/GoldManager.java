package mineplex.game.clans.economy;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.util.Vector;

import mineplex.core.MiniDbClientPlugin;
import mineplex.core.account.CoreClientManager;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.common.currency.GlobalCurrency;
import mineplex.core.common.util.C;
import mineplex.core.common.util.Callback;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.donation.DonationManager;
import mineplex.core.donation.Donor;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.clans.clans.ClansDataAccessLayer;
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.items.economy.GoldToken;
import mineplex.game.clans.shop.bank.BankShop;

public class GoldManager extends MiniDbClientPlugin<GoldData>
{
	public enum Perm implements Permission
	{
		GIVE_GOLD_COMMAND,
		SET_GOLD_COMMAND,
	}
	public static final double GEM_CONVERSION_RATE = 16;    // The number of gold coins when converted from a single gem

	public static final double DEATH_TAX = 0.04d;        // Percentage of gold lost on death
	public static final String META_STRING = "clans.goldAmount";

	private static GoldManager _instance;

	public static GoldManager getInstance()
	{
		return _instance;
	}

	private DonationManager _donationManager;
	private final int _serverId;
	private TransferTracker _transferTracker;
	private Set<Item> _itemSet;
	private Map<Player, Integer> _playerPickupMap;

	public GoldManager(ClansManager plugin, CoreClientManager clientManager, DonationManager donationManager, ClansDataAccessLayer dataAccessLayer)
	{
		super("Clans Gold", plugin.getPlugin(), clientManager);

		_instance = this;
		_donationManager = donationManager;
		_serverId = dataAccessLayer.getRepository().getServerId();
		_transferTracker = new TransferTracker();
		_itemSet = new HashSet<>();
		_playerPickupMap = new HashMap<>();
		new BankShop(plugin, clientManager, donationManager);
		
		generatePermissions();
	}
	
	private void generatePermissions()
	{
		PermissionGroup.ADMIN.setPermission(Perm.GIVE_GOLD_COMMAND, true, true);
		PermissionGroup.ADMIN.setPermission(Perm.SET_GOLD_COMMAND, true, true);
	}

	@Override
	public void addCommands()
	{
		addCommand(new GoldCommand(this));
		addCommand(new SetGoldCommand(this));
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event)
	{
		final Player player = event.getEntity();

		int gold = getGold(player);
		final int droppedGold = (int) (gold * DEATH_TAX);
		final Location deathLocation = player.getLocation();

		if (droppedGold > 0)
		{
			deductGold(success ->
			{
				if (!success)
				{
					return;
				}
				runSync(() ->
				{
					GoldManager.notify(player, "You dropped " + F.elem(droppedGold + "") + " gold on your death!");

					dropGold(deathLocation, droppedGold);
				});
			}, player, droppedGold);
		}
	}

	@EventHandler
	public void playerCmd(PlayerCommandPreprocessEvent event)
	{
		if (event.getMessage().startsWith("/gold"))
		{
			notify(event.getPlayer(), "Your Balance is " + C.cYellow + getGold(event.getPlayer()) + "g");
			event.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onPickup(PlayerPickupItemEvent event)
	{
		if (_itemSet.contains(event.getItem()))
		{
			event.setCancelled(true);

			int goldAmount = 1;
			List<MetadataValue> meta = event.getItem().getMetadata(META_STRING);
			if (meta != null && meta.size() == 1)
			{
				goldAmount = meta.get(0).asInt();
			}

			event.getItem().remove();
			event.getPlayer().playSound(event.getPlayer().getEyeLocation(), Sound.ORB_PICKUP, 1F, 2F);
			addGold(event.getPlayer(), goldAmount);

			int pickupMapGold = goldAmount;
			if (_playerPickupMap.containsKey(event.getPlayer()))
			{
				pickupMapGold += _playerPickupMap.get(event.getPlayer());
			}

			_playerPickupMap.put(event.getPlayer(), pickupMapGold);
		}
	}

	@EventHandler
	public void notifyPickup(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
			return;

		for (Map.Entry<Player, Integer> entry : _playerPickupMap.entrySet())
		{
			if (entry.getKey().isOnline())
			{
				notify(entry.getKey(), "You picked up " + F.elem(entry.getValue() + " gold"));
			}
		}

		_playerPickupMap.clear();
	}

	@EventHandler
	public void cleanItems(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC) return;

		Iterator<Item> itemIterator = _itemSet.iterator();

		while (itemIterator.hasNext())
		{
			Item item = itemIterator.next();

			if (!item.isValid() || item.getTicksLived() >= 12000) // 10 minutes
			{
				item.remove();
				itemIterator.remove();
			}
		}
	}
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onChunkUnload(ChunkUnloadEvent event)
	{
		List<Entity> entities = Arrays.asList(event.getChunk().getEntities());
		_itemSet.stream().filter(entities::contains).forEach(Item::remove);
	}

	public int getGold(Player player)
	{
		return Get(player).getBalance();
	}

	public int getGems(Player player)
	{
		return getDonor(player).getBalance(GlobalCurrency.GEM);
	}

	public void transferGemsToCoins(Player player, int gemAmount)
	{
		int gemCount = getGems(player);
		int goldCount = (int) (((double) gemAmount) * GEM_CONVERSION_RATE);

		if (gemCount >= gemAmount)
		{
			_donationManager.rewardCurrency(GlobalCurrency.GEM, player, "GoldManager", -gemAmount);
			addGold(player, goldCount);
			notify(player, String.format("You have transferred %d gems into %d gold coins!", gemAmount, goldCount));
			_transferTracker.insertTransfer(player);
		}
	}

	/**
	 * @param player - the player to be checked for whether they can transfer gems into coins
	 * @return true, if the player has not converted gems into coins within the
	 * last day, false otherwise.
	 */
	public boolean canTransferGems(Player player)
	{
		return !_transferTracker.hasTransferredToday(player);
	}

	public void cashIn(Player player, GoldToken token)
	{
		int value = token.getGoldValue();
		addGold(player, value);
		notify(player, String.format("You have cashed in a gold token worth %dg!", value));
	}

	public void dropGold(Location location, int amount)
	{
		dropGold(location, amount, 0.15);
	}

	public void dropGold(Location location, int amount, double velMult)
	{
		int count = amount / 1000;

		if (count > 75)
		{
			double x = Math.random() * 2 * Math.PI;
			Vector velocity = new Vector(Math.sin(x), 0, Math.cos(x));
			dropGold(location, amount, velocity, velMult, "Gold " + 0);
			return;
		}

		int extraGold = amount % 1000;

		for (int i = 0; i < count; i++)
		{
			double x = Math.random() * 2 * Math.PI;
			Vector velocity = new Vector(Math.sin(x), 0, Math.cos(x));
			dropGold(location, 1000, velocity, velMult, "Gold " + i);
		}

		// Drop Extra
		if (extraGold > 0)
		{
			double x = Math.random() * 2 * Math.PI;
			Vector velocity = new Vector(Math.sin(x), 0, Math.cos(x));
			dropGold(location, extraGold, velocity, velMult, "extra gold");
		}
	}

	private void dropGold(Location location, int amount, Vector velocity, double velMult, String name)
	{
		ItemStack stack = new ItemStack(Material.GOLD_NUGGET);
		ItemMeta meta = stack.getItemMeta();
		meta.setDisplayName(name);
		stack.setItemMeta(meta);

		Item item = location.getWorld().dropItem(location, stack);
		item.setPickupDelay(40);
		item.setMetadata(META_STRING, new FixedMetadataValue(getPlugin(), amount));

		_itemSet.add(item);

		// Velocity
		UtilAction.velocity(item, velocity, velMult, false, 0, 0.2, 0.2, false);
	}

	public void purchaseToken(final Player player, final int tokenValue)
	{
		final GoldToken token = new GoldToken(tokenValue);
		deductGold(success ->
		{
			if (success)
			{
				player.getInventory().addItem(token.toItemStack());
				GoldManager.notify(player, String.format("You have purchased a gold token worth %dg!", tokenValue));
			}
			else
			{
				GoldManager.notify(player, String.format("You have purchased a gold token worth %dg!", tokenValue));
			}
		}, player, tokenValue);
	}


	private Donor getDonor(Player player)
	{
		return _donationManager.Get(player);
	}

	public static void notify(Player player, String message)
	{
		UtilPlayer.message(player, F.main("Gold", message));
	}

	@Override
	public void disable()
	{
		for (Item item : _itemSet)
		{
			item.remove();
		}
	}

	public void setGold(final Callback<Boolean> callback, final String caller, final String name, final int accountId, final int amount, final boolean updateTotal)
	{
		_donationManager.getGoldRepository().setGold(success ->
		{
			if (success.booleanValue())
			{
				if (updateTotal)
				{
					GoldData data = Get(name);

					if (data != null)
					{
						data.setBalance(amount);
					}
				}
			}
			else
			{
				System.out.println("SET GOLD FAILED...");
			}

			if (callback != null)
			{
				callback.run(success);
			}
		}, _serverId, accountId, amount);
	}

	public void addGold(Player player, int amount)
	{
		if (amount >= 0)
		{
			rewardGold(null, player, amount, true);
		}
	}

	public void deductGold(Callback<Boolean> resultCallback, Player player, int amount)
	{
		if (amount > 0)
		{
			rewardGold(resultCallback, player, -amount, true);
		}
		if (amount == 0 && resultCallback != null)
		{
			resultCallback.run(true);
		}
	}

	public void rewardGold(final Callback<Boolean> callback, final Player player, final int amount, final boolean updateTotal)
	{
		rewardGold(callback, getClientManager().Get(player).getAccountId(), player.getName(), amount, updateTotal);
	}

	public void rewardGold(final Callback<Boolean> callback, int accountId, String name, final int amount, final boolean updateTotal)
	{
		if (amount == 0)
		{
			if (callback != null)
			{
				callback.run(true);
			}
			return;
		}

		_donationManager.getGoldRepository().rewardGold(success ->
		{
			if (success.booleanValue())
			{
				if (updateTotal)
				{
					GoldData data = Get(name);

					if (data != null)
					{
						data.addBalance(amount);
					}
				}
			}
			else
			{
				System.out.println("REWARD GOLD FAILED...");
			}

			if (callback != null)
			{
				callback.run(success);
			}
		}, _serverId, accountId, amount);
	}

	@Override
	public String getQuery(int accountId, String uuid, String name)
	{
		return "SELECT gold FROM clansGold WHERE accountId = '" + accountId + "' AND serverId=" + _serverId + ";";
	}

	@Override
	public void processLoginResultSet(String playerName, UUID playerUUID, int accountId, ResultSet resultSet) throws SQLException
	{
		if (resultSet.next())
		{
			Get(playerUUID).setBalance(resultSet.getInt(1));
		}
	}

	@Override
	protected GoldData addPlayer(UUID uuid)
	{
		return new GoldData();
	}
}