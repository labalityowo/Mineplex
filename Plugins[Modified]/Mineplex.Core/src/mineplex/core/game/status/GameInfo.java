package mineplex.core.game.status;

import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.common.Constants;
import mineplex.core.game.GameDisplay;

public class GameInfo
{

	public static GameInfo fromString(String json)
	{
		return Constants.GSON.fromJson(json, GameInfo.class);
	}

	private final GameDisplay _game;
	private final String _mode, _map;
	private final int _timer;
	private final String[] _votingOn;
	private final PermissionGroup _hostRank;
	private final GameDisplayStatus _status;
	private final GameJoinStatus _joinable;

	public GameInfo()
	{
		this(null, null, null, -1, new String[0], null, GameDisplayStatus.CLOSING, GameJoinStatus.CLOSED);
	}

	public GameInfo(GameDisplay game, String mode, String map, int timer, String[] votingOn, PermissionGroup hostRank, GameDisplayStatus status, GameJoinStatus joinable)
	{
		_game = game;
		_mode = mode;
		_map = map;
		_timer = timer;
		_votingOn = votingOn;
		_hostRank = hostRank;
		_status = status;
		_joinable = joinable;
	}

	public GameDisplay getGame()
	{
		return _game;
	}

	public String getMode()
	{
		return _mode;
	}

	public String getMap()
	{
		return _map;
	}

	public int getTimer()
	{
		return _timer;
	}

	public String[] getVotingOn()
	{
		return _votingOn;
	}

	public PermissionGroup getHostRank()
	{
		return _hostRank;
	}

	public GameDisplayStatus getStatus()
	{
		return _status;
	}

	public GameJoinStatus getJoinable()
	{
		return _joinable;
	}

	@Override
	public String toString()
	{
		return Constants.GSON.toJson(this);
	}

	public enum GameDisplayStatus
	{
		// Sorted by priority
		ALWAYS_OPEN,
		STARTING,
		VOTING,
		WAITING,
		IN_PROGRESS,
		CLOSING
	}

	public enum GameJoinStatus
	{
		OPEN,
		RANKS_ONLY,
		CLOSED
	}
}
