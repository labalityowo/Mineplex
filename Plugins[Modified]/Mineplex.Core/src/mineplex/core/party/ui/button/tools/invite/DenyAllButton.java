package mineplex.core.party.ui.button.tools.invite;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.menu.Button;
import mineplex.core.party.PartyManager;
import mineplex.core.party.ui.menus.PartyInvitesMenu;

/**
 * Deny's all invites currently pending
 */
public class DenyAllButton extends Button<PartyManager>
{

	private static final ItemStack ITEM = new ItemBuilder(Material.REDSTONE_BLOCK)
			.setTitle(C.cRed + "Delete all Invites")
			.setLore(" ", C.cGray + "This will remove all pending invites.")
			.build();

	public DenyAllButton(PartyManager plugin)
	{
		super(ITEM, plugin);
	}

	@Override
	public void onClick(Player player, ClickType clickType)
	{
		getPlugin().getInviteManager().getAllInvites(player).forEach(inviteData -> getPlugin().denyInviteBySender(player, inviteData.getInviterName()));
		new PartyInvitesMenu(getPlugin()).open(player);
	}
}