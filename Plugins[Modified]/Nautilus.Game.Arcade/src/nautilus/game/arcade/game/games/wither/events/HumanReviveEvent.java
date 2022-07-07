package nautilus.game.arcade.game.games.wither.events;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class HumanReviveEvent extends PlayerEvent
{
	private static final HandlerList handlers = new HandlerList();

	public static HandlerList getHandlerList()
	{
		return handlers;
	}

	@Override
	public HandlerList getHandlers()
	{
		return getHandlerList();
	}

	private final Player _revivedPlayer;

	public HumanReviveEvent(Player who, Player revivedPlayer)
	{
		super(who);

		_revivedPlayer = revivedPlayer;
	}

	public Player getRevivedPlayer()
	{
		return _revivedPlayer;
	}

}
