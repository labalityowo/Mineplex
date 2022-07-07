package mineplex.game.clans.clans.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import mineplex.game.clans.clans.ClanInfo;

public class ClanDisbandedEvent extends Event
{
	private static final HandlerList handlers = new HandlerList();
	
	private Player _disbander;
	private ClanInfo _clan;
	
	private boolean _cancelled;
	
	public ClanDisbandedEvent(ClanInfo info, Player disbander)
	{
		_disbander = disbander;
		_clan = info;
	}
	
	public Player getDisbander()
	{
		return _disbander;
	}
	
	public ClanInfo getClan()
	{
		return _clan;
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