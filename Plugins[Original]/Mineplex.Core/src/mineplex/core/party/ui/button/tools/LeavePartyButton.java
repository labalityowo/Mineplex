package mineplex.core.party.ui.button.tools;

import mineplex.core.common.util.C;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.menu.Button;
import mineplex.core.party.PartyManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

/**
 * Leaves a players current party
 */
public class LeavePartyButton extends Button<PartyManager>
{

	private static final ItemStack ITEM = new ItemBuilder(Material.REDSTONE_BLOCK)
	  .setTitle(C.cYellow + "Leave Party")
	  .setLore(" ", C.cRed + "Shift-Left-Click" + C.cGray + " to leave your party.")
	  .build();

	public LeavePartyButton(PartyManager plugin)
	{
		super(ITEM, plugin);
	}

	@Override
	public void onClick(Player player, ClickType clickType)
	{
		if(clickType != ClickType.SHIFT_LEFT)
		{
			return;
		}
		getPlugin().leaveParty(player);
		player.closeInventory();
	}

}