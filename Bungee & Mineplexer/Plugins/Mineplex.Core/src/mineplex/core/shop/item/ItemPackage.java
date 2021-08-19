package mineplex.core.shop.item;

import java.util.Arrays;
import java.util.List;

import mineplex.core.account.CoreClient;
import mineplex.core.common.util.InventoryUtil;
import net.minecraft.server.v1_8_R3.IInventory;

import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftInventory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ItemPackage implements ISalesPackage
{
    private ShopItem _shopItem;
    private boolean _restrictToHotbar;
    private int _gemCost;
    private boolean _free;
    private int _salesPackageId;

    public ItemPackage(ShopItem shopItem, int gemCost, boolean isFree, int salesPackageId)
    {
        this(shopItem, true, gemCost, isFree, salesPackageId);
    }
    
    public ItemPackage(ShopItem shopItem, boolean restrictToHotbar, int gemCost, boolean isFree, int salesPackageId)
    {
        _shopItem = shopItem;
        _restrictToHotbar = restrictToHotbar;
        _gemCost = gemCost;
        _free = isFree;
        _salesPackageId = salesPackageId;
    }

    @Override
    public String GetName()
    {
    	return _shopItem.GetName();
    }
    
    @Override
    public int GetSalesPackageId()
    {
    	return _salesPackageId;
    }
    
    public int GetGemCost()
    {
        return _gemCost;
    }
    
    @Override
    public boolean IsFree()
    {
    	return _free;
    }

    @Override
    public boolean CanFitIn(CoreClient player)
    {
        if (_shopItem.IsLocked() && !IsFree())
            return false;
        
        for (ItemStack itemStack : player.GetPlayer().getInventory())
        {
            if (itemStack != null && itemStack.getType() == _shopItem.getType() && (itemStack.getAmount() + _shopItem.getAmount()) <= (itemStack.getType() == Material.ARROW ? itemStack.getMaxStackSize() : 1))
            {
                return true;
            }
        }
        
        if (_gemCost == 0)
        	return true;
        
        if (InventoryUtil.first((CraftInventory)player.GetPlayer().getInventory(), _restrictToHotbar ? 9 : player.GetPlayer().getInventory().getSize(), null, true) == -1)
        	return false;
        else
        	return true;
    }

    @Override 
    public void DeliverTo(Player player)
    {
        ShopItem shopItem = _shopItem.clone();
        shopItem.SetDeliverySettings(); 
        
        if (shopItem.getType() == Material.ARROW)
        {
        	// int firstEmpty = player.getInventory().firstEmpty();
        	
            player.getInventory().addItem(shopItem);
            
            /* TODO default item?
            if (player.getInventory().firstEmpty() != firstEmpty)
            {
            	player.PutDefaultItem(player.getInventory().getItem(firstEmpty), firstEmpty);
            }
            
            for (Entry<Integer, ? extends ItemStack> entry : player.getInventory().all(Material.ARROW).entrySet())
    		{
            	player.PutDefaultItem(entry.getValue().clone(), entry.getKey());
    		}
    		*/
        }
        else
        {
            int emptySlot = player.getInventory().firstEmpty();
            
            player.getInventory().setItem(emptySlot, shopItem);
            // TODO default ? player.PutDefaultItem(shopItem.clone(), emptySlot);
        }
    }
    
    @Override 
    public void DeliverTo(Player player, int slot)
    {
        ShopItem shopItem = _shopItem.clone();
        shopItem.SetDeliverySettings();
        
        player.getInventory().setItem(slot, shopItem);
        // TODO default? player.PutDefaultItem(shopItem.clone(), slot);
    }
    
    @Override
    public void PurchaseBy(CoreClient player)
    {
        DeliverTo(player.GetPlayer());
    }

    @Override
    public int ReturnFrom(CoreClient player)
    {
        if (_shopItem.IsDisplay())
            return 0;
        
        ShopItem shopItem = _shopItem.clone();
        shopItem.SetDeliverySettings();
        
        int count = 0;
        
        count = InventoryUtil.getCountOfObjectsRemoved((CraftInventory)player.GetPlayer().getInventory(), 9, (ItemStack)shopItem);
        
        /* TODO default
        for (int i=0; i < 9; i++)
        {
        	player.Class().PutDefaultItem(player.Class().GetInventory().getItem(i), i);
        }
        */
        
        return count;
    }
    
    @Override
    public List<Integer> AddToCategory(IInventory inventory, int slot)
    {
        inventory.setItem(slot, _shopItem.getHandle());        
        
        return Arrays.asList(slot);
    }

    public ShopItem GetItem()
    {
        return _shopItem;
    }
}
