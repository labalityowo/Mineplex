package mineplex.hub.hubgame.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

import mineplex.hub.hubgame.CycledGame;
import mineplex.hub.hubgame.CycledGame.GameState;

public class HubGamePlayerDeathEvent extends PlayerEvent
{

	private static final HandlerList _handlers = new HandlerList();

	private final CycledGame _game;
	private final boolean _end;

	public HubGamePlayerDeathEvent(Player player, CycledGame game, boolean end)
	{
		super(player);

		_game = game;
		_end = end;
	}

	public CycledGame getGame()
	{
		return _game;
	}

	public boolean isEnding()
	{
		return _end;
	}

	public static HandlerList getHandlerList()
	{
		return _handlers;
	}

	@Override
	public HandlerList getHandlers()
	{
		return getHandlerList();
	}

}
