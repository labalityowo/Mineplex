package mineplex.hub.hubgame.common;

import mineplex.core.lifetimes.ListenerComponent;
import mineplex.hub.hubgame.HubGame;

public class HubGameComponent<T extends HubGame> extends ListenerComponent
{

	protected final T _game;

	public HubGameComponent(T game)
	{
		_game = game;
	}
}
