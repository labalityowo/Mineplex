package mineplex.core.party.ui.button.tools.view;

import mineplex.core.common.util.C;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.party.Party;
import mineplex.core.party.PartyManager;
import mineplex.core.party.ui.button.tools.PartyButton;
import mineplex.core.party.ui.menus.input.PlayerSuggestPlayerMenu;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

/**
 * Suggest a new player to be invited
 */
public class SuggestPlayerButton extends PartyButton
{

	private static final ItemStack ITEM = new ItemBuilder(Material.BOOK_AND_QUILL)
	  .setTitle(C.cYellow + "Suggest Player")
	  .setLore(" ", C.cGray + "Suggest a player for the owner to invite")
	  .build();

	public SuggestPlayerButton(Party party, PartyManager plugin)
	{
		super(ITEM, party, plugin);
	}

	@Override
	public void onClick(Player player, ClickType clickType)
	{
		new PlayerSuggestPlayerMenu(getPlugin(), player, getParty()).openInventory();
	}
}
