package mineplex.game.clans.clans.invsee.ui;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftInventory;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.meta.ItemMeta;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilText;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.clans.invsee.InvseeManager;
import mineplex.game.clans.clans.invsee.InvseeModifyOnlineInventoryEvent;
import net.minecraft.server.v1_8_R3.ContainerPlayer;
import net.minecraft.server.v1_8_R3.IInventory;
import net.minecraft.server.v1_8_R3.MinecraftServer;
import net.minecraft.server.v1_8_R3.NBTCompressedStreamTools;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.NBTTagList;
import net.minecraft.server.v1_8_R3.PlayerInventory;
import net.minecraft.server.v1_8_R3.WorldNBTStorage;

public class InvseeInventory implements Listener
{
	private final InvseeManager _invseeManager;

	// This is the UUID of the target player. This should stay constant. Use this for comparison
	private final UUID _uuid;
	// This is the current player. It will switch when the player joins/quits
	private OfflinePlayer _targetPlayer;
	// This is the inventory of said player. If the player is not online then this inventory is a fake PlayerInventory
	private net.minecraft.server.v1_8_R3.PlayerInventory _playerInventory;
	// This is the set of all admins viewing this player
	private Set<Player> _viewers = new HashSet<>();
	// This is the inventory that all admins will be looking at
	private Inventory _inventory;
	// This is whether the target player should be allowed to open any inventories
	private boolean _canOpenInventory = true;

	public InvseeInventory(InvseeManager manager, OfflinePlayer player)
	{
		_invseeManager = manager;
		_uuid = player.getUniqueId();
		_targetPlayer = player;

		_inventory = UtilServer.getServer().createInventory(null, 6 * 9, player.getName());

		for (int index = 38; index < 45; index++)
		{
			_inventory.setItem(index, ItemStackFactory.Instance.CreateStack(Material.STAINED_GLASS_PANE, (byte) 14, 1, C.Bold));
		}
		_inventory.setItem(47, ItemStackFactory.Instance.CreateStack(Material.STONE_BUTTON, (byte) 0, 1, C.cGreenB + "Inventory Control", generateLore(_canOpenInventory)));

		_inventory.setItem(48, ItemStackFactory.Instance.CreateStack(Material.STAINED_GLASS_PANE, (byte) 14, 1, C.Bold));

		UtilServer.RegisterEvents(this);
		updateInventory();
		update(true);
	}

	private String[] generateLore(boolean canOpenInventory)
	{
		return UtilText.splitLinesToArray(new String[]{
				C.cYellow + "Left-Click" + C.cWhite + " to force close any open inventories the target has open",
				"",
				C.cYellow + "Right-Click" + C.cWhite + " to " + (canOpenInventory ? "disable inventory opening" : "enable inventory opening")
		}, LineFormat.LORE);
	}

	/*
	 * Add the player to the list of viewers and open the inventory for him
	 */
	public void addAndShowViewer(Player requester)
	{
		_viewers.add(requester);
		requester.openInventory(_inventory);
	}

	/*
	 * Check whether the given player is currently viewing this InvseeInventory
	 */
	public boolean isViewer(Player check)
	{
		return _viewers.contains(check);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void on(PlayerJoinEvent event)
	{
		if (_uuid.equals(event.getPlayer().getUniqueId()))
		{
			_targetPlayer = event.getPlayer();
			updateInventory();
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void on(PlayerQuitEvent event)
	{
		// If a viewer quit, this will clean it up
		_viewers.remove(event.getPlayer());
		if (_viewers.size() == 0)
		{
			_invseeManager.close(_uuid);
		}
		else
		{
			// This should always work
			if (_uuid.equals(event.getPlayer().getUniqueId()))
			{
				_targetPlayer = Bukkit.getOfflinePlayer(_uuid);
				updateInventory();
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void on(InventoryOpenEvent event)
	{
		if (event.getPlayer().getUniqueId().equals(_uuid) && !_canOpenInventory)
		{
			Bukkit.getScheduler().runTaskLater(UtilServer.getPlugin(), () -> event.getPlayer().closeInventory(), 20);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void on(InventoryCloseEvent event)
	{
		if (_inventory.equals(event.getInventory()))
		{
			_viewers.remove(event.getPlayer());
			if (_viewers.size() == 0)
			{
				_invseeManager.close(_uuid);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void on(InventoryDragEvent event)
	{
		if (_inventory.equals(event.getInventory()))
		{
			ClansManager.getInstance().runSync(() ->
			{
				update(false);
				saveInventory();
			});
		}
		else if (event.getWhoClicked().getUniqueId().equals(_targetPlayer.getUniqueId()))
		{
			ClansManager.getInstance().runSync(() ->
			{
				update(true);
				saveInventory();
			});
		}
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void on(InventoryClickEvent event)
	{
		if (_inventory.equals(event.getClickedInventory()))
		{
			if (event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.STAINED_GLASS_PANE
					&& event.getCurrentItem().getItemMeta() != null
					&& event.getCurrentItem().getItemMeta().getDisplayName() != null
					&& event.getCurrentItem().getItemMeta().getDisplayName().equals(C.Bold))
			{
				event.setCancelled(true);
				return;
			}
			if (event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.STONE_BUTTON
					&& event.getCurrentItem().getItemMeta() != null
					&& event.getCurrentItem().getItemMeta().getDisplayName() != null
					&& event.getCurrentItem().getItemMeta().getDisplayName().equals(C.cGreenB + "Inventory Control"))
			{
				event.setCancelled(true);
				if (event.getClick() == ClickType.LEFT)
				{
					if (_targetPlayer.isOnline())
					{
						((Player) _targetPlayer).closeInventory();
					}
				}
				else if (event.getClick() == ClickType.RIGHT)
				{
					if (_targetPlayer.isOnline())
					{
						_canOpenInventory = !_canOpenInventory;
						ItemMeta meta = event.getCurrentItem().getItemMeta();
						meta.setLore(Arrays.asList(generateLore(_canOpenInventory)));
						event.getCurrentItem().setItemMeta(meta);
						if (!_canOpenInventory)
						{
							((Player) _targetPlayer).closeInventory();
						}
					}
				}
				return;
			}

			if (MAPPING_INVENTORY_REVERSE.containsKey(event.getRawSlot()))
			{
				ClansManager.getInstance().runSync(() ->
				{
					update(false);
					saveInventory();
				});
			}
			else if (MAPPING_CRAFTING_REVERSE.containsKey(event.getRawSlot()))
			{
				if (_targetPlayer.isOnline())
				{
					ClansManager.getInstance().runSync(() ->
					{
						update(false);
					});
				}
			}
			else if (event.getRawSlot() == 49)
			{
				if (_targetPlayer.isOnline())
				{
					ClansManager.getInstance().runSync(() ->
					{
						update(false);
					});
				}
			}
		}
		else
		{
			if (event.getWhoClicked().getUniqueId().equals(_targetPlayer.getUniqueId()))
			{
				ClansManager.getInstance().runSync(() ->
				{
					update(true);
				});
			}
		}
	}

	/*
	 * Updates the inventory instance
	 */
	private void updateInventory()
	{
		if (_targetPlayer.isOnline())
		{
			_playerInventory = ((CraftPlayer) _targetPlayer).getHandle().inventory;
		}
		else
		{
			NBTTagCompound compound = ((WorldNBTStorage) MinecraftServer.getServer().worlds.get(0).getDataManager()).getPlayerData(_uuid.toString());
			// Should not matter if null
			_playerInventory = new PlayerInventory(null);
			if (compound.hasKeyOfType("Inventory", 9))
			{
				_playerInventory.b(compound.getList("Inventory", 10));
			}
		}
	}

	private void saveInventory()
	{
		if (!_targetPlayer.isOnline())
		{
			try
			{
				WorldNBTStorage worldNBTStorage = ((WorldNBTStorage) MinecraftServer.getServer().worlds.get(0).getDataManager());
				NBTTagCompound compound = worldNBTStorage.getPlayerData(_uuid.toString());
				compound.set("Inventory", new NBTTagList());
				_playerInventory.a(compound.getList("Inventory", 10));
				File file = new File(worldNBTStorage.getPlayerDir(), _targetPlayer.getUniqueId().toString() + ".dat.tmp");
				File file1 = new File(worldNBTStorage.getPlayerDir(), _targetPlayer.getUniqueId().toString() + ".dat");
				NBTCompressedStreamTools.a(compound, new FileOutputStream(file));
				if (file1.exists())
				{
					file1.delete();
				}

				file.renameTo(file1);
			}
			catch (Exception var5)
			{
				_invseeManager.log("Failed to save player inventory for " + _targetPlayer.getName());
				for (Player player : _viewers)
				{
					UtilPlayer.message(player, F.main("Invsee", "Could not save inventory for " + _targetPlayer.getName()));
				}
				var5.printStackTrace(System.out);
			}
		}
	}

	/**
	 * Update the player inventory and invsee inventory.
	 *
	 * @param targetClick If true, then it means the player being invseen has modified their inventory. Otherwise, it's the admin who has modified something
	 */
	private void update(boolean targetClick)
	{
		IInventory iInventoryThis = ((CraftInventory) _inventory).getInventory();

		if (targetClick)
		{
			// Update items on hotbar
			for (int otherSlot = 0; otherSlot < 9; otherSlot++)
			{
				iInventoryThis.setItem(MAPPING_INVENTORY.get(otherSlot), _playerInventory.getItem(otherSlot));
			}
			// Update main inventory
			for (int otherSlot = 9; otherSlot < 36; otherSlot++)
			{
				iInventoryThis.setItem(MAPPING_INVENTORY.get(otherSlot), _playerInventory.getItem(otherSlot));
			}
			// Update armor
			for (int otherSlot = 36; otherSlot < 40; otherSlot++)
			{
				iInventoryThis.setItem(MAPPING_INVENTORY.get(otherSlot), _playerInventory.getItem(otherSlot));
			}

			if (_targetPlayer.isOnline())
			{
				ContainerPlayer containerPlayer = (ContainerPlayer) ((CraftPlayer) _targetPlayer).getHandle().defaultContainer;
				for (int craftingIndex = 0; craftingIndex < 4; craftingIndex++)
				{
					iInventoryThis.setItem(MAPPING_CRAFTING.get(craftingIndex), containerPlayer.craftInventory.getItem(craftingIndex));
				}
			}
			iInventoryThis.setItem(49, _playerInventory.getCarried());
		}
		else
		{
			if (_targetPlayer.isOnline())
			{
				UtilServer.CallEvent(new InvseeModifyOnlineInventoryEvent((Player)_targetPlayer));
			}
			
			// Update items on hotbar
			for (int otherSlot = 0; otherSlot < 9; otherSlot++)
			{
				_playerInventory.setItem(otherSlot, iInventoryThis.getItem(MAPPING_INVENTORY.get(otherSlot)));
			}
			// Update main inventory
			for (int otherSlot = 9; otherSlot < 36; otherSlot++)
			{
				_playerInventory.setItem(otherSlot, iInventoryThis.getItem(MAPPING_INVENTORY.get(otherSlot)));
			}
			// Update armor
			for (int otherSlot = 36; otherSlot < 40; otherSlot++)
			{
				_playerInventory.setItem(otherSlot, iInventoryThis.getItem(MAPPING_INVENTORY.get(otherSlot)));
			}

			if (_targetPlayer.isOnline())
			{
				ContainerPlayer containerPlayer = (ContainerPlayer) ((CraftPlayer) _targetPlayer).getHandle().defaultContainer;
				for (int craftingIndex = 0; craftingIndex < 4; craftingIndex++)
				{
					containerPlayer.craftInventory.setItem(craftingIndex, iInventoryThis.getItem(MAPPING_CRAFTING.get(craftingIndex)));
				}
			}
			_playerInventory.setCarried(iInventoryThis.getItem(49));
		}
		for (Player viewing : _viewers)
		{
			viewing.updateInventory();
		}
		if (_targetPlayer.isOnline())
		{
			((Player) _targetPlayer).updateInventory();
		}
	}

	// Maps slot indices of player inventories to slot indices of double chests
	private static final Map<Integer, Integer> MAPPING_INVENTORY = new HashMap<>();
	private static final Map<Integer, Integer> MAPPING_INVENTORY_REVERSE = new HashMap<>();
	// Maps slot indices of player inventories to slot indices of crafting window
	private static final Map<Integer, Integer> MAPPING_CRAFTING = new HashMap<>();
	private static final Map<Integer, Integer> MAPPING_CRAFTING_REVERSE = new HashMap<>();

	static
	{
		int[] inventoryMapping = new int[]
				{
						27, 28, 29, 30, 31, 32, 33, 34, 35, //Hotbar
						0, 1, 2, 3, 4, 5, 6, 7, 8, // Top row inventory
						9, 10, 11, 12, 13, 14, 15, 16, 17, //Second row inventory
						18, 19, 20, 21, 22, 23, 24, 25, 26, //Third row inventory
						53, 52, 51, 50 //Armor
				};
		int[] craftingMapping = new int[]
				{
						36, 37, //Top crafting
						45, 46  //Bottom crafting
				};
		for (int i = 0; i < inventoryMapping.length; i++)
		{
			MAPPING_INVENTORY.put(i, inventoryMapping[i]);
			MAPPING_INVENTORY_REVERSE.put(inventoryMapping[i], i);
		}

		for (int i = 0; i < craftingMapping.length; i++)
		{
			MAPPING_CRAFTING.put(i, craftingMapping[i]);
			MAPPING_CRAFTING_REVERSE.put(craftingMapping[i], i);
		}
	}
}