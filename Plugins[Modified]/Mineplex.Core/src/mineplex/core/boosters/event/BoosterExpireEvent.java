package mineplex.core.boosters.event;

import mineplex.core.boosters.Booster;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called when a Booster is finished.
 *
 * @author Shaun Bennett
 */
public class BoosterExpireEvent extends Event
{
	private String _boosterGroup;
	private Booster _booster;

	public BoosterExpireEvent(String boosterGroup, Booster booster)
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
