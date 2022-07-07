package mineplex.core.progression.gui.guis;

import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.menu.Menu;
import mineplex.core.progression.KitProgressionManager;
import mineplex.core.progression.ProgressiveKit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * An implementation of {@link Menu} corresponding to kits
 */
public abstract class KitMenu extends Menu<KitProgressionManager>
{

	/**
	 * Coming soon icon for upgrades
	 */
	protected static final ItemStack COMING_SOON = new ItemBuilder(Material.INK_SACK)
	  .setAmount(1)
	  .setData(DyeColor.GRAY.getDyeData())
	  .setTitle(ChatColor.RED + "Coming Soon")
	  .setLore(" ", ChatColor.GRAY + "Upgrades coming soon!")
	  .build();

	private ProgressiveKit _kit;

	public KitMenu(ProgressiveKit kit, KitProgressionManager manager)
	{
		super(kit.getDisplayName(), manager);
		_kit = kit;
	}

	public ProgressiveKit getKit()
	{
		return _kit;
	}
}
