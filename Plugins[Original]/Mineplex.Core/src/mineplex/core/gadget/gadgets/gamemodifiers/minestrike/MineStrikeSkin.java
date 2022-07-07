package mineplex.core.gadget.gadgets.gamemodifiers.minestrike;

import org.bukkit.Material;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum MineStrikeSkin
{

	P250_Muertos(				  "P250", 		  "P250 Muertos", 				Material.INK_SACK, 			(byte)  3,	1),
	CZ75_Auto_Tigris(			  "CZ75-Auto", 	  "CZ75-Auto Tigris", 			Material.CLAY_BRICK, 		(byte)  0,	1),
	Desert_Eagle_Blaze(			  "Desert Eagle", "Desert Eagle Blaze", 		Material.NETHER_STALK,		(byte)  0, 	1),
	Desert_Eagle_Golden_Gun(      "Desert Eagle", "Golden Gun",                 Material.GLOWSTONE_DUST,    (byte)  0,	1),
	Nova_Koi(					  "Nova", 		  "Nova Koi", 					Material.INK_SACK, 			(byte) 14,	2),
	XM1014_Tranquility(			  "XM1014", 	  "XM1014 Tranquility", 		Material.DIAMOND, 			(byte)  0, 	2),
	XM1014_Pig_Gun(               "XM1014",       "XM1014 Pig Gun",             Material.LEATHER,           (byte)  0,	2),
	PP_Bizon_Streak(			  "PP-Bizon", 	  "PP-Bizon Streak", 			Material.INK_SACK, 			(byte)  4,	3),
	P90_Asiimov(				  "P90", 		  "P90 Asiimov", 				Material.INK_SACK, 			(byte)  0,	3),
	SSG_08_Blood_in_the_Water(	  "SSG 08", 	  "SSG 08 Blood in the Water",	Material.INK_SACK, 			(byte) 12,	5),
	AWP_Asiimov(				  "AWP", 		  "AWP Asiimov", 				Material.SULPHUR, 			(byte)  0,	5),
	P2000_Fire_Elemental(		  "P2000", 		  "P2000 Fire Elemental", 		Material.INK_SACK, 			(byte)  6,	1),
	FAMAS_Pulse(				  "FAMAS",		  "FAMAS Pulse",				Material.CLAY_BALL, 		(byte)  0,	4),
	M4A4_Howl(					  "M4A4", 		  "M4A4 Howl", 					Material.INK_SACK, 			(byte) 11,	4),
	M4A4_Enderman(                "M4A4",         "Enderman M4",                Material.COAL,              (byte)  0,	4),
	Steyr_AUG_Torque(			  "Steyr AUG", 	  "Steyr AUG Torque", 			Material.BLAZE_ROD, 		(byte)  0,	5),
	Glock_18_Fade(				  "Glock 18", 	  "Glock 18 Fade",				Material.INK_SACK, 			(byte)  9,	1),
	Galil_AR_Eco(				  "Galil AR", 	  "Galil AR Eco", 				Material.INK_SACK, 			(byte) 10,	4),
	AK_47_Vulcan(				  "AK-47", 		  "AK-47 Vulcan", 				Material.INK_SACK, 			(byte)  7,	4),
	AK_47_Guardian(               "AK-47",        "Guardian AK",                Material.PRISMARINE_SHARD,  (byte)  0,	4),
	SG553_Pulse(				  "SG553", 		  "SG553 Pulse", 				Material.INK_SACK, 			(byte)  5,	5),


	Knife_M9_Bayonette_Fade(	  "Knife", 		  "M9 Bayonette Fade", 			Material.DIAMOND_SWORD, 	(byte)  0,	6),
	Knife_Counter_Terrorist_Sword("Knife",        "Counter Terrorist Sword",    Material.STICK,             (byte)  0,	6),
	Knife_Terrorist_Sword(        "Knife",        "Terrorist Sword",            Material.FEATHER,           (byte)  0,	6),
	Knife_M9_Bayonette_Glass(     "Knife",        "Glass M9 Bayonette",         Material.QUARTZ,            (byte)  0,	6),
	Knife_2017_Champ_Knife(       "Knife",        "2017 Championship Knife",    Material.STONE_SWORD,       (byte)  0,	6);

	private static final Map<Integer, List<MineStrikeSkin>> BY_CATEGORY;

	static
	{
		BY_CATEGORY = new HashMap<>();

		for (MineStrikeSkin skin : MineStrikeSkin.values())
		{
			BY_CATEGORY.putIfAbsent(skin.getCategory(), new ArrayList<>());
			BY_CATEGORY.get(skin.getCategory()).add(skin);
		}
	}

	public static List<MineStrikeSkin> getByCategory(int category)
	{
		return BY_CATEGORY.get(category);
	}

	private final String _weaponName;
	private final String _skinName;
	private final Material _skinMaterial;
	private final byte _skinData;
	private final int _category;

	MineStrikeSkin(String weaponName, String skinName, Material newMaterial, byte newData, int category)
	{
		_weaponName = weaponName;
		_skinName = skinName;
		_skinMaterial = newMaterial;
		_skinData = newData;
		_category = category;
	}

	public String getWeaponName()
	{
		return _weaponName;
	}

	public String getSkinName()
	{
		return _skinName;
	}

	public Material getSkinMaterial()
	{
		return _skinMaterial;
	}

	public byte getSkinData()
	{
		return _skinData;
	}

	public int getCategory()
	{
		return _category;
	}

}
