package mineplex.game.clans.clans.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import mineplex.game.clans.clans.ClanInfo;

public class ClanJoinEvent extends Event
{
	private static final HandlerList handlers = new HandlerList();
	
	private Player _player;
	
	private ClanInfo _clan;
	
	private boolean _cancelled;
	
	public ClanJoinEvent(ClanInfo clan, Player player)
	{
		_player = player;
		
		_clan = clan;
	}
	
	public Player getPlayer()
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