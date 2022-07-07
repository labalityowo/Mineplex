package mineplex.core.party.ui.button.tools.owner;

import mineplex.core.common.util.C;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.menu.Button;
import mineplex.core.party.PartyManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

/**
 * Disbands a party
 */
public class DisbandPartyButton extends Button<PartyManager>
{

	private static final ItemStack ITEM = new ItemBuilder(Material.BARRIER)
	  .setTitle(C.cRedB + "Disband your party")
	  .setLore(" ", C.cGray + "This will erase your party!", C.cRed + "Shift-Right-Click" + C.cGray + " to disband.")
	  .build();

	public DisbandPartyButton(PartyManager plugin)
	{
		super(ITEM, plugin);
	}

	@Override
	public void onClick(Player player, ClickType clickType)
	{
		if(clickType != ClickType.SHIFT_RIGHT)
		{
			return;
		}
		getPlugin().disband(player);
		player.closeInventory();
	}
}