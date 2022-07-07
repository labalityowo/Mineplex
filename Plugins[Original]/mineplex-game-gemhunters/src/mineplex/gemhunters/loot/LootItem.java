package mineplex.gemhunters.loot;

import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.UtilMath;

/**
 * Represents an item that can be contained in a chest inside the Gem Hunters
 * world.
 */
public class LootItem
{

	private final ItemStack _itemStack;
	private final int _minAmount;
	private final int _maxAmount;
	private final double _probability;
	private final String _metadata;
	
	public LootItem(ItemStack itemStack, int minAmount, int maxAmount, double probability, String metadata)
	{
		_itemStack = itemStack;
		_minAmount = minAmount;
		_maxAmount = maxAmount;
		_probability = probability;
		_metadata = metadata;
	}

	/**
	 * Returns the Minecraft {@link ItemStack} bound to this
	 * {@link LootItem}.<br>
	 * The {@link ItemStack} returned will have an amount/size between the
	 * minAmount and maxAmount integers (set within the constuctor's parameters)
	 * inclusively.
	 * 
	 * @return
	 */
	public ItemStack getItemStack()
	{
		_itemStack.setAmount(_minAmount + UtilMath.r(_maxAmount - _minAmount + 1));

		return _itemStack;
	}

	/**
	 * The minimum amount or size an {@link ItemStack} of this {@link LootItem}
	 * can have.
	 * 
	 * @return
	 */
	public int getMinAmount()
	{
		return _minAmount;
	}

	/**
	 * The maximum amount or size an {@link ItemStack} of this {@link LootItem}
	 * can have.
	 * 
	 * @return
	 */
	public int getMaxAmount()
	{
		return _maxAmount;
	}

	/**
	 * The double value of the item's probability of being chosen to when
	 * picking an individual chest's loot.
	 * 
	 * @return
	 */
	public double getProbability()
	{
		return _probability;
	}

	/**
	 * Any metadata bound to a {@link LootItem}. Useful for determining if an
	 * item has a particular <i>skill</i> or <i>ability</i> attached to it which
	 * you can use in code.
	 * 
	 * @return
	 */
	public String getMetadata()
	{
		return _metadata;
	}

}
