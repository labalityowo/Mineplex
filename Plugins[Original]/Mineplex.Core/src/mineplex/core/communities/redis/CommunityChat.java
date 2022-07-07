package mineplex.core.communities.redis;

import mineplex.serverdata.commands.ServerCommand;

public class CommunityChat extends ServerCommand
{
	private String _senderName;
	private Integer _communityId;
	private String _message;

	public CommunityChat(String senderName, Integer communityId, String message)
	{
		_senderName = senderName;
		_communityId = communityId;
		_message = message;
	}

	public String getSenderName()
	{
		return _senderName;
	}
	
	public Integer getCommunityId()
	{
		return _communityId;
	}

	public String getMessage()
	{
		return _message;
	}
}