package mineplex.hub.hubgame;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;

import mineplex.core.common.util.UtilServer;
import mineplex.hub.hubgame.event.HubGamePlayerDeathEvent;
import mineplex.hub.hubgame.event.HubGameStateChangeEvent;

public abstract class CycledGame extends HubGame
{

	// Game Properties
	private GameState _state;

	// Game Timer
	private long _lastStart;

	// Players
	private final List<Player> _queuedPlayers;
	private final List<Player> _alivePlayers;
	private final List<Player> _allPlayers;
	private final List<Player> _places;

	// Lobby
	private int _countdown;

	public CycledGame(HubGameManager manager, HubGameType type)
	{
		super(manager, type);

		_state = GameState.Waiting;

		_queuedPlayers = new ArrayList<>();
		_alivePlayers = new ArrayList<>(type.getMaxPlayers());
		_allPlayers = new ArrayList<>(type.getMaxPlayers());
		_places = new ArrayList<>(type.getMaxPlayers());

		_countdown = -1;
	}

	@Override
	public List<Player> getAlivePlayers()
	{
		return _alivePlayers;
	}

	public List<Player> getAllPlayers()
	{
		return _allPlayers;
	}

	/*
	 	Game Events
	 */

	public void onPrepare()
	{
	}

	public void onLive()
	{
	}

	public abstract boolean endCheck();

	public void onEnd()
	{
	}

	public void onCleanup()
	{
		_alivePlayers.clear();
		_allPlayers.clear();
		_places.clear();
	}

	/*
		Player Events
	 */

	public void onPlayerQueue(Player player)
	{
	}

	public void onPlayerLeaveQueue(Player player)
	{
	}

	@Override
	public void onPlayerDeath(Player player)
	{
		onPlayerDeath(player, false);
	}

	public void onPlayerDeath(Player player, boolean end)
	{
		HubGamePlayerDeathEvent event = new HubGamePlayerDeathEvent(player, this, end);
		UtilServer.CallEvent(event);
	}

	@Override
	public void onCleanupPlayer(Player player)
	{
		_queuedPlayers.remove(player);
		_alivePlayers.remove(player);
		_allPlayers.remove(player);
		_places.remove(player);
	}

	public boolean isLive()
	{
		return _state == GameState.Live;
	}

	public void setState(GameState state)
	{
		_state = state;

		switch (state)
		{
			case Prepare:
				onPrepare();
				break;
			case Live:
				_lastStart = System.currentTimeMillis();
				onLive();
				break;
		}

		UtilServer.CallEvent(new HubGameStateChangeEvent(this, state));
		setPhase(state);
	}

	public GameState getGameState()
	{
		return _state;
	}

	public long getLastStart()
	{
		return _lastStart;
	}

	public List<Player> getQueuedPlayers()
	{
		return _queuedPlayers;
	}

	public List<Player> getNextPlayers()
	{
		return _queuedPlayers.subList(0, Math.min(_queuedPlayers.size(), getGameType().getMaxPlayers()));
	}

	public List<Player> getPlaces()
	{
		return _places;
	}

	public void setCountdown(int countdown)
	{
		_countdown = countdown;
	}

	public int getCountdown()
	{
		return _countdown;
	}

	public enum GameState
	{

		Waiting, Prepare, Live, End

	}

}
