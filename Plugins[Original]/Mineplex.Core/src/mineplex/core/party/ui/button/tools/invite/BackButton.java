package mineplex.core.party.ui.button.tools.invite;

import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.menu.Button;
import mineplex.core.party.PartyManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

/**
 * Sends a player back to the main page
 */
public class BackButton extends Button<PartyManager>
{

	private static ItemStack ITEM = new ItemBuilder(Material.BED)
	  .setTitle(ChatColor.GRAY + "\u21FD Go Back")
	  .build();

	public BackButton(PartyManager plugin)
	{
		super(ITEM, plugin);
	}

	@Override
	public void onClick(Player player, ClickType clickType)
	{
		player.closeInventory();
		player.chat("/party");
	}
}