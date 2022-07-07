package mineplex.core.boosters.event;

import mineplex.core.boosters.Booster;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called when a Booster is activated. This will be called regardless of which "BoosterGroup" the current server is set
 * to, so if you only want Boosters on the current BoosterGroup, you will need to verify it.
 *
 * @author Shaun Bennett
 */
public class BoosterActivateEvent extends Event
{
	private String _boosterGroup;
	private Booster _booster;

	public BoosterActivateEvent(String boosterGroup, Booster booster)
	{
		_boosterGroup = boosterGroup;
		_booster = booster;
	}

	public String getBoosterGroup()
	{
		return _boosterGroup;
	}

	public Booster getBooster()
	{
		return _booster;
	}

	private static final HandlerList _handlers = new HandlerList();
	private static HandlerList getHandlerList()
	{
		return _handlers;
	}

	@Override
	public HandlerList getHandlers()
	{
		return getHandlerList();
	}

}
