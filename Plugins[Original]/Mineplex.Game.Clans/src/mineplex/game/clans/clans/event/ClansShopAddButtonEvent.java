package mineplex.game.clans.clans.event;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import mineplex.core.shop.ShopBase;

public class ClansShopAddButtonEvent extends Event
{
	private static final HandlerList handlers = new HandlerList();
	
	private final Player _player;
	private final ShopBase<?> _shop;
	private Material _material;
	private int _buyPrice;
	private int _sellPrice;
	private byte _data;
	private String _displayName;
	private int _amount;
	private int _slot;
	
	private boolean _cancelled;
	
	public ClansShopAddButtonEvent(Player player, ShopBase<?> shop, int slot, Material material, int buyPrice, int sellPrice, byte data, String displayName, int amount)
	{
		_player = player;
		_shop = shop;
		_slot = slot;
		_material = material;
		_buyPrice = buyPrice;
		_sellPrice = sellPrice;
		_data = data;
		_displayName = displayName;
		_amount = amount;
	}
	
	public Player getPlayer()
	{
		return _player;
	}
	
	public ShopBase<?> getShop()
	{
		return _shop;
	}
	
	public Material getMaterial()
	{
		return _material;
	}
	
	public int getBuyPrice()
	{
		return _buyPrice;
	}
	
	public void setBuyPrice(int buyPrice)
	{
		_buyPrice = buyPrice;
	}
	
	public int getSellPrice()
	{
		return _sellPrice;
	}
	
	public void setSellPrice(int sellPrice)
	{
		_sellPrice = sellPrice;
	}
	
	public int getSlot()
	{
		return _slot;
	}
	
	public void setSlot(int slot)
	{
		_slot = slot;
	}
	
	public byte getData()
	{
		return _data;
	}
	
	public void setData(byte data)
	{
		_data = data;
	}
	
	public void setMaterial(Material material)
	{
		_material = material;
	}
	
	public void setDisplayName(String displayName)
	{
		_displayName = displayName;
	}
	
	public void setAmount(int amount)
	{
		_amount = amount;
	}
	
	public String getDisplayName()
	{
		return _displayName;
	}
	
	public int getAmount()
	{
		return _amount;
	}
	
	public boolean isCancelled()
	{
		return _cancelled;
	}
	
	public void setCancelled(boolean cancelled)
	{
		_cancelled = cancelled;
	}
	
	public HandlerList getHandlers()
	{
		return handlers;
	}
	
	public static HandlerList getHandlerList()
	{
		return handlers;
	}
}