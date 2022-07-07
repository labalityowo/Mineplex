package mineplex.game.clans.clans.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import mineplex.game.clans.clans.ClanInfo;

public class ClanDeleteEvent extends Event
{
	private static final HandlerList handlers = new HandlerList();

	private ClanInfo _clanInfo;

	public ClanDeleteEvent(ClanInfo clanInfo)
	{
		_clanInfo = clanInfo;
	}

	public ClanInfo getClanInfo()
	{
		return _clanInfo;
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