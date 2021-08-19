package mineplex.core.communities.redis;

import mineplex.serverdata.commands.ServerCommand;

public class CommunityUpdateName extends ServerCommand
{
	private Integer _communityId;
	private String _sender;
	private String _name;

	public CommunityUpdateName(Integer communityId, String sender, String name)
	{
		_communityId = communityId;
		_sender = sender;
		_name = name;
	}

	public Integer getCommunityId()
	{
		return _communityId;
	}
	
	public String getSender()
	{
		return _sender;
	}
	
	public String getName()
	{
		return _name;
	}
}