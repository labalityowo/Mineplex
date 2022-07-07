package nautilus.game.arcade.game.games.paintball.events;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class PaintballEvent extends PlayerEvent
{
	/**
	 * Created by: Mysticate
	 * Timestamp: November 19, 2015
	 */
	
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

	private Player _revivedPlayer;

	public PaintballEvent(Player who, Player revivedPlayer)
	{
		super(who);

		_revivedPlayer = revivedPlayer;
	}

	public Player getKiller()
	{
		return _revivedPlayer;
	}
}
