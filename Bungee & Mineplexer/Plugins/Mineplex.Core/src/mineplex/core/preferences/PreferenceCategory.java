package mineplex.core.preferences;

import mineplex.core.common.util.C;
import mineplex.core.itemstack.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 *
 */
public enum PreferenceCategory
{
	USER("User", Material.PAPER),
	EXCLUSIVE("Exclusive", Material.DIAMOND),
	GAME_PLAY("Game Mechanic", Material.REDSTONE_COMPARATOR),
	MISC("Miscellaneous", Material.COMPASS),
	SOCIAL("Social", Material.RED_ROSE),
	;

	private String _name;
	private ItemStack _itemStack;

	PreferenceCategory(String name, Material icon)
	{
		_name = name + " Preferences";
		_itemStack = new ItemBuilder(icon)
		  .setTitle(C.cYellow + _name)
		  .build();
	}

	public ItemStack getItem()
	{
		return _itemStack;
	}

	public String getName()
	{
		return _name;
	}
}