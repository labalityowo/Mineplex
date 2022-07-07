package mineplex.core.party.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * This event is called when a party owner selects the compass icon.
 */
public class PartySelectServerEvent extends Event
{
	private static final HandlerList HANDLER_LIST = new HandlerList();

	private final Player _player;

	public PartySelectServerEvent(Player player)
	{
		_player = player;
	}

	public Player getPlayer()
	{
		return _player;
	}

	@Override
	public HandlerList getHandlers()
	{
		return HANDLER_LIST;
	}

	public static HandlerList getHandlerList()
	{
		return HANDLER_LIST;
	}
}
