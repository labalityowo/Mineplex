package mineplex.core.communities.gui.buttons;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import mineplex.core.communities.data.Community;
import mineplex.core.communities.data.CommunityJoinRequestInfo;
import mineplex.core.communities.data.CommunityRole;

public class CommunityJoinRequestButton extends CommunitiesGUIButton
{
	private Player _viewer;
	private Community _community;
	private CommunityJoinRequestInfo _info;
	
	public CommunityJoinRequestButton(Player viewer, Community community, CommunityJoinRequestInfo info)
	{
		super(info.getRepresentation());
		
		_viewer = viewer;
		_community = community;
		_info = info;
	}

	@Override
	public void update()
	{
		Button = _info.getRepresentation();
	}

	@Override
	public void handleClick(ClickType type)
	{
		if (type == ClickType.LEFT)
		{
			if (getCommunityManager().Get(_viewer).getRoleIn(_community) != null && getCommunityManager().Get(_viewer).getRoleIn(_community).ordinal() <= CommunityRole.COLEADER.ordinal())
			{
				getCommunityManager().handleInvite(_viewer, _community, _info.Name);
			}
		}
		if (type == ClickType.SHIFT_RIGHT)
		{
			if (getCommunityManager().Get(_viewer).getRoleIn(_community) != null && getCommunityManager().Get(_viewer).getRoleIn(_community).ordinal() <= CommunityRole.COLEADER.ordinal())
			{
				getCommunityManager().handleCloseJoinRequest(_viewer, _community, _info, true);
			}
		}
	}
}