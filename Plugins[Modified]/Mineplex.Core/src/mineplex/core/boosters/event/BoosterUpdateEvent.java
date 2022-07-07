package mineplex.core.boosters.event;

import mineplex.core.boosters.Booster;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.List;
import java.util.Map;

/**
 * Called when {@link mineplex.core.boosters.redis.BoosterUpdateListener} receives updated Boosters over redis pubsub.
 *
 * @author Shaun Bennett
 */
public class BoosterUpdateEvent extends Event
{
	private Map<String, List<Booster>> _boosterMap;

	public BoosterUpdateEvent(Map<String, List<Booster>> boosterMap)
	{
		_boosterMap = boosterMap;
	}

	public Map<String, List<Booster>> getBoosterMap()
	{
		return _boosterMap;
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
