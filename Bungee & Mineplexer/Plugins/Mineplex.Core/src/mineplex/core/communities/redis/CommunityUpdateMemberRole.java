package mineplex.core.communities.redis;

import mineplex.serverdata.commands.ServerCommand;

public class CommunityUpdateMemberRole extends ServerCommand
{
	private Integer _communityId;
	private String _sender;
	private String _playerUUID;
	private String _memberRole;

	public CommunityUpdateMemberRole(Integer communityId, String sender, String playerUUID, String memberRole)
	{
		_communityId = communityId;
		_sender = sender;
		_playerUUID = playerUUID;
		_memberRole = memberRole;
	}

	public Integer getCommunityId()
	{
		return _communityId;
	}
	
	public String getSender()
	{
		return _sender;
	}
	
	public String getPlayerUUID()
	{
		return _playerUUID;
	}
	
	public String getMemberRole()
	{
		return _memberRole;
	}
}