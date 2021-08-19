package mineplex.core.communities.redis;

import mineplex.serverdata.commands.ServerCommand;

public class CommunityUnInvite extends ServerCommand
{
	private Integer _communityId;
	private String _sender;
	private String _playerName;
	private String _playerUUID;
	private boolean _announce;
	
	public CommunityUnInvite(Integer communityId, String sender, String playerName, String playerUUID, boolean announce)
	{
		_communityId = communityId;
		_sender = sender;
		_playerName = playerName;
		_playerUUID = playerUUID;
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
	
	public boolean shouldAnnounce()
	{
		return _announce;
	}
}