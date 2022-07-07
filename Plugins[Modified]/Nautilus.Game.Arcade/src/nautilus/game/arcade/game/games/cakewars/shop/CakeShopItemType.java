package nautilus.game.arcade.game.games.cakewars.shop;

import mineplex.core.common.util.UtilItem.ItemCategory;

public enum CakeShopItemType
{

	// Weapons
	SWORD(ItemCategory.SWORD, false, false, true),
	BOW(false, false, true),

	// Tools
	AXE(ItemCategory.AXE, false, false, true),
	PICKAXE(ItemCategory.PICKAXE, false, false, true),
	SHEARS(false, false, true),

	// Armour
	HELMET(false, false, true),
	CHESTPLATE(false, false, true),
	LEGGINGS(false, false, true),
	BOOTS(false, false, true),

	// Blocks
	BLOCK(true, false, true),

	// Special
	TEAM_UPGRADE(false, false, false),
	TRAP(false, true, false),

	// Other
	OTHER(true, false, true);

	private final ItemCategory _removeOnPurchase;
	private final boolean _multiBuy;
	private final boolean _onePerTeam;
	private final boolean _isItem;

	CakeShopItemType(boolean multiBuy, boolean onePerTeam, boolean isItem)
	{
		this(null, multiBuy, onePerTeam, isItem);
	}

	CakeShopItemType(ItemCategory removeOnPurchase, boolean multiBuy, boolean onePerTeam, boolean isItem)
	{
		_removeOnPurchase = removeOnPurchase;
		_multiBuy = multiBuy;
		_onePerTeam = onePerTeam;
		_isItem = isItem;
	}

	public ItemCategory getRemoveOnPurchase()
	{
		return _removeOnPurchase;
	}

	public boolean isMultiBuy()
	{
		return _multiBuy;
	}

	public boolean isOnePerTeam()
	{
		return _onePerTeam;
	}

	public boolean isItem()
	{
		return _isItem;
	}
}
