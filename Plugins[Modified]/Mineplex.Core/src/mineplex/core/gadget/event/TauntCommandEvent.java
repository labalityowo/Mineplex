package mineplex.core.gadget.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import mineplex.core.common.util.UtilTime;
import mineplex.core.game.GameDisplay;

public class TauntCommandEvent extends Event
{

	private static final HandlerList handlers = new HandlerList();

	private Player _player;
	private boolean _gameInProgress;
	private boolean _alive;
	private boolean _spectator;
	private long _lastPvp;
	private TauntState _state = TauntState.NONE;
	private GameDisplay _gameDisplay;

	public TauntCommandEvent(Player player, boolean gameInProgress, boolean alive, boolean spectator, long lastPvp, GameDisplay gameDisplay)
	{
		_player = player;
		_gameInProgress = gameInProgress;
		_alive = alive;
		_spectator = spectator;
		_lastPvp = lastPvp;
		_gameDisplay = gameDisplay;
	}

	public Player getPlayer()
	{
		return _player;
	}

	public boolean isGameInProgress()
	{
		return _gameInProgress;
	}

	public boolean isAlive()
	{
		return _alive;
	}

	public boolean isSpectator()
	{
		return _spectator;
	}

	public boolean isInPvp(long cooldown)
	{
		return !UtilTime.elapsed(_lastPvp, cooldown);
	}

	public TauntState getState()
	{
		return _state;
	}

	public GameDisplay getGameDisplay()
	{
		return _gameDisplay;
	}

	public void setState(TauntState state)
	{
		_state = state;
	}

	public HandlerList getHandlers()
	{
		return handlers;
	}

	public static HandlerList getHandlerList()
	{
		return handlers;
	}

	public enum TauntState
	{
		NONE(""),
		NO_TAUNT("You have no active taunts!"),
		NOT_IN_GAME("You are not in a game!"),
		NOT_ALIVE("You are not playing the game!"),
		SPECTATOR("You can't run this as a spectator!"),
		PVP("You can't run this while in pvp!"),
		GAME_DISABLED("Taunts are disabled in this game!");

		private String _message;

		TauntState(String message)
		{
			_message = message;
		}

		public String getMessage()
		{
			return _message;
		}
	}

}
