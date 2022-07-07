package nautilus.game.arcade.game.games.survivalgames.kit.necroinventory;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilItem;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.itemstack.ItemStackFactory;

public class NecroInventoryPage
{
	public enum EquipmentType
	{
		HELMET("Helmet"),
		CHESTPLATE("Chestplate"),
		LEGGINGS("Leggings"),
		BOOTS("Boots"),
		WEAPON("Weapon");

		private String _name;

		EquipmentType(String name)
		{
			_name = name;
		}

		public String getName()
		{
			return _name;
		}
	}

	private Map<Integer, EquipmentType> slots;

	private Player _player;
	private Skeleton _minion;

	private Inventory _inventory;

	public NecroInventoryPage(Player player, Skeleton minion)
	{
		slots = new HashMap<>();
		slots.put(0, EquipmentType.HELMET);
		slots.put(1, EquipmentType.CHESTPLATE);
		slots.put(2, EquipmentType.LEGGINGS);
		slots.put(3, EquipmentType.BOOTS);
		slots.put(4, EquipmentType.WEAPON);

		_player = player;
		_minion = minion;
	}

	// Get the player name, from "Skeletal PLAYERNAME"
	private String getTitle()
	{
		return _minion.getCustomName().split(" ")[1] + " - Inventory";
	}

	private ItemStack getButtonEmpty(String inventoryType)
	{
		return new ItemBuilder(Material.STAINED_GLASS_PANE)
				.setData(DyeColor.GRAY.getWoolData())
				.setTitle(C.cRed + "No " + inventoryType + " equipped")
				.addLore(C.mBody + "Drag an item onto this slot", "to equip it.")
				.build();
	}

	private ItemStack getButton(ItemStack stack, String inventoryType)
	{
		if (stack != null && stack.getType() != Material.AIR)
		{
			return stack;
		}

		return getButtonEmpty(inventoryType);
	}

	public void openInventory()
	{
		Inventory inventory = Bukkit.createInventory(_player, InventoryType.HOPPER, getTitle());

		EntityEquipment equipment = _minion.getEquipment();

		inventory.setItem(0, getButton(equipment.getHelmet(), slots.get(0).getName()));
		inventory.setItem(1, getButton(equipment.getChestplate(), slots.get(1).getName()));
		inventory.setItem(2, getButton(equipment.getLeggings(), slots.get(2).getName()));
		inventory.setItem(3, getButton(equipment.getBoots(), slots.get(3).getName()));
		inventory.setItem(4, getButton(equipment.getItemInHand(), slots.get(4).getName()));

		_inventory = inventory;

		_player.openInventory(inventory);
	}

	public boolean matchesInventory(Inventory inventory)
	{
		return inventory != null && _inventory != null && inventory.equals(_inventory);
	}

	private boolean isValidItem(ItemStack item, EquipmentType equipmentType)
	{
		switch (equipmentType)
		{
			case HELMET:
				return UtilItem.isHelmet(item);
			case CHESTPLATE:
				return UtilItem.isChestplate(item);
			case LEGGINGS:
				return UtilItem.isLeggings(item);
			case BOOTS:
				return UtilItem.isBoots(item);
			case WEAPON:
				return true;
			default:
				return false;
		}
	}

	private void setEquipment(ItemStack item, EquipmentType equipmentType)
	{
		EntityEquipment equipment = _minion.getEquipment();

		switch (equipmentType)
		{
			case HELMET:
				equipment.setHelmet(item);
				break;
			case CHESTPLATE:
				equipment.setChestplate(item);
				break;
			case LEGGINGS:
				equipment.setLeggings(item);
				break;
			case BOOTS:
				equipment.setBoots(item);
				break;
			case WEAPON:
				equipment.setItemInHand(item);
				break;
		}
	}

	private void equip(ItemStack item, int slot)
	{
		if (!slots.containsKey(slot))
		{
			return;
		}

		_inventory.setItem(slot, item);

		EquipmentType equipmentType = slots.get(slot);

		setEquipment(item, equipmentType);

		_player.sendMessage(F.main("Game", C.mBody + "Your minion equipped " + C.cYellow + ItemStackFactory.Instance.GetItemStackName(item) + C.mBody + "."));
		_player.playSound(_player.getLocation(), Sound.HORSE_ARMOR, 1f, 1f);
	}

	private void unequip(int slot)
	{
		if (!slots.containsKey(slot))
		{
			return;
		}

		ItemStack old = _inventory.getItem(slot);

		EquipmentType equipmentType = slots.get(slot);

		_inventory.setItem(slot, getButtonEmpty(equipmentType.getName()));

		ItemStack air = new ItemStack(Material.AIR);

		setEquipment(air, equipmentType);

		_player.sendMessage(F.main("Game", C.mBody + "Your minion unequipped " + C.cYellow + ItemStackFactory.Instance.GetItemStackName(old) + C.mBody + "."));
	}

	private boolean isPickup(InventoryAction action)
	{
		return (action == InventoryAction.PICKUP_ALL || action == InventoryAction.PICKUP_HALF || action == InventoryAction.PICKUP_ONE || action == InventoryAction.PICKUP_SOME);
	}

	private void playDeny(Player player)
	{
		player.playSound(player.getLocation(), Sound.ITEM_BREAK, 1, .6f);
	}

	public void inventoryClick(InventoryClickEvent event)
	{
		if (_inventory == null || event.getInventory() == null || event.getClickedInventory() == null)
		{
			return;
		}

		if (!event.getInventory().equals(_inventory))
		{
			return;
		}

		if (event.getWhoClicked() == null || !(event.getWhoClicked() instanceof Player))
		{
			return;
		}

		Player player = (Player) event.getWhoClicked();

		if (!event.getClickedInventory().equals(_inventory))
		{
			// It's the player's inventory
			if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY)
			{
				ItemStack shiftClickedItem = event.getClickedInventory().getItem(event.getSlot());

				for (int slot : slots.keySet())
				{
					EquipmentType equipmentType = slots.get(slot);

					if (isValidItem(shiftClickedItem, equipmentType))
					{
						if (_inventory.getItem(slot).getType() != Material.STAINED_GLASS_PANE)
						{
							player.getInventory().setItem(event.getSlot(), _inventory.getItem(slot));
						}
						else
						{
							player.getInventory().setItem(event.getSlot(), new ItemStack(Material.AIR));
						}

						equip(shiftClickedItem, slot);
						event.setCancelled(true);
						return;
					}
				}

				playDeny(player);
				event.setCancelled(true);
			}

			return;
		}

		boolean isPlaceholder = _inventory.getItem(event.getSlot()).getType() == Material.STAINED_GLASS_PANE;

		// If the user is picking up an item
		// meaning they are NOT swapping one...
		if (isPickup(event.getAction()))
		{
			// If it's a placeholder pane
			// cancel it for obvious reasons
			if (isPlaceholder)
			{
				playDeny(player);
			}
			// Otherwise it's OK to pick up,
			// so unequip that item.
			else
			{
				_player.setItemOnCursor(_inventory.getItem(event.getSlot()));
				unequip(event.getSlot());
			}

			event.setCancelled(true);
			return;
		}

		// If the player has an item in their cursor and is
		// trying to swap it with something in the hopper inventory
		if (event.getAction() == InventoryAction.SWAP_WITH_CURSOR)
		{
			// If the item in the player's
			// cursor is allowed to go into
			// that gear slot, put it in there.
			if (isValidItem(event.getCursor(), slots.get(event.getSlot())))
			{
				equip(event.getCursor(), event.getSlot());

				// Don't give them the placeholder item on their cursor...
				if (!isPlaceholder)
				{
					player.setItemOnCursor(_inventory.getItem(event.getSlot()));
				}
				else
				{
					player.setItemOnCursor(new ItemStack(Material.AIR));
				}
			}
			// If the cursor is not empty,
			// don't let the player do anything
			// with it.
			else if (event.getCursor() != null && event.getCursor().getType() != Material.AIR)
			{
				playDeny(player);
				event.setCancelled(true);
				return;
			}
			// Otherwise, let the item in the
			// slot be unequipped because the
			// cursor is empty.
			else if (!isPlaceholder)
			{
				// Set cursor before unequipping so the item is still there
				_player.setItemOnCursor(_inventory.getItem(event.getSlot()));
				unequip(event.getSlot());
			}
			// It is a placeholder, so play the deny
			else
			{
				playDeny(player);
			}

			event.setCancelled(true);
			return;
		}

		// Shift clicking has odd behavior with ghosting items
		// so just disable shift clicking items out of the hopper.
		if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY)
		{
			 _player.sendMessage(F.main("Game", "You can't use shift-click to unequip items."));
			 playDeny(player);
		}

		// No clue what they're trying to do in my inventory,
		// but I haven't caught it so just cancel the event.
		event.setCancelled(true);
	}

	public void inventoryDrag(InventoryDragEvent event)
	{
		if (_inventory == null || event.getInventory() == null)
		{
			return;
		}

		if (!event.getInventory().equals(_inventory))
		{
			return;
		}

		if (!(event.getWhoClicked() instanceof Player) || event.getWhoClicked().equals(_player))
		{
			return;
		}

		event.setCancelled(true);
	}

	public void cleanup()
	{
		_inventory = null;
	}
}
