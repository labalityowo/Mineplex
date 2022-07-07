package mineplex.core.party.ui.button.tools.main;

import mineplex.core.common.util.C;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.menu.Button;
import mineplex.core.party.PartyManager;
import mineplex.core.party.ui.menus.input.PartyInvitePlayerMenu;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

/**
 * Invites a player to a new party
 */
public class InvitePlayerButton extends Button<PartyManager>
{

	private static final ItemStack ITEM = new ItemBuilder(Material.NAME_TAG)
	  .setTitle(C.cYellow + "Invite a Player")
	  .setLore(" ", C.cGray + "Invites a player to join", C.cGray + "you in a new party.")
	  .build();

	public InvitePlayerButton(PartyManager plugin)
	{
		super(ITEM, plugin);
	}

	@Override
	public void onClick(Player player, ClickType clickType)
	{
		new PartyInvitePlayerMenu(getPlugin(), player, null).openInventory();
	}
}