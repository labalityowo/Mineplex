package mineplex.core.progression.gui.buttons;

import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.progression.ProgressiveKit;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

/**
 * This manages toggling default selection of a kit
 * I.E. Whether or not this kit will be always enabled without the user doing anything
 */
public class KitPermanentDefaultButton extends KitButton
{

	private static final String[] DESCRIPTION = {
	  " ",
	  ChatColor.WHITE + "Selects this kit as your default kit",
	  ChatColor.WHITE + "until you decide to change it.",
	  " ",
	  ChatColor.WHITE + "Default kits will be auto-selected",
	  ChatColor.WHITE + "when you join a lobby.",
	  " "
	};

	private static final ItemStack ITEM = new ItemBuilder(Material.STAINED_GLASS_PANE)
	  .setData(DyeColor.LIGHT_BLUE.getWoolData())
	  .setTitle(ChatColor.YELLOW + "Default Kit")
	  .setLore(DESCRIPTION)
	  .build();

	public KitPermanentDefaultButton(ProgressiveKit kit)
	{
		super(kit, ITEM);
	}

	@Override
	public void onClick(Player player, ClickType clickType)
	{
		getKit().onSelected(player.getUniqueId());

		getKit().onSetDefault(player.getUniqueId());

	}
}
