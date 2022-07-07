package mineplex.core.communities.redis;

import mineplex.serverdata.commands.ServerCommand;

public class CommunityJoinRequest extends ServerCommand
{
	private Integer _communityId;
	private String _playerName;
	private String _playerUUID;
	private Integer _accountId;

	public CommunityJoinRequest(Integer communityId, String playerName, String playerUUID, Integer accountId)
	{
		_communityId = communityId;
		_playerName = playerName;
		_playerUUID = playerUUID;
		_accountId = accountId;
	}

	public Integer getCommunityId()
	{
		return _communityId;
	}
	
	public String getPlayerName()
	{
		return _playerName;
	}
	
	public String getPlayerUUID()
	{
		return _playerUUID;
	}
	
	public Integer getAccountId()
	{
		return _accountId;
	}
}