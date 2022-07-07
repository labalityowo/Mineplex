package mineplex.core.gadget.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerToggleSwimEvent extends Event
{

	private static final HandlerList handlers = new HandlerList();

	private Player _player;
	private boolean _swimming;

	public PlayerToggleSwimEvent(Player player, boolean swimming)
	{
		_player = player;
		_swimming = swimming;
	}

	public Player getPlayer()
	{
		return _player;
	}

	public boolean isSwimming()
	{
		return _swimming;
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
