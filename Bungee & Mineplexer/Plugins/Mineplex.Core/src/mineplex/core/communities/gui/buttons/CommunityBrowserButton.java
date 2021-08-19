package mineplex.core.communities.gui.buttons;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilText;
import mineplex.core.communities.data.ICommunity;
import mineplex.core.communities.gui.pages.CommunityMembersPage;
import mineplex.core.itemstack.ItemBuilder;

public class CommunityBrowserButton extends CommunitiesGUIButton
{
	private ICommunity _community;
	private Player _viewer;

	public CommunityBrowserButton(Player viewer, ICommunity community)
	{
		super(createButton(community));

		_viewer = viewer;
		_community = community;
	}

	private static ItemStack createButton(ICommunity community)
	{
		return new ItemBuilder(new ItemStack(community.getFavoriteGame().getMaterial(), 1, community.getFavoriteGame().getMaterialData()))
				.setTitle(C.cGreenB + community.getName())
				.addLore(UtilText.splitLinesToArray(new String[] {
						C.cRed,
						C.cYellow + "Members " + C.cWhite + community.getMemberCount(),
						C.cYellow + "Favorite Game " + C.cWhite + community.getFavoriteGame().getName(),
						C.cYellow + "Privacy " + C.cWhite + community.getPrivacySetting().getDisplayText(),
						C.cYellow + "Description " + C.cWhite + community.getDescription(),
						C.cBlue,
						C.cGreen + "Click to view community"}, LineFormat.LORE))
				.build();
	}

	@Override
	public void update()
	{
		Button = createButton(_community);
	}

	@Override
	public void handleClick(ClickType type)
	{
		getCommunityManager().tempLoadCommunity(_community.getId(), community ->
			new CommunityMembersPage(_viewer, community).open());
	}
}