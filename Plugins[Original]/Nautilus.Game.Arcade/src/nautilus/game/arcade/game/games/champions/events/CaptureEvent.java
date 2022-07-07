package nautilus.game.arcade.game.games.champions.events;

import java.util.Collection;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * CaptureEvent
 *
 * @author xXVevzZXx
 */
public class CaptureEvent extends Event
{
	private Collection<Player> _players;
	
	public CaptureEvent(Collection<Player> players)
	{
		_players = players;
	}
	
	public Collection<Player> getPlayers()
	{
		return _players;
	}
	
	@Override
	public HandlerList getHandlers()
	{
		return getHandlerList();
	}

	private static HandlerList _handlers = new HandlerList();

	public static HandlerList getHandlerList()
	{
		return _handlers;
	}

}
