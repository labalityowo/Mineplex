package mineplex.core.inventory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.EnchantingInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import mineplex.cache.player.PlayerCache;
import mineplex.core.MiniDbClientPlugin;
import mineplex.core.account.CoreClientManager;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.common.util.Callback;
import mineplex.core.common.util.NautHashMap;
import mineplex.core.inventory.command.GiveItemCommand;
import mineplex.core.inventory.data.InventoryRepository;
import mineplex.core.inventory.data.Item;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class InventoryManager extends MiniDbClientPlugin<ClientInventory>
{
	public enum Perm implements Permission
	{
		GIVE_ITEM_COMMAND,
	}

	private static Object _inventoryLock = new Object();

	private InventoryRepository _repository;

	private NautHashMap<String, Item> _items = new NautHashMap<>();
	private NautHashMap<Integer, String> _itemIdNameMap = new NautHashMap<>();

	private NautHashMap<Player, NautHashMap<String, Integer>> _inventoryQueue = new NautHashMap<>();

	public InventoryManager(JavaPlugin plugin, CoreClientManager clientManager)
	{
		super("Inventory Manager", plugin, clientManager);

		_repository = new InventoryRepository(plugin);

		Bukkit.getServer().getScheduler().runTaskLaterAsynchronously(getPlugin(), () ->
		{
			updateItems();
		}, 20L);
		
		generatePermissions();
	}
	
	private void generatePermissions()
	{
		PermissionGroup.ADMIN.setPermission(Perm.GIVE_ITEM_COMMAND, true, true);
	}

	private void updateItems()
	{
		List<Item> items = _repository.retrieveItems();

		synchronized (_inventoryLock)
		{
			for (Item item : items)
			{
				_items.put(item.Name, item);
				_itemIdNameMap.put(item.Id, item.Name);
			}
		}
	}

	public void addItemToInventory(final Player player, final String item, final int count)
	{
		if (_items.containsKey(item))
		{
			Get(player).addItem(new ClientItem(_items.get(item), count));
		}

		if (!_inventoryQueue.containsKey(player))
			_inventoryQueue.put(player, new NautHashMap<String, Integer>());

		int totalAmount = count;

		if (_inventoryQueue.get(player).containsKey(item))
			totalAmount += _inventoryQueue.get(player).get(item);

		_inventoryQueue.get(player).put(item, totalAmount);
	}

	public void addItemToInventory(final Callback<Boolean> callback, final Player player, final String item, final int count)
	{
		int accountId = getClientManager().getAccountId(player);
		addItemToInventoryForOffline(new Callback<Boolean>()
		{
			public void run(Boolean success)
			{
				if (!success)
				{
					System.out.println("Add item to Inventory FAILED for " + player.getName());
				}
				else
				{
					if (_items.containsKey(item))
					{
						Get(player).addItem(new ClientItem(_items.get(item), count));
					}
				}

				if (callback != null)
					callback.run(success);
			}
		}, accountId, item, count);
	}

	public boolean validItem(String item)
	{
		synchronized (_inventoryLock)
		{
			return _items.containsKey(item);
		}
	}

	public Item getItem(String itemName)
	{
		for (Map.Entry<String, Item> entry : _items.entrySet())
		{
			String name = entry.getKey();

			if (name.equalsIgnoreCase(itemName))
			{
				return entry.getValue();
			}
		}

		return null;
	}

	public void addItemToInventoryForOffline(final Callback<Boolean> callback, final UUID uuid, final String item, final int count)
	{
		Bukkit.getServer().getScheduler().runTaskAsynchronously(getPlugin(), new Runnable()
		{
			public void run()
			{
				int accountId = PlayerCache.getInstance().getAccountId(uuid);
				if (accountId != -1)
				{
					addItemToInventoryForOffline(callback, accountId, item, count);
				}
				else
				{
					ClientManager.loadAccountIdFromUUID(uuid, new Callback<Integer>()
					{
						@Override
						public void run(Integer id)
						{
							if (id > 0)
							{
								addItemToInventoryForOffline(callback, id, item, count);
							}
							else
							{
								runSync(() -> callback.run(false));
							}
						}
					});
				}
			}
		});
	}

	public void addItemToInventoryForOffline(final Callback<Boolean> callback, final int accountId, final String item, final int count)
	{
		Bukkit.getServer().getScheduler().runTaskAsynchronously(getPlugin(), new Runnable()
		{
			public void run()
			{
				synchronized (_inventoryLock)
				{
					if (!_items.containsKey(item))
					{
						_repository.addItem(item);
						System.out.println("InventoryManager Adding Item : " + item);
					}
				}

				updateItems();

				synchronized (_inventoryLock)
				{
					final boolean success = _repository.incrementClientInventoryItem(accountId, _items.get(item).Id, count);
					
					if (callback != null)
					{
						Bukkit.getServer().getScheduler().runTask(getPlugin(), new Runnable()
						{
							public void run()
							{
								callback.run(success);
							}
						});
					}
				}
			}
		});
	}

	@EventHandler
	public void onOpenEnchantingTable(InventoryOpenEvent event)
	{
		if (event.getInventory().getType() != InventoryType.ENCHANTING)
		{
			return;
		}

		int level = ((Player) event.getPlayer()).getLevel();

		for (HumanEntity viewer : event.getViewers())
		{
			level = Math.max(((Player) viewer).getLevel(), level);
		}

		event.getInventory().setItem(1, new ItemStack(Material.INK_SACK, level, (byte) 4));
	}

	// fixme broken cast
//	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event)
	{
		if (event.getBlock().getType() != Material.ENCHANTMENT_TABLE)
		{
			return;
		}

		((EnchantingInventory) event.getBlock().getState()).setSecondary(new ItemStack(Material.AIR));
	}

	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event)
	{
		if (event.getInventory().getType() != InventoryType.ENCHANTING)
		{
			return;
		}

		if (event.getViewers().size() > 1)
		{
			int level = ((Player) event.getPlayer()).getLevel();

			for (HumanEntity viewer : event.getViewers())
			{
				level = Math.max(((Player) viewer).getLevel(), level);
			}

			event.getInventory().setItem(1, new ItemStack(Material.INK_SACK, level, (byte) 4));
			return;
		}

		event.getInventory().setItem(1, new ItemStack(Material.AIR));
	}

	@EventHandler
	public void onInventoryClick(final InventoryClickEvent event)
	{
		if (event.getClickedInventory() == null || event.getClickedInventory().getType() != InventoryType.ENCHANTING)
		{
			return;
		}

		if (event.getSlot() == 1)
		{
			event.setCancelled(true);
			return;
		}

		if (event.getSlot() != 2)
		{
			return;
		}

		Bukkit.getScheduler().scheduleSyncDelayedTask(getPlugin(), new Runnable()
		{
			public void run()
			{
				Inventory inv = event.getInventory();

				if (inv.getViewers().isEmpty())
				{
					return;
				}

				int level = 0;

				for (HumanEntity viewer : inv.getViewers())
				{
					level = Math.max(((Player) viewer).getLevel(), level);
				}

				event.getInventory().setItem(1, new ItemStack(Material.INK_SACK, level, (byte) 4));
			}
		});
	}

	@EventHandler
	public void updateInventoryQueue(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
			return;

		for (final Player player : _inventoryQueue.keySet())
		{
			for (final String item : _inventoryQueue.get(player).keySet())
			{
				final int count = _inventoryQueue.get(player).get(item);

				addItemToInventoryForOffline(new Callback<Boolean>()
				{
					public void run(Boolean success)
					{
						if (!success)
						{
							System.out.println("Add item to Inventory FAILED for " + player);

							if (_items.containsKey(item))
							{
								Get(player).addItem(new ClientItem(_items.get(item), -count));
							}
						}
					}
				}, player.getUniqueId(), item, count);
			}

			// Clean
			_inventoryQueue.get(player).clear();
		}

		// Clean
		_inventoryQueue.clear();
	}

	@Override
	protected ClientInventory addPlayer(UUID uuid)
	{
		return new ClientInventory();
	}

	@Override
	public void addCommands()
	{
		addCommand(new GiveItemCommand(this));
	}

	@Override
	public void processLoginResultSet(String playerName, UUID uuid, int accountId, ResultSet resultSet) throws SQLException
	{
		Set(uuid, _repository.loadClientInformation(resultSet, _itemIdNameMap));
	}

	@Override
	public String getQuery(int accountId, String uuid, String name)
	{
		return "SELECT ai.itemId, count FROM accountInventory AS ai WHERE ai.accountId = '" + accountId + "';";
	}
}
