package nautilus.game.arcade.game.games.cakewars.shop;

import org.bukkit.inventory.ItemStack;

public class CakeShopItem implements CakeItem
{

	private final CakeShopItemType _itemType;
	private final ItemStack _itemStack;
	private final int _cost;

	public CakeShopItem(CakeShopItemType itemType, ItemStack itemStack, int cost)
	{
		_itemType = itemType;
		_itemStack = itemStack;
		_cost = cost;
	}

	@Override
	public CakeShopItemType getItemType()
	{
		return _itemType;
	}

	@Override
	public ItemStack getItemStack()
	{
		return _itemStack;
	}

	@Override
	public int getCost()
	{
		return _cost;
	}
}
