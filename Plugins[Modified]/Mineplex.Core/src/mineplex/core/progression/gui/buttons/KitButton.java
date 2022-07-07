package mineplex.core.progression.gui.buttons;

import mineplex.core.menu.Button;
import mineplex.core.progression.KitProgressionManager;
import mineplex.core.progression.ProgressiveKit;
import org.bukkit.inventory.ItemStack;

/**
 * Similar to KitMenu, this helps with organizing buttons related to kits
 * Since they all share common features.
 */
public abstract class KitButton extends Button<KitProgressionManager>
{

	private ProgressiveKit _kit;

	public KitButton(ProgressiveKit kit, ItemStack itemStack)
	{
		this(kit, itemStack, null);
	}

	public KitButton(ProgressiveKit kit, ItemStack itemStack, KitProgressionManager plugin)
	{
		super(itemStack, plugin);
		_kit = kit;
	}

	protected ProgressiveKit getKit()
	{
		return _kit;
	}

}
