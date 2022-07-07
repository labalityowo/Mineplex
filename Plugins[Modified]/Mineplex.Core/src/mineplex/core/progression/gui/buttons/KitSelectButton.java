package mineplex.core.progression.gui.buttons;

import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilText;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.progression.ProgressiveKit;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

/**
 * This manages selecting of the kit for the current game.
 */
public class KitSelectButton extends KitButton
{

	private static final String[] LORE = {
	  " ",
	  ChatColor.WHITE + "Click to select this kit",
	  " "
	};

	public KitSelectButton(ProgressiveKit kit)
	{
		super(kit, new ItemBuilder(Material.STAINED_GLASS_PANE)
		  .setData(DyeColor.LIME.getWoolData())
		  .setTitle(ChatColor.YELLOW + kit.getDisplayName())
		  .setLore(UtilText.splitLinesToArray(LORE, LineFormat.LORE))
		  .build());
	}

	@Override
	public void onClick(Player player, ClickType clickType)
	{
		getKit().onSelected(player.getUniqueId());
	}
}
