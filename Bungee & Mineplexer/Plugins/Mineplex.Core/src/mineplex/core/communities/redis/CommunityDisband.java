package mineplex.core.communities.redis;

import mineplex.serverdata.commands.ServerCommand;

public class CommunityDisband extends ServerCommand
{
	private String _senderName;
	private Integer _communityId;

	public CommunityDisband(String senderName, Integer communityId)
	{
		_senderName = senderName;
		_communityId = communityId;
	}
	
	public String getSenderName()
	{
		return _senderName;
	}
	
	public Integer getCommunityId()
	{
		return _communityId;
	}
}