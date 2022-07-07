package mineplex.core.party.ui.button.tools.owner;

import mineplex.core.common.util.C;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.party.Party;
import mineplex.core.party.PartyManager;
import mineplex.core.party.ui.button.tools.PartyButton;
import mineplex.core.party.ui.menus.input.PartyInvitePlayerMenu;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

/**
 * Sends an invitation to a specific player
 */
public class AddPlayerButton extends PartyButton
{

	private static final ItemStack ITEM = new ItemBuilder(Material.SIGN)
	  .setTitle(C.cYellow + "Invite a Player")
	  .setLore(" ", C.cGray + "Brings up the Inviting Anvil!", C.cGray + "Simply type a player's name", C.cGray + "and click the paper to invite them")
	  .build();

	public AddPlayerButton(PartyManager plugin, Party party)
	{
		super(ITEM, party, plugin);
	}

	@Override
	public void onClick(Player player, ClickType clickType)
	{
		new PartyInvitePlayerMenu(getPlugin(), player, getParty()).openInventory();
	}

}