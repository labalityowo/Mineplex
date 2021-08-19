package mineplex.core.communities.redis;

import mineplex.serverdata.commands.ServerCommand;

public class CommunityCreate extends ServerCommand
{
	private String _leaderUUID;
	private String _leaderName;
	private Integer _leaderId;
	private Integer _communityId;
	private String _communityName;

	public CommunityCreate(String leaderUUID, String leaderName, Integer leaderId, Integer communityId, String communityName)
	{
		_leaderUUID = leaderUUID;
		_leaderName = leaderName;
		_leaderId = leaderId;
		_communityId = communityId;
		_communityName = communityName;
	}
	
	public String getLeaderUUID()
	{
		return _leaderUUID;
	}
	
	public String getLeaderName()
	{
		return _leaderName;
	}
	
	public Integer getLeaderId()
	{
		return _leaderId;
	}
	
	public Integer getCommunityId()
	{
		return _communityId;
	}
	
	public String getCommunityName()
	{
		return _communityName;
	}
}