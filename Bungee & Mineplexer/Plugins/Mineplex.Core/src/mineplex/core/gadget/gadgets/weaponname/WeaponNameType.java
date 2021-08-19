package mineplex.core.gadget.gadgets.weaponname;

import org.bukkit.Material;

import mineplex.core.common.util.C;
import mineplex.core.gadget.types.WeaponNameGadget.WeaponType;
import mineplex.core.gadget.util.CostConstants;

public enum WeaponNameType
{

	SWORD_WOOD_SPLINTER(WeaponType.WOOD_SWORD, "Splinter", CostConstants.FOUND_IN_TREASURE_CHESTS, Material.STICK),
	SWORD_WOOD_THORN(WeaponType.WOOD_SWORD, "Thorn", CostConstants.FOUND_IN_TREASURE_CHESTS, Material.RED_ROSE),
	SWORD_WOOD_TOOTHPICK(WeaponType.WOOD_SWORD, "Kevin's Lost Toothpick", CostConstants.FOUND_IN_TREASURE_CHESTS, Material.GRASS),
	SWORD_WOOD_STRIKER(WeaponType.WOOD_SWORD, "Striker", CostConstants.FOUND_IN_TREASURE_CHESTS, Material.BLAZE_ROD),
	SWORD_WOOD_THORN_BUSH(WeaponType.WOOD_SWORD, "Thorn Bush", CostConstants.FOUND_IN_TREASURE_CHESTS, Material.DOUBLE_PLANT, (byte) 4),
	SWORD_WOOD_HOWLING_SWORD(WeaponType.WOOD_SWORD, "Howling Sword", CostConstants.FOUND_IN_TREASURE_CHESTS, Material.BONE),
	SWORD_WOOD_QUiCK_BLADE(WeaponType.WOOD_SWORD, "Quick Blade", CostConstants.FOUND_IN_TREASURE_CHESTS, Material.FEATHER),
	SWORD_WOOD_WINTER_THORN(WeaponType.WOOD_SWORD, "Winterthorn", CostConstants.FOUND_IN_TREASURE_CHESTS, Material.SNOW_BALL),
	SWORD_WOOD_SPIKE(WeaponType.WOOD_SWORD, "Needle", CostConstants.FOUND_IN_TREASURE_CHESTS, Material.TORCH),
	SWORD_WOOD_LONGSWORD(WeaponType.WOOD_SWORD, "Longsword", CostConstants.FOUND_IN_TREASURE_CHESTS, Material.WOOD_SWORD),
	SWORD_WOOD_WHISPER(WeaponType.WOOD_SWORD, "Whisper", CostConstants.FOUND_IN_TREASURE_CHESTS, Material.STRING),
	SWORD_WOOD_BRUISER(WeaponType.WOOD_SWORD, "Bruiser", CostConstants.FOUND_IN_TREASURE_CHESTS, Material.MAGMA_CREAM),

	SWORD_STONE_SPLINTER(WeaponType.STONE_SWORD, "Heart of the Mountain", CostConstants.FOUND_IN_TREASURE_CHESTS, Material.STONE_PICKAXE),
	SWORD_STONE_QUERN(WeaponType.STONE_SWORD, "Quern-Biter", CostConstants.FOUND_IN_TREASURE_CHESTS, Material.IRON_SPADE),
	SWORD_STONE_GRAVEN_EDGE(WeaponType.STONE_SWORD, "Graven Edge", CostConstants.FOUND_IN_TREASURE_CHESTS, Material.STONE),
	SWORD_STONE_PROTECTOR(WeaponType.STONE_SWORD, "Protector", CostConstants.FOUND_IN_TREASURE_CHESTS, Material.DIAMOND_CHESTPLATE),
	SWORD_STONE_GHOST_REAVER(WeaponType.STONE_SWORD, "Ghost Reaver", CostConstants.FOUND_IN_TREASURE_CHESTS, Material.SKULL_ITEM, (byte) 1),
	SWORD_STONE_SKULL_CRUSHER(WeaponType.STONE_SWORD, "Skullcrusher", CostConstants.FOUND_IN_TREASURE_CHESTS, Material.SKULL_ITEM),
	SWORD_STONE_RAGNAROK(WeaponType.STONE_SWORD, "Ragnarok", CostConstants.FOUND_IN_TREASURE_CHESTS, Material.LAVA_BUCKET),
	SWORD_STONE_WARMONGER(WeaponType.STONE_SWORD, "Warmonger", CostConstants.FOUND_IN_TREASURE_CHESTS, Material.BEACON),
	SWORD_STONE_GUTRENDER(WeaponType.STONE_SWORD, "Gutrender", CostConstants.FOUND_IN_TREASURE_CHESTS, Material.REDSTONE),
	SWORD_STONE_FROSTY(WeaponType.STONE_SWORD, "Frosty Greatsword", CostConstants.FOUND_IN_TREASURE_CHESTS, Material.SNOW_BALL),
	SWORD_STONE_SHADOW(WeaponType.STONE_SWORD, "Shadowstrike", CostConstants.FOUND_IN_TREASURE_CHESTS, Material.INK_SACK),
	SWORD_STONE_TENDERIZER(WeaponType.STONE_SWORD, "Tenderizer", CostConstants.FOUND_IN_TREASURE_CHESTS, Material.RAW_BEEF),

	SWORD_GOLD_DIGGER(WeaponType.GOLD_SWORD, "Gold Digger", CostConstants.FOUND_IN_TREASURE_CHESTS, Material.GOLD_PICKAXE),
	SWORD_GOLD_PEACEKEEPER(WeaponType.GOLD_SWORD, "Peacekeeper", CostConstants.FOUND_IN_TREASURE_CHESTS, Material.GOLD_HELMET),
	SWORD_GOLD_GLADIUS(WeaponType.GOLD_SWORD, "Gladius", CostConstants.FOUND_IN_TREASURE_CHESTS, Material.YELLOW_FLOWER),
	SWORD_GOLD_GHOST_WALKER(WeaponType.GOLD_SWORD, "Ghostwalker", CostConstants.FOUND_IN_TREASURE_CHESTS, Material.DIAMOND_HOE),
	SWORD_GOLD_SCAR(WeaponType.GOLD_SWORD, "Scar", CostConstants.FOUND_IN_TREASURE_CHESTS, Material.ANVIL, (byte) 2),
	SWORD_GOLD_BURN(WeaponType.GOLD_SWORD, "Burn", CostConstants.FOUND_IN_TREASURE_CHESTS, Material.LAVA_BUCKET),
	SWORD_GOLD_SUNSTRIKE(WeaponType.GOLD_SWORD, "Sun Strike", CostConstants.FOUND_IN_TREASURE_CHESTS, Material.BLAZE_POWDER),
	SWORD_GOLD_MAGEBLADE(WeaponType.GOLD_SWORD, "Mage Blade", CostConstants.FOUND_IN_TREASURE_CHESTS, Material.POTION),
	SWORD_GOLD_FLAMING_RAPIER(WeaponType.GOLD_SWORD, "Flaming Rapier", CostConstants.FOUND_IN_TREASURE_CHESTS, Material.MAGMA_CREAM),

	SWORD_IRON_ALIEN(WeaponType.IRON_SWORD, "Alien Artifact", CostConstants.FOUND_IN_TREASURE_CHESTS, Material.SLIME_BLOCK),
	SWORD_IRON_STORMBRINGER(WeaponType.IRON_SWORD, "Stormbringer", CostConstants.FOUND_IN_TREASURE_CHESTS, Material.WATER_BUCKET),
	SWORD_IRON_STING(WeaponType.IRON_SWORD, "Sting", CostConstants.FOUND_IN_TREASURE_CHESTS, Material.SHEARS),
	SWORD_IRON_DRAGONFIRE(WeaponType.IRON_SWORD, "Dragonfire", CostConstants.FOUND_IN_TREASURE_CHESTS, Material.DRAGON_EGG),
	SWORD_IRON_HOPE(WeaponType.IRON_SWORD, "Hope's End", CostConstants.FOUND_IN_TREASURE_CHESTS, Material.DETECTOR_RAIL),
	SWORD_IRON_DOOMBRINGER(WeaponType.IRON_SWORD, "Doombringer", CostConstants.FOUND_IN_TREASURE_CHESTS, Material.FIREBALL),
	SWORD_IRON_STORMCALLER(WeaponType.IRON_SWORD, "Storm Caller", CostConstants.FOUND_IN_TREASURE_CHESTS, Material.GHAST_TEAR),
	SWORD_IRON_BLACKOUT(WeaponType.IRON_SWORD, "Black Out", CostConstants.FOUND_IN_TREASURE_CHESTS, Material.WOOL, (byte) 15),

	SWORD_DIAMOND_ICE_HEART(WeaponType.DIAMOND_SWORD, "Ice Heart", C.cAquaB + "Ice Heart", CostConstants.NO_LORE, Material.ICE),
	SWORD_DIAMOND_SHINY(WeaponType.DIAMOND_SWORD, "Oooh Shiny", C.cGoldB + "Oooh Shiny", CostConstants.NO_LORE, Material.GOLD_NUGGET),
	SWORD_DIAMOND_DRAGONSLAYER(WeaponType.DIAMOND_SWORD, "Dragonslayer", C.cDRedB + "Dragonslayer", CostConstants.NO_LORE, Material.EYE_OF_ENDER),
	SWORD_DIAMOND_OBLIVION(WeaponType.DIAMOND_SWORD, "Oblivion", C.cRedB + "Oblivion", CostConstants.NO_LORE, Material.ENDER_PEARL),
	SWORD_DIAMOND_DEATHRAZE(WeaponType.DIAMOND_SWORD, "Deathraze", C.cRedB + "Deathraze", CostConstants.NO_LORE, Material.DARK_OAK_STAIRS),
	SWORD_DIAMOND_OATHBREAKER(WeaponType.DIAMOND_SWORD, "Oathbreaker", C.cGreenB + "Oathbreaker", CostConstants.NO_LORE, Material.SPIDER_EYE),
	SWORD_DIAMOND_VANQUISHER(WeaponType.DIAMOND_SWORD, "Vanquisher", C.cYellowB + "Vanquisher", CostConstants.NO_LORE, Material.DIAMOND_AXE),
	SWORD_DIAMOND_NOBLE_PHANTASM(WeaponType.DIAMOND_SWORD, "Noble Phantasm", C.cAquaB + "Noble Phantasm", CostConstants.NO_LORE, Material.CAKE),;

	private final WeaponType _weaponType;
	private final String _name, _display;
	private final int _cost;
	private final Material _material;
	private final byte _materialData;

	WeaponNameType(WeaponType weaponType, String name, int cost, Material material)
	{
		this(weaponType, name, name, cost, material);
	}

	WeaponNameType(WeaponType weaponType, String name, int cost, Material material, byte materialData)
	{
		this(weaponType, name, name, cost, material, materialData);
	}

	WeaponNameType(WeaponType weaponType, String name, String display, int cost, Material material)
	{
		this(weaponType, name, display, cost, material, (byte) 0);
	}

	WeaponNameType(WeaponType weaponType, String name, String display, int cost, Material material, byte materialData)
	{
		_weaponType = weaponType;
		_name = name;
		_display = display;
		_cost = cost;
		_material = material;
		_materialData = materialData;
	}

	public WeaponType getWeaponType()
	{
		return _weaponType;
	}

	public String getName()
	{
		return _name;
	}

	public String getDisplay()
	{
		return _display;
	}

	public int getCost()
	{
		return _cost;
	}

	public Material getMaterial()
	{
		return _material;
	}

	public byte getMaterialData()
	{
		return _materialData;
	}
}
