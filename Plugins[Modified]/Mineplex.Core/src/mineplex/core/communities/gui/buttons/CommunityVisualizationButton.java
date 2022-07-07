package mineplex.core.communities.gui.buttons;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilText;
import mineplex.core.communities.data.Community;
import mineplex.core.communities.data.ICommunity;
import mineplex.core.communities.gui.pages.CommunityMembersPage;
import mineplex.core.itemstack.ItemBuilder;

public class CommunityVisualizationButton extends CommunitiesGUIButton
{
	private Player _viewer;
	private ICommunity _community;
	private boolean _invite;
	
	public CommunityVisualizationButton(Player viewer, ICommunity community, boolean invite)
	{
		super(new ItemBuilder(Material.BARRIER).build());
		
		_viewer = viewer;
		_community = community;
		_invite = invite;
		update();
	}

	@Override
	public void update()
	{
		ItemBuilder builder = new ItemBuilder(new ItemStack(_community.getFavoriteGame().getMaterial(), 1, _community.getFavoriteGame().getMaterialData()))
				.setTitle(C.cGreenB + _community.getName())
				.addLore(UtilText.splitLinesToArray(new String[] {
						C.cRed,
						C.cYellow + "Members " + C.cWhite + _community.getMemberCount(),
						C.cYellow + "Favorite Game " + C.cWhite + _community.getFavoriteGame().getName(),
						C.cYellow + "Description " + C.cWhite + _community.getDescription()}, LineFormat.LORE));

		if (_invite)
		{
			builder.addLore(UtilText.splitLinesToArray(new String[] {
					C.cGold,
					C.cYellow + "Shift-Left Click " + C.cWhite + "Join",
					C.cYellow + "Shift-Right Click " + C.cWhite + "Decline"}, LineFormat.LORE));
		}

		builder.addLore(C.cBlue, C.cGreen + "Click to view community");
		Button = builder.build();
	}

	@Override
	public void handleClick(ClickType type)
	{
		if (_invite && type == ClickType.SHIFT_RIGHT)
		{
			getCommunityManager().handleRejectInvite(_viewer, _community.getId());
		}
		else if (_invite && type == ClickType.SHIFT_LEFT)
		{
			getCommunityManager().handleJoin(_viewer, _community, true);
		}
		else
		{
			getCommunityManager().tempLoadCommunity(_community.getId(), community ->
				new CommunityMembersPage(_viewer, community));
		}
	}
}