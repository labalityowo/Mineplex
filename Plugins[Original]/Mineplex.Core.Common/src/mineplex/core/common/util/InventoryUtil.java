package mineplex.core.common.util;

import java.util.HashMap;

import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftInventory;
import org.bukkit.inventory.ItemStack;

public class InventoryUtil
{
    public static HashMap<Integer, ItemStack> removeItem(CraftInventory inventory, int endingSlot, ItemStack... items) 
    {
        HashMap<Integer, ItemStack> leftover = new HashMap<Integer, ItemStack>();

        if (endingSlot >= 54)
            return leftover;

        for (int i = 0; i < items.length; i++) 
        {
            ItemStack item = items[i];
            int toDelete = item.getAmount();

            while (true) 
            {
                int first = first(inventory, endingSlot, item, false);

                if (first == -1) 
                {
                    item.setAmount(toDelete);
                    leftover.put(i, item);
                    break;
                } 
                else 
                {
                    ItemStack itemStack = inventory.getItem(first);
                    int amount = itemStack.getAmount();

                    if (amount <= toDelete) 
                    {
                        toDelete -= amount;
                        inventory.clear(first);
                    } 
                    else 
                    {
                        itemStack.setAmount(amount - toDelete);
                        inventory.setItem(first, itemStack);
                        toDelete = 0;
                    }
                }

                if (toDelete <= 0) 
                    break;
            }
        }
        
        return leftover;
    }
    
    public static int first(CraftInventory craftInventory, int endingSlot, ItemStack item, boolean withAmount) 
    {        
        if (endingSlot >= 54)
            return -1;
        
        ItemStack[] inventory = craftInventory.getContents();
        
        for (int i = 0; i < endingSlot; i++) 
        {
            if (inventory[i] == null) 
            {
            	if (item == null)
            		return i;
            	else
            		continue;
            }
            else if (item == null)
            	continue;

            boolean equals = (item.getTypeId() == inventory[i].getTypeId() && item.getDurability() == inventory[i].getDurability() && item.getEnchantments().equals(inventory[i].getEnchantments()));

            if (equals && withAmount) 
            {
                equals = inventory[i].getAmount() >= item.getAmount();
            } 

            if (equals) 
            {
                return i;
            }
        }
        
        return -1;
    }

	public static int getCountOfObjectsRemoved(CraftInventory getInventory, int i, ItemStack itemStack) 
	{
		int count = 0;
		
        while(getInventory.contains(itemStack.getType(), itemStack.getAmount()) && InventoryUtil.removeItem(getInventory, i, itemStack).size() == 0)
        {
            count++;
        }
        
		return count;
	}

	public static int GetCountOfObjectsRemovedInSlot(CraftInventory getInventory, int slot, ItemStack itemStack) 
	{
		int count = 0;
		ItemStack slotStack = getInventory.getItem(slot);
		
        while(slotStack.getType() == itemStack.getType() && slotStack.getAmount() >= itemStack.getAmount())
        {
        	slotStack.setAmount(slotStack.getAmount() - itemStack.getAmount());
            count++;
        }
        
        if (slotStack.getAmount() == 0)
        	getInventory.setItem(slot, null);
        
		return count;
	}
}
