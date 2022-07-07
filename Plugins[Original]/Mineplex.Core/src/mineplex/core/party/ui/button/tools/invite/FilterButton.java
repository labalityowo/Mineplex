package mineplex.core.party.ui.button.tools.invite;

import mineplex.core.common.util.C;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.menu.Button;
import mineplex.core.party.PartyManager;
import mineplex.core.party.ui.menus.input.InviteFilterMenu;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

/**
 * Filters all current pending invites and displays only the results to a player
 */
public class FilterButton extends Button<PartyManager>
{

	private static final ItemStack ITEM = new ItemBuilder(Material.NAME_TAG)
	  .setTitle(C.cYellow + "Filter Invites")
	  .setLore(" ",
		C.cGray + "Click to bring up an Anvil GUI",
		C.cGray + "where you type and filter",
		C.cGray + "Party invites by their name",
		" ",
		C.cGreen + "Input \"Clear Filter\" to remove the filter")
	  .build();

	public FilterButton(PartyManager plugin)
	{
		super(ITEM, plugin);
	}

	public FilterButton(String filter, PartyManager plugin)
	{
		super(new ItemBuilder(Material.NAME_TAG)
		  .setTitle(C.cYellow + "Filter Invites")
		  .setLore(" ", C.cWhite + "Active Filter: " + C.cGreen + filter, " ",
			C.cGray + "Click to bring up an Anvil GUI",
			C.cGray + "where you type and filter",
			C.cGray + "Party invites by their name",
			" ",
			C.cGreen + "Input \"Clear Filter\" to remove the filter")
		  .setGlow(true)
		  .build(), plugin);
	}

	@Override
	public void onClick(Player player, ClickType clickType)
	{
		new InviteFilterMenu(getPlugin(), player, null).openInventory();
	}

}