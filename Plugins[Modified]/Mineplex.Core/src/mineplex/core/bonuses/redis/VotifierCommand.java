package mineplex.core.bonuses.redis;

import mineplex.serverdata.commands.ServerCommand;

public class VotifierCommand extends ServerCommand
{
	private String _playerName;
	private int _rewardReceived;
	private boolean _clans;

	public VotifierCommand(String playerName, int rewardReceived, boolean clans, String... targetServer)
	{
		super(targetServer);

		_playerName = playerName;
		_rewardReceived = rewardReceived;
		_clans = clans;
	}

	public String getPlayerName()
	{
		return _playerName;
	}

	public int getRewardReceived()
	{
		return _rewardReceived;
	}
	
	public boolean isClans()
	{
		return _clans;
	}
}