package mineplex.core.treasure.reward;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.itemstack.ItemBuilder;

public enum RewardRarity
{

	COMMON("Common", C.cWhite, C.cGray, 7),
	UNCOMMON("Uncommon", C.cAqua, C.cDAqua, 3),
	RARE("Rare", C.cPurple, C.cDPurple, 10),
	LEGENDARY("Legendary", C.cGreen, C.cDGreen, 5),
	MYTHICAL("Mythical", C.cRed, C.cDRed, 14);

	private final String _name;
	private final String _color;
	private final String _darkColor;
	private final ItemStack _itemStack;

	RewardRarity(String name, String color, String darkColor, int itemStackData)
	{
		_name = name;
		_color = color;
		_darkColor = darkColor;
		_itemStack = new ItemBuilder(Material.STAINED_GLASS_PANE)
				.setData((short) itemStackData)
				.build();
	}

	public String getName()
	{
		return _name;
	}

	public String getColor()
	{
		return _color;
	}

	public String getDarkColor()
	{
		return _darkColor;
	}

	public ItemStack getItemStack()
	{
		return _itemStack;
	}
}
