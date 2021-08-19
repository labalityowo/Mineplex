package mineplex.core.party.ui.button.tools.main;

import mineplex.core.common.util.C;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.menu.Button;
import mineplex.core.party.PartyManager;
import mineplex.core.party.ui.menus.PartyInvitesMenu;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

/**
 * Opens the {@code {@link PartyInvitesMenu}}
 */
public class ViewInvitesButton extends Button<PartyManager>
{

	private static final ItemStack ITEM = new ItemBuilder(Material.BOOK)
	  .setTitle(C.cYellow + "View Invites")
	  .setLore(" ", C.cGray + "Manage invites to parties.")
	  .build();

	public ViewInvitesButton(PartyManager plugin)
	{
		super(ITEM, plugin);
	}

	@Override
	public void onClick(Player player, ClickType clickType)
	{
		new PartyInvitesMenu(getPlugin()).open(player);
	}
}
