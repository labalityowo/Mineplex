package mineplex.core.communities.redis;

import mineplex.serverdata.commands.ServerCommand;

public class CommunityCloseJoinRequest extends ServerCommand
{
	private Integer _communityId;
	private String _sender;
	private String _playerName;
	private String _playerUUID;
	private Integer _accountId;
	private boolean _announce;

	public CommunityCloseJoinRequest(Integer communityId, String sender, String playerName, String playerUUID, Integer accountId, boolean announce)
	{
		_communityId = communityId;
		_sender = sender;
		_playerName = playerName;
		_playerUUID = playerUUID;
		_accountId = accountId;
		_announce = announce;
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
	
	public Integer getAccountId()
	{
		return _accountId;
	}
	
	public boolean shouldAnnounce()
	{
		return _announce;
	}
}