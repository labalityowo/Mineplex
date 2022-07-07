package mineplex.core.party.ui.menus;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.menu.Button;
import mineplex.core.menu.Menu;
import mineplex.core.menu.builtin.ButtonOpenInventory;
import mineplex.core.party.PartyManager;
import mineplex.core.party.ui.button.tools.main.InvitePlayerButton;
import mineplex.core.party.ui.button.tools.main.ViewInvitesButton;

public class PartyMainMenu extends Menu<PartyManager>
{
	private static final ItemStack VIEW_INVITES_ITEM = new ItemBuilder(Material.BOOK)
			.setTitle(C.cYellow + "View Invites")
			.setLore(" ", C.cGray + "Manage invites to parties.")
			.build();

	public PartyMainMenu(PartyManager plugin)
	{
		super("Mineplex Parties", plugin);
	}

	@Override
	protected Button[] setUp(Player player)
	{
		Button[] buttons = new Button[9];

		buttons[3] = new InvitePlayerButton(getPlugin());
		buttons[5] = new ButtonOpenInventory<>(VIEW_INVITES_ITEM, getPlugin(), () -> new PartyInvitesMenu(getPlugin()));

		return buttons;
	}
}