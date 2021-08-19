package mineplex.core.common.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.function.Predicate;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class UtilInv
{
	private static DullEnchantment DULL_ENCHANTMENT = new DullEnchantment();

	public static void addDullEnchantment(ItemStack itemStack)
	{
		itemStack.addEnchantment(DULL_ENCHANTMENT, 1);
	}
	
	public static void removeDullEnchantment(ItemStack itemStack)
	{
	    itemStack.removeEnchantment(DULL_ENCHANTMENT);
	}
	
	public static DullEnchantment getDullEnchantment()
	{
	    return DULL_ENCHANTMENT;
	}
	
	@SuppressWarnings("deprecation")
	public static boolean insert(Player player, ItemStack stack)
	{
		//CHECK IF FIT
		
		//Insert
		player.getInventory().addItem(stack);
		player.updateInventory();
		return true;
	}
	
	public static boolean contains(Player player, Material item, byte data, int required)
	{
		return contains(player, null, item, data, required);
	}

	public static boolean contains(Player player, String itemNameContains, Material item, byte data, int required)
	{
		return contains(player, itemNameContains, item, data, required, true, true, true);
	}

	public static boolean contains(Player player, String itemNameContains, Material item, byte data, int required, boolean checkArmor, boolean checkCursor, boolean checkCrafting)
	{
		
		for (ItemStack stack : getItems(player, checkArmor, checkCursor, checkCrafting))
		{
			if (required <= 0)
			{
				return true;
			}
			
			if (stack == null)
				continue;
			
			if (stack.getType() != item)
				continue;
			
			if (stack.getAmount() <= 0)
				continue;
			
			if (data >=0 && 
				stack.getData() != null && stack.getData().getData() != data)
				continue;
			
			if (itemNameContains != null && 
				(stack.getItemMeta().getDisplayName() == null || !stack.getItemMeta().getDisplayName().contains(itemNameContains)))
				continue;
			
			required -= stack.getAmount();
		}
		
		if (required <= 0)
		{
			return true;
		}
		
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public static boolean remove(Player player, Material item, byte data, int toRemove) 
	{
		if (!contains(player, item, data, toRemove))
			return false;
		
		for (int i : player.getInventory().all(item).keySet()) 
		{
			if (toRemove <= 0)
				continue;
			
			ItemStack stack = player.getInventory().getItem(i);

			if (stack.getData() == null || stack.getData().getData() == data)
			{
				int foundAmount = stack.getAmount();

				if (toRemove >= foundAmount) 
				{
					toRemove -= foundAmount;
					player.getInventory().setItem(i, null);
				} 

				else 
				{
					stack.setAmount(foundAmount - toRemove);
					player.getInventory().setItem(i, stack);
					toRemove = 0;
				}
			} 
		}
		
		player.updateInventory();
		return true;
	}

	public static void Clear(Player player)
	{
		//player.getOpenInventory().close();
		
		PlayerInventory inv = player.getInventory();
		
		inv.clear();
		inv.setArmorContents(new ItemStack[4]);
		player.setItemOnCursor(new ItemStack(Material.AIR));

		Inventory openInventory = player.getOpenInventory().getTopInventory();

		if (openInventory.getHolder() == player)
		{
			openInventory.clear();
		}

		player.saveData();
	}

	public static ArrayList<ItemStack> getItems(Player player)
	{
		return getItems(player, true, true, true);
	}

	public static ArrayList<ItemStack> getItemsUncloned(Player player)
	{
		return getItems(player, true, true, true, false);
	}

	public static ArrayList<ItemStack> getItems(Player player, boolean getArmor, boolean getCursor, boolean getCrafting)
	{
		return getItems(player, getArmor, getCursor, getCrafting, true);
	}

	public static ArrayList<ItemStack> getItems(Player player, boolean getArmor, boolean getCursor, boolean getCrafting, boolean clone)
	{
		ArrayList<ItemStack> items = new ArrayList<ItemStack>();
		PlayerInventory inv = player.getInventory();

		for (ItemStack item : inv.getContents())
		{
			if (item != null && item.getType() != Material.AIR)
			{
				items.add(clone ? item.clone() : item);
			}
		}

		if (getArmor)
		{
			for (ItemStack item : inv.getArmorContents())
			{
				if (item != null && item.getType() != Material.AIR)
				{
					items.add(clone ? item.clone() : item);
				}
			}
		}

		if (getCursor)
		{
			ItemStack cursorItem = player.getItemOnCursor();

			if (cursorItem != null && cursorItem.getType() != Material.AIR)
				items.add(clone ? cursorItem.clone() : cursorItem);
		}

		if (getCrafting)
		{
			Inventory openInventory = player.getOpenInventory().getTopInventory();

			if (openInventory.getHolder() == player && openInventory.getType() == InventoryType.CRAFTING)
			{
				for (ItemStack item : openInventory.getContents())
				{
					if (item != null && item.getType() != Material.AIR)
					{
						items.add(clone ? item.clone() : item);
					}
				}
			}
		}

		return items;
	}
	
	public static void drop(Player player, boolean clear)
	{
		for (ItemStack cur : getItems(player))
		{
			player.getWorld().dropItemNaturally(player.getLocation(), cur);
		}
		
		if (clear)
			Clear(player);
	}

	@SuppressWarnings("deprecation")
	public static void Update(Entity player) 
	{
		if (!(player instanceof Player))
			return;
		
		((Player)player).updateInventory();
	}

	public static int removeAll(Player player, Material type, byte data) 
	{
		HashSet<ItemStack> remove = new HashSet<ItemStack>();
		int count = 0;
		
		for (ItemStack item : player.getInventory().getContents())
			if (item != null)
				if (item.getType() == type)
					if (data == -1 || item.getData() == null || (item.getData() != null && item.getData().getData() == data))
					{
						count += item.getAmount();
						remove.add(item);
					}
	
		for (ItemStack item : remove)
			player.getInventory().remove(item);	

		return count;
	}
	
	public static byte GetData(ItemStack stack)
	{
		if (stack == null)
			return (byte)0;
		
		if (stack.getData() == null)
			return (byte)0;
		
		return stack.getData().getData();
	}

	public static boolean IsItem(ItemStack item, Material type, byte data)
	{
		return IsItem(item, null, type.getId(), data);
	}
	
	public static boolean IsItem(ItemStack item, String name, Material type, byte data)
	{
		return IsItem(item, name, type.getId(), data);
	}
	
	public static boolean IsItem(ItemStack item, String name, int id, byte data)
	{
		if (item == null)
			return false;
		
		if (item.getTypeId() != id)
			return false;
		
		if (data != -1 && GetData(item) != data)
			return false;
		
		if (name != null && (item.getItemMeta().getDisplayName() == null || !item.getItemMeta().getDisplayName().contains(name)))
			return false;
		
		return true;
	}
	
	public static void DisallowMovementOf(InventoryClickEvent event, String name, Material type, byte data, boolean inform) 
	{
		DisallowMovementOf(event, name, type, data, inform, false);
	}
	
	public static void DisallowMovementOf(InventoryClickEvent event, String name, Material type, byte data, boolean inform, boolean allInventorties) 
	{
		/*
		System.out.println("Inv Type: " + event.getInventory().getType());
		System.out.println("Click: " + event.getClick());
		System.out.println("Action: " + event.getAction());
		 
		System.out.println("Slot: " + event.getSlot());
		System.out.println("Slot Raw: " + event.getRawSlot());
		System.out.println("Slot Type: " + event.getSlotType());
		
		System.out.println("Cursor: " + event.getCursor());
		System.out.println("Current: " + event.getCurrentItem());
		
		System.out.println("View Type: " + event.getView().getType());
		System.out.println("View Top Type: " + event.getView().getTopInventory().getType());
		System.out.println("HotBar Button: " + event.getHotbarButton());
		*/
		
		//Do what you want in Crafting Inv
		if (!allInventorties && event.getInventory().getType() == InventoryType.CRAFTING)
			return;
		
		//Hotbar Swap
		if (event.getAction() == InventoryAction.HOTBAR_SWAP ||
			event.getAction() == InventoryAction.HOTBAR_MOVE_AND_READD)
		{
			boolean match = false;
			
			if (IsItem(event.getCurrentItem(), name, type, data))
				match = true;

			if (IsItem(event.getWhoClicked().getInventory().getItem(event.getHotbarButton()), name, type, data))
				match = true;
			
			if (!match) 
				return; 
			
			//Inform
			if (inform)
			{
				UtilPlayer.message(event.getWhoClicked(), F.main("Inventory", "You cannot hotbar swap " + F.item(name) + "."));
			}
			event.setCancelled(true);
		}
		//Other
		else
		{
			if (event.getCurrentItem() == null)
				return;

			IsItem(event.getCurrentItem(), name, type, data);
			
			//Type
			if (!IsItem(event.getCurrentItem(), name, type, data))
				return;
			//Inform
			if (inform)
			{
				UtilPlayer.message(event.getWhoClicked(), F.main("Inventory", "You cannot move " + F.item(name) + "."));
			}
			event.setCancelled(true);
		}
	}

	public static void UseItemInHand(Player player)
	{
		if (player.getItemInHand().getAmount() > 1)
			player.getItemInHand().setAmount(player.getItemInHand().getAmount() - 1);
		else
			player.setItemInHand(null);
		
		Update(player);
	}
	
	public static int getAmount(Player player, Material mat)
	{
		return getAmount(player, mat, true);
	}
	
	public static int getAmount(Player player, Material mat, boolean includeArmorAndCursorAndCrafting)
	{
		int amount = 0;

		for (ItemStack item : getItems(player, includeArmorAndCursorAndCrafting, includeArmorAndCursorAndCrafting,
				includeArmorAndCursorAndCrafting))
		{
			if (item.getType() == mat)
			{
				amount += item.getAmount();
			}
		}
		
		return amount;
	}

	public static ItemStack decrement(ItemStack item)
	{
		ItemStack newItem;
		
		if (item.getAmount() == 1)
		{
			newItem = null;
		}
		else
		{
			newItem = item;
			newItem.setAmount(newItem.getAmount() - 1);
		}
		
		return newItem;
	}

	public static boolean HasSpace(Player player, Material material, int amount)
	{
		int slotsFree = 0;
		
		for (int slot = 0; slot < player.getInventory().getSize(); slot++)
		{
			if (player.getInventory().getItem(slot) == null)
			{
				slotsFree++;
				
				if (slotsFree >= amount / 64)
				{
					return true;
				}
			}
			else if (player.getInventory().getItem(slot).getType().equals(material) && amount <= (64 - player.getInventory().getItem(slot).getAmount()))
			{
				return true;
			}
		}
		
		return false;
	}
	
	public static boolean hasSpace(Player player, int slots)
	{
		int slotsFree = 0;
		
		for (int slot = 0; slot < player.getInventory().getSize(); slot++)
		{
			if (player.getInventory().getItem(slot) == null)
			{
				slotsFree++;
			}
		}
		
		return slotsFree >= slots;
	}

	public static void give(Player player, Material material)
	{
		give(player, material, 1);
	}
	
	public static void give(Player player, Material material, int amount)
	{
		give(player, material, amount, (byte) 0);
	}
	
	public static void give(Player shooter, Material material, int amount, byte data)
	{
		shooter.getInventory().addItem(new ItemStack(material, amount, data));
	}

	/**
	 * Checks if an InventoryClickEvent should be cancelled based on the given predicate. The predicate should return true
	 * if the given ItemStack is one that should not be moved
	 */
	public static boolean shouldCancelEvent(InventoryClickEvent event, Predicate<ItemStack> predicate)
	{
		List<ItemStack> check = new ArrayList<>();

		if (event.getHotbarButton() != -1)
		{
			check.add(event.getWhoClicked().getInventory().getItem(event.getHotbarButton())); // Check item in hotbar slot
			ItemStack other = event.getCurrentItem();
			if (other != null && other.getType() != Material.AIR)
				check.add(other); // Check the other slot (where the cursor is)
		}
		else
		{
			ItemStack other = event.getCurrentItem();
			if (other != null && other.getType() != Material.AIR)
				check.add(other); // Check the clicked item
		}

		for (ItemStack item : check)
		{
			if (predicate.test(item))
				return true;
		}

		return false;
	}
}
