package mineplex.game.clans.clans.war.event;

import mineplex.game.clans.clans.war.WarSiege;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called when a war siege is started
 */
public class WarSiegeStartEvent extends Event
{
	private WarSiege _warSiege;

	public WarSiegeStartEvent(WarSiege warSiege)
	{
		_warSiege = warSiege;
	}

	public WarSiege getWarSiege()
	{
		return _warSiege;
	}

	// Bukkit event stuff
	private static final HandlerList handlers = new HandlerList();
	public static HandlerList getHandlerList() { return handlers; }
	public HandlerList getHandlers() { return handlers; }
}