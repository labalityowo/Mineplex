package nautilus.game.pvp.modules.Fishing;

import org.bukkit.Material;

public enum Loot
{
	A(Material.VINE, 24, 200, Rarity.Common),
	B(Material.APPLE, 1, 200, Rarity.Common),
	C(Material.LOG, 32, 200, Rarity.Common),
	D(Material.DEAD_BUSH, 1, 200, Rarity.Common),
	E(Material.COOKED_CHICKEN, 1, 200, Rarity.Common),
	F(Material.COMPASS, 1, 200, Rarity.Common),
	G(Material.LEAVES, 32, 200, Rarity.Common),
	H(Material.FISHING_ROD, 1, 200, Rarity.Common),
	I(Material.BONE, 12, 200, Rarity.Common),

	IRONHelm(Material.IRON_HELMET, 35, Rarity.Moderate),
	IRONChest(Material.IRON_CHESTPLATE, 35, Rarity.Moderate),
	IRONLegs(Material.IRON_LEGGINGS, 35, Rarity.Moderate),
	IRONBoots(Material.IRON_BOOTS, 35, Rarity.Moderate),

	GOLDHelm(Material.GOLD_HELMET, 35, Rarity.Moderate),
	GOLDChest(Material.GOLD_CHESTPLATE, 35, Rarity.Moderate),
	GOLDLegs(Material.GOLD_LEGGINGS, 35, Rarity.Moderate),
	GOLDBoots(Material.GOLD_BOOTS, 35, Rarity.Moderate),

	LEATHERHelm(Material.LEATHER_HELMET, 35, Rarity.Moderate),
	LEATHERChest(Material.LEATHER_CHESTPLATE, 35, Rarity.Moderate),
	LEATHERLegs(Material.LEATHER_LEGGINGS, 35, Rarity.Moderate),
	LEATHERBoots(Material.LEATHER_BOOTS, 35, Rarity.Moderate),

	DIAMONDHelm(Material.DIAMOND_HELMET, 35, Rarity.Moderate),
	DIAMONDChest(Material.DIAMOND_CHESTPLATE, 35, Rarity.Moderate),
	DIAMONDLegs(Material.DIAMOND_LEGGINGS, 35, Rarity.Moderate),
	DIAMONDBoots(Material.DIAMOND_BOOTS, 35, Rarity.Moderate),

	CHAINMAILHelm(Material.CHAINMAIL_HELMET, 35, Rarity.Moderate),
	CHAINMAILChest(Material.CHAINMAIL_CHESTPLATE, 35, Rarity.Moderate),
	CHAINMAILLegs(Material.CHAINMAIL_LEGGINGS, 35, Rarity.Moderate),
	CHAINMAILBoots(Material.CHAINMAIL_BOOTS, 35, Rarity.Moderate),

	R1(Material.GOLD_SWORD, 25, Rarity.Rare),
	R2(Material.GOLD_AXE, 25, Rarity.Rare),
	R3(Material.DIAMOND_SWORD, 25, Rarity.Rare),
	R4(Material.DIAMOND_AXE, 25, Rarity.Rare),
	R5(Material.TNT, 25, Rarity.Rare),

	Unique(null, 1, Rarity.Legendary);

	private Material type;
	private int amount = 1;
	private int scale;
	private Rarity rarity;

	private Loot(Material type, int amount, int scale, Rarity rarity) 
	{
		this.scale = scale;
		this.amount = amount;
		this.type = type;
		this.rarity = rarity;

	}

	private Loot(Material type, int scale, Rarity rarity) 
	{
		this.scale = scale;
		this.type = type;
		this.rarity = rarity;

	}

	public int GetScale()
	{
		return scale;
	}

	public Material GetType()
	{
		return type;
	}

	public int GetAmount()
	{
		return amount;
	}

	public Rarity GetRarity()
	{
		return rarity;
	}
}
