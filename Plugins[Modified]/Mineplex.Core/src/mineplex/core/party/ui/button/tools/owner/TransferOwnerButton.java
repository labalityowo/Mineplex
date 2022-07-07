package mineplex.core.party.ui.button.tools.owner;

import mineplex.core.common.util.C;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.party.Party;
import mineplex.core.party.PartyManager;
import mineplex.core.party.ui.button.tools.PartyButton;
import mineplex.core.party.ui.menus.input.PartyTransferOwnerMenu;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

/**
 * Transfers ownership of a party to another player
 */
public class TransferOwnerButton extends PartyButton
{

	private static final ItemStack ITEM = new ItemBuilder(Material.BOOK_AND_QUILL)
	  .setTitle(C.cYellow + "Transfer Ownership")
	  .setLore(" ", C.cGray + "Transfers ownership of the party", C.cGray + "to another player",
		" ", C.cDRed + "This cannot be undone!")
	  .build();

	public TransferOwnerButton(Party party, PartyManager plugin)
	{
		super(ITEM, party, plugin);
	}

	@Override
	public void onClick(Player player, ClickType clickType)
	{
		new PartyTransferOwnerMenu(getPlugin(), player, getParty()).openInventory();
	}
}