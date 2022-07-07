package mineplex.core.gadget.gadgets.hat;

import mineplex.core.common.skin.SkinData;
import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilText;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public enum HatType
{

	COAL("Lump of Coal Hat", UtilText.splitLineToArray(C.cGray + "When life gives you coal, make a weird cube hat out it!", LineFormat.LORE), -1, Material.COAL_BLOCK),
	COMPANION_BLOCK("Companion Block", UtilText.splitLineToArray(C.cGray + "The Enrichment Center is required to remind you that the Weighted Companion cube cannot talk. In the event that it does talk The Enrichment Center asks you to ignore its advice.", LineFormat.LORE), -2, SkinData.COMPANION_CUBE, "Companion"),
	GRINCH("The Grinch", UtilText.splitLineToArray(C.cGray + "Great! Now where's the Roast Beast?!", LineFormat.LORE), -3, SkinData.THE_GRINCH, "The Grinch Hat"),
	LOVESTRUCK("Love Struck", UtilText.splitLineToArray(C.cGray + "I think I'm in love... Wait a minute, did someone use a love potion on me?!", LineFormat.LORE), -6, SkinData.LOVESTRUCK, "Love Struck Hat", "Lovestruck"),
	PRESENT("Present", UtilText.splitLineToArray(C.cGray + "WHAT'S IN THE PRESENT? Oh, it's just you...", LineFormat.LORE), -3, SkinData.PRESENT),
	RUDOLPH("Rudolph", UtilText.splitLineToArray(C.cGray + "HEY YOU! Wanna lead Santa's sleigh team?", LineFormat.LORE), -3, SkinData.RUDOLPH),
	SANTA("Santa", UtilText.splitLineToArray(C.cGray + "Now you can work the Mall circuit!", LineFormat.LORE), -3, SkinData.SANTA),
	SECRET_PACKAGE("Secret Package", UtilText.splitLineToArray(C.cGray + "I hope Chiss is inside!", LineFormat.LORE), -6, SkinData.SECRET_PACKAGE),
	SNOWMAN("Snowman Head", UtilText.splitLineToArray(C.cGray + "Do you want to be a snowman?", LineFormat.LORE), -2, SkinData.SNOWMAN),
	TEDDY_BEAR("Teddy Bear", UtilText.splitLineToArray(C.cGray + "Aww, it's a cute teddy bear! What shall I name him?", LineFormat.LORE), -6, SkinData.TEDDY_BEAR),
	UNCLE_SAM("Uncle Sam Hat", UtilText.splitLineToArray(UtilText.colorWords("Uncle Sam has a big hat but now you can too.", ChatColor.RED, ChatColor.WHITE, ChatColor.BLUE), LineFormat.LORE), -8, SkinData.UNCLE_SAM),
	PUMPKIN("Pumpkin Hat", UtilText.splitLineToArray(C.cGray + "Pumpkin on the head, don't end up dead!", LineFormat.LORE), -9, Material.PUMPKIN),
	CANADA("Warm Canadian Hat", UtilText.splitLineToArray(C.cGray + "Keep your ears nice and warm while up north.", LineFormat.LORE), -8, SkinData.CANADA_HAT),
	AMERICA("Patriotic American Hat", UtilText.splitLineToArray(C.cGray + "Careful not to get a big head.", LineFormat.LORE), -8, SkinData.AMERICA_HAT),

	;

	private final String _name;
	private final String[] _lore;
	private final int _cost;
	private final ItemStack _hat;
	private final String[] _altNames;

	HatType(String name, String[] lore, int cost, Material material, String... altNames)
	{
		_name = name;
		_lore = lore;
		_cost = cost;
		_hat = new ItemStack(material);
		_altNames = altNames;
	}

	HatType(String name, String[] lore, int cost, SkinData skin, String... altNames)
	{
		_name = name;
		_lore = lore;
		_cost = cost;
		_hat = skin.getSkull();
		_altNames = altNames;
	}

	public String getName()
	{
		return _name;
	}

	public String[] getLore()
	{
		return _lore;
	}

	public int getCost()
	{
		return _cost;
	}

	public ItemStack getHat()
	{
		return _hat;
	}

	public String[] getAltNames()
	{
		return _altNames;
	}

}
