package mineplex.core.treasure.types;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.skin.SkinData;
import mineplex.core.common.util.C;
import mineplex.core.itemstack.ItemBuilder;

public enum TreasureType
{

	OLD(
			C.cYellow + "Old Treasure",
			"Old Chest",
			"Old",
			new ItemStack(Material.CHEST),
			"We've scoured the lands of Minecraft",
			"and found these abandoned chests.",
			"The contents are unknown, but",
			"according to the inscriptions on the",
			"the straps they appear to contain",
			"many kinds of loot."
	),
	ANCIENT(
			C.cGold + "Ancient Treasure",
			"Ancient Chest",
			"Ancient",
			new ItemStack(Material.TRAPPED_CHEST),
			"Some of our bravest adventurers",
			"have discovered these chests within ",
			"temples hidden in Minecrafts worlds."
	),
	MYTHICAL(
			C.cRed + "Mythical Treasure",
			"Mythical Chest",
			"Mythical",
			new ItemStack(Material.ENDER_CHEST),
			"All our previous adventurers have",
			"perished in search of these chests.",
			"However, legends of their existence",
			"convinced Sterling, Chiss and Defek7",
			"to venture out and discover the",
			"location of these chests on their own."
	),
	ILLUMINATED(
			C.cAqua + "Illuminated Treasure",
			"Illuminated Chest",
			"Illuminated",
			new ItemStack(Material.SEA_LANTERN),
			"The illuminated chest shines brightly",
			"in the depths, always bringing a new",
			"treasure from the darkness."
	),
	OMEGA(
			C.cAqua + "Omega Chest",
			"Omega Chest",
			"Omega",
			SkinData.OMEGA_CHEST.getSkull(),
			"The most powerful of all chests,",
			"it is able to go back in time to find",
			"loot that has been lost..."
	),
	MINESTRIKE(
			C.cGold + "MineStrike Treasure",
			"Minestrike Chest",
			"MinestrikeChest",
			new ItemStack(Material.TNT),
			"The Minestrike Chest is the only place to get",
			"the unique skins for Minestrike weapons!"
	),
	MOBA(
			C.cAqua + "GWEN's Treasure",
			"HOG Chest",
			"HOGChest",
			new ItemStack(Material.PRISMARINE),
			"Heroes of GWEN exclusive cosmetics!",
			"Each chest contains 4 exclusive items to the game!"
	),
	TRICK_OR_TREAT(
			C.cGold + "Trick or Treat Chest",
			"Trick or Treat Chest",
			"TrickOrTreat",
			new ItemStack(Material.SKULL_ITEM),
			"The Trick or Treat Chest contains all",
			"sorts of surprises, from Rank Upgrades to",
			"long lost Halloween items, and even other chests!"
	),
	TRICK_OR_TREAT_2017(
			C.cGold + "Trick or Treat Bag",
			"Trick or Treat Chest 2017",
			"TrickOrTreat2017",
			new ItemStack(Material.JACK_O_LANTERN),
			"It's that time of year again!",
			"Where the ghosts and ghouls roam",
			"free! Hahahaha"
	),
	CHRISTMAS(
			C.cDGreen + "Winter Holiday Treasure",
			"Winter Chest",
			"Christmas",
			SkinData.PRESENT.getSkull(),
			"Legend tells of the Winter Lord's",
			"vast treasure horde, locked away",
			"in a vault of ice, deep beneath the",
			"Frozen Sea. It is said it can only be",
			"accessed in the deepest parts of Winter..."
	),
	FREEDOM(
			C.cRed + "Freedom " + C.cBlue + "Treasure",
			"Freedom Treasure",
			"Freedom",
			SkinData.FREEDOM_CHEST.getSkull(),
			"It is said that George Washington",
			"carved this chest himself from the wood",
			"of the cherry tree he cut down..."
	),
	HAUNTED(
			C.cGold + "Haunted Chest",
			"Haunted Chest",
			"Haunted",
			SkinData.HAUNTED_CHEST.getSkull(),
			"The Haunted Chest can only be found",
			"during the month of October 2016 when the",
			"veil between this world and its shadow is thin..."
	),
	THANKFUL(
			C.cGold + "Thankful Treasure",
			"Thankful Chest",
			"Thankful",
			new ItemStack(Material.COOKED_CHICKEN),
			"The Thankful Chest is our way of",
			"showing thanks to you, containing items from Rank Upgrades to",
			"Power Play Club Subscriptions, among other things!"
	),
	GINGERBREAD(
			C.cRed + "Gingerbread " + C.cGreen + "Treasure",
			"Gingerbread Chest",
			"Gingerbread",
			SkinData.GINGERBREAD.getSkull(),
			"The legendary burglar, the Gingerbread Man,",
			"has finally been caught! Now, for the first time",
			"his loot is available for auction."
	),
	LOVE(
			C.cRed + "Love Treasure",
			"Love Chest",
			"LoveChest",
			new ItemBuilder(Material.WOOL,  (short) 6).build(),
			"Cupid and his hunters have searched far and wide",
			"to collect a whole bunch of lovey dovey items."
	),
	ST_PATRICKS(
			C.cGreen + "St Patrick's Treasure",
			"St Patricks Chest",
			"StPatricksChest",
			SkinData.LEPRECHAUN.getSkull(),
			"Happy St. Patrick's Day!",
			"Get your Pot's of Gold and",
			"Luck of the Irish in this chest!"
	),
	SPRING(
			C.cGreen + "Spring Treasure",
			"Spring Chest",
			"SpringChest",
			new ItemStack(Material.DOUBLE_PLANT, 1, (short) 4),
			"Spring is here!"
	),
	CARL_SPINNER(),
	GAME_LOOT(),
	;

	private final String _name;
	private final String _itemName;
	private final String _statName;
	private final ItemStack _itemStack;
	private final String[] _description;

	TreasureType()
	{
		this(null, null, null, null);
	}

	TreasureType(String name, String itemName, String statName, ItemStack itemStack, String... description)
	{
		_name = name;
		_itemName = itemName;
		_statName = statName;
		_itemStack = itemStack;
		_description = description;
	}

	public String getName()
	{
		return _name;
	}

	public String getItemName()
	{
		return _itemName;
	}

	public String getStatName()
	{
		return _statName;
	}

	public ItemStack getItemStack()
	{
		return _itemStack;
	}

	public String[] getDescription()
	{
		return _description;
	}
}
