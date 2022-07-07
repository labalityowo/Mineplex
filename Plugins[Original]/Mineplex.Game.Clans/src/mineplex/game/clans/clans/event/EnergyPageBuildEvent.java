package mineplex.game.clans.clans.event;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import mineplex.core.shop.ShopBase;

public class EnergyPageBuildEvent extends Event
{
	private static final HandlerList handlers = new HandlerList();
	
	private boolean _free;
	private boolean _cancelled;
	
	private Player _player;
	
	public EnergyPageBuildEvent(Player player)
	{
		_player = player;
	}
	
	public Player getPlayer()
	{
		return _player;
	}
	
	public boolean free()
	{
		return _free;
	}
	
	public void setFree(boolean free)
	{
		_free = free;
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