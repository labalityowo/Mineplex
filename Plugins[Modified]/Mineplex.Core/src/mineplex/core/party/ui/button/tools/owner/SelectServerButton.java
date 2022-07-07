package mineplex.core.party.ui.button.tools.owner;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilServer;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.party.Party;
import mineplex.core.party.event.PartySelectServerEvent;
import mineplex.core.party.ui.button.tools.PartyButton;

/**
 * Opens the server selection menu
 */
public class SelectServerButton extends PartyButton
{

	private static final ItemStack ITEM = new ItemBuilder(Material.COMPASS)
			.setTitle(C.cYellow + "Select Server")
			.setLore(" ", C.cGray + "Brings up the Server Selection GUI")
			.build();

	public SelectServerButton(Party party)
	{
		super(ITEM, party, null);
	}

	@Override
	public void onClick(Player player, ClickType clickType)
	{
		UtilServer.getPluginManager().callEvent(new PartySelectServerEvent(player));
	}
}