package mineplex.core.communities.gui.buttons;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import mineplex.core.Managers;
import mineplex.core.account.CoreClientManager;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.communities.data.Community;
import mineplex.core.communities.CommunityManager;
import mineplex.core.communities.data.CommunityMemberInfo;
import mineplex.core.communities.data.CommunityRole;

public class CommunityMemberButton extends CommunitiesGUIButton
{
	private Player _viewer;
	private Community _community;
	private CommunityMemberInfo _info;
	private boolean _fetching = false;
	
	public CommunityMemberButton(Player viewer, Community community, CommunityMemberInfo info)
	{
		super(info.getRepresentation(getCommunityManager().Get(viewer).getRoleIn(community)));
		
		_viewer = viewer;
		_community = community;
		_info = info;
	}

	@Override
	public void update()
	{
		Button = _info.getRepresentation(getCommunityManager().Get(_viewer).getRoleIn(_community));
	}

	@Override
	public void handleClick(ClickType type)
	{
		if (_fetching)
		{
			return;
		}
		if (type == ClickType.SHIFT_RIGHT)
		{
			if (getCommunityManager().Get(_viewer).getRoleIn(_community) != null && getCommunityManager().Get(_viewer).getRoleIn(_community).ordinal() < _info.Role.ordinal())
			{
				getCommunityManager().handleKick(_viewer, _community, _info);
			}
		}
		if (type == ClickType.LEFT || type == ClickType.SHIFT_LEFT)
		{
			if (getCommunityManager().Get(_viewer).getRoleIn(_community) != null && getCommunityManager().Get(_viewer).getRoleIn(_community) == CommunityRole.LEADER && _info.Role != CommunityRole.LEADER)
			{
				if (_info.Role == CommunityRole.MEMBER)
				{
					getCommunityManager().handleRoleUpdate(_viewer, _community, _info, CommunityRole.COLEADER);
				}
				if (_info.Role == CommunityRole.COLEADER && type == ClickType.SHIFT_LEFT)
				{
					if (getCommunityManager().ownsCommunity(_info.UUID))
					{
						UtilPlayer.message(_viewer, F.main(getCommunityManager().getName(), F.name(_info.Name) + " can only own one community at a time!"));
						return;
					}
					CoreClientManager clientManager = Managers.get(CoreClientManager.class);
					_fetching = true;
					clientManager.fetchGroups(_info.AccountId, (primaryGroup, additionalGroups) ->
					{
						if (primaryGroup.hasPermission(CommunityManager.Perm.OWN_COMMUNITY))
						{
							getCommunityManager().handleRoleUpdate(_viewer, _community, _info, CommunityRole.LEADER);
							getCommunityManager().handleRoleUpdate(_viewer, _community, _community.getMembers().get(_viewer.getUniqueId()), CommunityRole.COLEADER);
						}
						else
						{
							UtilPlayer.message(_viewer, F.main(getCommunityManager().getName(), "Only Eternal rank and above can own a community!"));
						}
						_fetching = false;
					}, () ->
					{
						UtilPlayer.message(_viewer, F.main(getCommunityManager().getName(), "Only Eternal rank and above can own a community!"));
						_fetching = false;
					});
				}
			}
		}
		if (type == ClickType.RIGHT)
		{
			if (getCommunityManager().Get(_viewer).getRoleIn(_community) != null && getCommunityManager().Get(_viewer).getRoleIn(_community) == CommunityRole.LEADER && _info.Role == CommunityRole.COLEADER)
			{
				getCommunityManager().handleRoleUpdate(_viewer, _community, _info, CommunityRole.MEMBER);
			}
		}
	}
}