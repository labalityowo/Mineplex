package mineplex.core.communities.redis;

import mineplex.serverdata.commands.ServerCommand;

public class CommunityUpdateMemberChatReading extends ServerCommand
{
	private Integer _communityId;
	private String _playerUUID;
	private boolean _reading;

	public CommunityUpdateMemberChatReading(Integer communityId, String playerUUID, boolean reading)
	{
		_communityId = communityId;
		_playerUUID = playerUUID;
		_reading = reading;
	}

	public Integer getCommunityId()
	{
		return _communityId;
	}
	
	public String getPlayerUUID()
	{
		return _playerUUID;
	}
	
	public boolean reading()
	{
		return _reading;
	}
}