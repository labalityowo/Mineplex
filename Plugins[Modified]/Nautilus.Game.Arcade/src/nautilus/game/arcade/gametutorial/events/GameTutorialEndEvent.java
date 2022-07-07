package nautilus.game.arcade.gametutorial.events;

import nautilus.game.arcade.gametutorial.GameTutorial;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class GameTutorialEndEvent extends Event
{
	private static final HandlerList handlers = new HandlerList();
	private GameTutorial _tutorial;

	public GameTutorialEndEvent(GameTutorial tutorial)
	{
		_tutorial = tutorial;
	}

	public HandlerList getHandlers()
	{
		return handlers;
	}

	public static HandlerList getHandlerList()
	{
		return handlers;
	}

	public GameTutorial getTutorial()
	{
		return _tutorial;
	}
}
