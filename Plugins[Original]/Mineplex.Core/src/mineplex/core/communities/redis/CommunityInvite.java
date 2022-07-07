package mineplex.core.communities.redis;

import mineplex.serverdata.commands.ServerCommand;

public class CommunityInvite extends ServerCommand
{
	private Integer _communityId;
	private String _sender;
	private String _playerName;
	private String _playerUUID;
	
	public CommunityInvite(Integer communityId, String sender, String playerName, String playerUUID)
	{
		_communityId = communityId;
		_sender = sender;
		_playerName = playerName;
		_playerUUID = playerUUID;
	}

	public Integer getCommunityId()
	{
		return _communityId;
	}
	
	public String getSender()
	{
		return _sender;
	}
	
	public String getPlayerName()
	{
		return _playerName;
	}
	
	public String getPlayerUUID()
	{
		return _playerUUID;
	}
}