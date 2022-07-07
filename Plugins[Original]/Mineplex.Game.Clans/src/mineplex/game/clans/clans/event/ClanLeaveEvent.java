package mineplex.game.clans.clans.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import mineplex.game.clans.clans.ClanInfo;
import mineplex.game.clans.clans.ClansPlayer;

public class ClanLeaveEvent extends Event
{
	private static final HandlerList handlers = new HandlerList();
	
	private ClansPlayer _player;
	
	private ClanInfo _clan;
	
	private boolean _cancelled;
	
	public ClanLeaveEvent(ClanInfo clan, ClansPlayer clansPlayer)
	{
		_player = clansPlayer;
		
		_clan = clan;
	}
	
	public ClansPlayer getPlayer()
	{
		return _player;
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