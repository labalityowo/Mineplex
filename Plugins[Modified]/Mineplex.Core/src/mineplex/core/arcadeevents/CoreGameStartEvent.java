package mineplex.core.arcadeevents;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import mineplex.core.game.GameDisplay;

/**
 * This event is called when a game starts in arcade
 * It's called inside Arcade and handled inside Core,
 * so we can track game events in core
 */
public class CoreGameStartEvent extends Event
{

	private static final HandlerList handlers = new HandlerList();

	private GameDisplay _gameDisplay;

	/**
	 * @param gameDisplay the type of the game
	 */
	public CoreGameStartEvent(GameDisplay gameDisplay)
	{
		_gameDisplay = gameDisplay;
	}

	public GameDisplay getGameDisplay()
	{
		return _gameDisplay;
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
