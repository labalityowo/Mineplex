package mineplex.core.shop.item;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilInv;

public class ShopItem extends ItemStack
{
	protected String _name;
	private String _deliveryName;
	protected String[] _lore;
	private int _deliveryAmount;
	private boolean _locked;
	private boolean _displayItem;

	public ShopItem(ItemStack itemStack, boolean locked, boolean displayItem)
	{
		this(itemStack, itemStack.getItemMeta().getDisplayName(), itemStack.getItemMeta().getDisplayName(), 1, locked, displayItem);
	}
	
	public ShopItem(ItemStack itemStack, String name, String deliveryName, int deliveryAmount, boolean locked, boolean displayItem)
	{
		super(itemStack);

		_name = name;
		_deliveryName = deliveryName;
		_displayItem = displayItem;
		_deliveryAmount = deliveryAmount;
		if (itemStack.getItemMeta().hasLore())
	        _lore = itemStack.getItemMeta().getLore().toArray(new String[0]);
		else 
		    _lore = new String[0];

		UpdateVisual(true);
	}
	
	public net.minecraft.server.v1_8_R3.ItemStack getHandle() {
	    return CraftItemStack.asNMSCopy(this);
	}

	public ShopItem(Material type, String name, int deliveryAmount, boolean locked)
	{
		this(type, name, null, deliveryAmount, locked);
	}
	
	public ShopItem(int type, String name, int deliveryAmount, boolean locked)
	{
		this(type, name, null, deliveryAmount, locked);
	}

	public ShopItem(Material type, String name, String[] lore, int deliveryAmount, boolean locked)
	{
		this(type, name, lore, deliveryAmount, locked, false);
	}
	
	public ShopItem(int type, String name, String[] lore, int deliveryAmount, boolean locked)
	{
		this(type, name, lore, deliveryAmount, locked, false);
	}

	public ShopItem(Material type, String name, String[] lore, int deliveryAmount, boolean locked, boolean displayItem)
	{
		this(type, (byte)0, name, null, lore, deliveryAmount, locked, displayItem);
	}
	
	public ShopItem(int type, String name, String[] lore, int deliveryAmount, boolean locked, boolean displayItem)
	{
		this(type, (byte)0, name, null, lore, deliveryAmount, locked, displayItem);
	}

	public ShopItem(Material type, byte data, String name, String[] lore, int deliveryAmount, boolean locked, boolean displayItem)
	{
		this(type, data, name, null, lore, deliveryAmount, locked, displayItem);
	}
	
	public ShopItem(int type, byte data, String name, String[] lore, int deliveryAmount, boolean locked, boolean displayItem)
	{
		this(type, data, name, null, lore, deliveryAmount, locked, displayItem);
	}

	public ShopItem(Material type, byte data, String name, String deliveryName, String[] lore, int deliveryAmount, boolean locked, boolean displayItem)
	{
		this(type.getId(), data, name, deliveryName, lore, deliveryAmount, locked, displayItem);
	}
	
	public ShopItem(int type, byte data, String name, String deliveryName, String[] lore, int deliveryAmount, boolean locked, boolean displayItem)
	{
		super(type, Math.max(deliveryAmount, 1), data, null);

		_name = name;
		_deliveryName = deliveryName;
		_lore = lore;
		_displayItem = displayItem;
		_deliveryAmount = deliveryAmount;
		_locked = locked;

		UpdateVisual(false);

		setAmount(Math.max(deliveryAmount, 1)); 
	}

	public boolean IsLocked()
	{
		return _locked;
	}

	public void SetDeliverySettings()
	{
		setAmount(_deliveryAmount);

		//Delivery Name
		if (_deliveryName != null)
		{
		    ItemMeta meta = getItemMeta();
		    meta.setDisplayName(_deliveryName);
		    setItemMeta(meta);
		}
	}

	public ShopItem clone()
	{
		return new ShopItem(super.clone(), _name, _deliveryName, _deliveryAmount, _locked, _displayItem);
	}

	protected void UpdateVisual(boolean clone)
	{
        ItemMeta meta = getItemMeta();
		if (!clone)
		{
		    meta.setDisplayName((_locked && !_displayItem? C.cRed:C.cGreen) + C.Bold + _name);
		}

		ArrayList<String> lore = new ArrayList<String>();

		if (_lore != null)
		{
			for (String line : _lore)
			{
				if (line != null && !line.isEmpty())
					lore.add(line);
			}
		}
		meta.setLore(lore);;

        setItemMeta(meta);
	}

	public boolean IsDisplay()
	{
		return _displayItem;
	}
	
	public ShopItem addGlow()
	{
		UtilInv.addDullEnchantment(this);
		
		return this;
	}

	public ShopItem SetLocked(boolean owns)
	{
		_locked = owns;
		UpdateVisual(false);
		
		return this;
	}

	public String GetName()
	{
		return _name;
	}
	
	public ShopItem SetName(String name)
	{
		_name = name;
		
		return this;
	}

	public ShopItem SetLore(String[] string)
	{
		_lore = string; 

		ArrayList<String> lore = new ArrayList<String>();

		if (_lore != null)
		{
			for (String line : _lore)
			{
				if (line != null && !line.isEmpty())
					lore.add(line);
			}
		}

		ItemMeta meta = getItemMeta();
		meta.setLore(lore);
		setItemMeta(meta);
		
		return this;
	} 
	
	public ShopItem hideInfo()
	{
		ItemMeta meta = getItemMeta();
		meta.addItemFlags(ItemFlag.values());
		setItemMeta(meta);
	
		return this;
	}
}