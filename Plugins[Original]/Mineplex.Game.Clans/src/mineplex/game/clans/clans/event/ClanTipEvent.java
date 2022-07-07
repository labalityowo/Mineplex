package mineplex.game.clans.clans.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import mineplex.game.clans.clans.ClanTips.TipType;

public class ClanTipEvent extends Event
{
	private static final HandlerList handlers = new HandlerList();
	
	private Player _player;
	private TipType _tip;
	
	private boolean _cancelled;
	
	public ClanTipEvent(TipType tip, Player player)
	{
		_player = player;
		
		_tip = tip;
	}
	
	public Player getPlayer()
	{
		return _player;
	}
	
	public TipType getTip()
	{
		return _tip;
	}
	
	public void setCancelled(boolean cancelled)
	{
		_cancelled = cancelled;
	}
	
	public boolean isCancelled()
	{
		return _cancelled;
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