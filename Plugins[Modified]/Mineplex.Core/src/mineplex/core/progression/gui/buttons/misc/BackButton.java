package mineplex.core.progression.gui.buttons.misc;

import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.menu.Button;
import mineplex.core.progression.KitProgressionManager;
import mineplex.core.progression.gui.guis.KitMenu;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

/**
 * This button take you back to the specified menu
 */
public class BackButton extends Button<KitProgressionManager>
{

	private static ItemStack ITEM = new ItemBuilder(Material.BED)
	  .setTitle(ChatColor.GRAY + "\u21FD Go Back")
	  .build();

	private KitMenu _toMenu;

	public BackButton(KitMenu toMenu)
	{
		super(ITEM, null);
		_toMenu = toMenu;
	}

	@Override
	public void setItemStack(ItemStack itemStack)
	{
		//You can't remove this item
	}

	@Override
	public void onClick(Player player, ClickType clickType)
	{
		_toMenu.open(player);
	}
}
