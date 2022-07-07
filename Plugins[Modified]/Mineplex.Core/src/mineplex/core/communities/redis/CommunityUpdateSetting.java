package mineplex.core.communities.redis;

import mineplex.serverdata.commands.ServerCommand;

public class CommunityUpdateSetting extends ServerCommand
{
	private Integer _communityId;
	private String _sender;
	private String _setting;
	private String _newValue;

	public CommunityUpdateSetting(Integer communityId, String sender, String setting, String newValue)
	{
		_communityId = communityId;
		_sender = sender;
		_setting = setting;
		_newValue = newValue;
	}

	public Integer getCommunityId()
	{
		return _communityId;
	}
	
	public String getSender()
	{
		return _sender;
	}
	
	public String getSetting()
	{
		return _setting;
	}
	
	public String getNewValue()
	{
		return _newValue;
	}
}