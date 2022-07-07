package mineplex.game.clans.clans.event;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import mineplex.game.clans.clans.ClanInfo;

public class ClanSetHomeEvent extends Event
{
	private static final HandlerList handlers = new HandlerList();
	
	private Player _player;
	private ClanInfo _clan;
	
	private Location _loc;
	
	private boolean _cancelled;
	
	public ClanSetHomeEvent(ClanInfo clan, Player player, Location location)
	{
		_player = player;
		_loc = location;
		_clan = clan;
	}
	
	public Player getPlayer()
	{
		return _player;
	}
	
	public Location getLocation()
	{
		return _loc;
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