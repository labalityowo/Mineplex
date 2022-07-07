package mineplex.core.communities.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import mineplex.core.Managers;
import mineplex.core.communities.CommunityManager;

public class CommunityMemberData
{
	private final Map<Integer, CommunityRole> _communities = new ConcurrentHashMap<>();
	
	public final List<Integer> Invites = new ArrayList<>();
	
	private int _chattingTo = -1;

	public CommunityMemberData()
	{
	}
	
	public int getTotalCommunities()
	{
		return _communities.size();
	}
	
	public boolean ownsCommunity()
	{
		return _communities.containsValue(CommunityRole.LEADER);
	}
	
	public int getCommunityChattingTo()
	{
		return _chattingTo;
	}

	public void setCommunityChattingTo(int id)
	{
		_chattingTo = id;
	}

	public void setCommunityChattingTo(Community community)
	{
		setCommunityChattingTo(community.getId());
	}

	public Set<Integer> getCommunityIds()
	{
		return _communities.keySet();
	}

	public List<Community> getCommunities()
	{
		List<Community> ret = new ArrayList<>(_communities.size());
		for (int id : _communities.keySet())
		{
			Community community = Managers.get(CommunityManager.class).getLoadedCommunity(id);
			if (community != null)
			{
				ret.add(community);
			}
		}

		return ret;
	}

	public boolean isMemberOf(Community community)
	{
		return _communities.containsKey(community.getId());
	}
	
	public CommunityRole getRoleIn(Community community)
	{
		return _communities.get(community.getId());
	}
	
	public void joinCommunity(Integer communityId, CommunityRole role)
	{
		_communities.put(communityId, role);
	}
	
	public void joinCommunity(Community community)
	{
		joinCommunity(community.getId(), CommunityRole.MEMBER);
	}
	
	public void setRoleIn(Community community, CommunityRole role)
	{
		_communities.replace(community.getId(), role);
	}
	
	public void leaveCommunity(Community community)
	{
		_communities.remove(community.getId());
	}
}