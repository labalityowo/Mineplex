package nautilus.game.arcade.game.modules.chest;

import mineplex.core.common.util.UtilMath;
import org.bukkit.inventory.ItemStack;

public class ChestLootItem
{

	private ItemStack _item;
	private int _lowestAmount, _highestAmount;

	ChestLootItem(ItemStack item, int lowestAmount, int highestAmount)
	{
		_item = item;
		_lowestAmount = lowestAmount;
		_highestAmount = highestAmount;
	}

	public ItemStack getItem()
	{
		ItemStack itemStack = _item.clone();

		if (_lowestAmount != _highestAmount)
		{
			itemStack.setAmount(UtilMath.rRange(_lowestAmount, _highestAmount));
		}

		return itemStack;
	}
}