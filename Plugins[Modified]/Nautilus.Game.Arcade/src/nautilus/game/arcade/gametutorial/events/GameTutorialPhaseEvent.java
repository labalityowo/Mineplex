package nautilus.game.arcade.gametutorial.events;

import nautilus.game.arcade.gametutorial.GameTutorial;
import nautilus.game.arcade.gametutorial.TutorialPhase;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class GameTutorialPhaseEvent extends Event
{
	private static final HandlerList handlers = new HandlerList();
	private GameTutorial _tutorial;
	
	private TutorialPhase _from;
	private TutorialPhase _to;

	public GameTutorialPhaseEvent(GameTutorial tutorial, TutorialPhase from, TutorialPhase to)
	{
		_tutorial = tutorial;
		_from = from;
		_to = to;
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
	
	public TutorialPhase getFrom()
	{
		return _from;
	}
	
	public TutorialPhase getTo()
	{
		return _to;
	}
	
}
