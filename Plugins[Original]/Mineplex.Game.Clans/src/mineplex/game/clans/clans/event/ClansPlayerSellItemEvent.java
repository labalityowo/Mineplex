package mineplex.game.clans.clans.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

import mineplex.core.shop.page.ShopPageBase;
import mineplex.game.clans.shop.ShopItemButton;

public class ClansPlayerSellItemEvent extends Event
{
	private static final HandlerList handlers = new HandlerList();
	
	private Player _player;
	private ShopPageBase<?, ?> _page;
	private int _cost;
	private int _amount;
	private ItemStack _item;
	
	private ShopItemButton<? extends ShopPageBase<?, ?>> _button;
	
	private boolean _cancelled;
	
	public ClansPlayerSellItemEvent(Player player, ShopPageBase<?, ?> page, ShopItemButton<? extends ShopPageBase<?, ?>> shopItemButton, ItemStack item, int cost, int amount)
	{
		_page = page;
		_button = shopItemButton;
		_amount = amount;
		_item = item;
		_player = player;
		_cost = cost;
	}
	
	public Player getPlayer()
	{
		return _player;
	}
	
	public ShopPageBase<?, ?> getPage()
	{
		return _page;
	}
	
	public ItemStack getItem()
	{
		return _item;
	}
	
	public void setItem(ItemStack item)
	{
		_item = item;
	}
	
	public int getCost()
	{
		return _cost;
	}
	
	public void setCost(int cost)
	{
		_cost = cost;
	}
	
	public int getAmount()
	{
		return _amount;
	}
	
	public void setAmount(int amount)
	{
		_amount = amount;
	}
	
	public ShopItemButton<? extends ShopPageBase<?, ?>> getButton()
	{
		return _button;
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