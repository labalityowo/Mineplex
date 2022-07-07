package mineplex.game.clans.clans.war.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import mineplex.game.clans.core.war.ClanWarData;

public class WarStartEvent extends Event
{
	private ClanWarData _war;

	public WarStartEvent(ClanWarData war)
	{
		_war = war;
	}

	public ClanWarData getWar()
	{
		return _war;
	}

	// Bukkit event stuff
	private static final HandlerList handlers = new HandlerList();
	public static HandlerList getHandlerList() { return handlers; }
	public HandlerList getHandlers() { return handlers; }
}