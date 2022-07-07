package mineplex.core.gadget.gadgets.balloons;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilText;
import mineplex.core.disguise.disguises.DisguiseBase;
import mineplex.core.disguise.disguises.DisguiseBat;
import mineplex.core.disguise.disguises.DisguiseCat;
import mineplex.core.disguise.disguises.DisguiseCow;
import mineplex.core.disguise.disguises.DisguiseGuardian;
import mineplex.core.disguise.disguises.DisguiseMooshroom;
import mineplex.core.disguise.disguises.DisguisePig;
import mineplex.core.disguise.disguises.DisguiseSheep;
import mineplex.core.disguise.disguises.DisguiseSilverFish;
import mineplex.core.disguise.disguises.DisguiseSlime;
import mineplex.core.disguise.disguises.DisguiseSquid;
import mineplex.core.disguise.disguises.DisguiseVillager;
import mineplex.core.disguise.disguises.DisguiseWolf;
import mineplex.core.disguise.disguises.DisguiseZombie;
import mineplex.core.gadget.util.CostConstants;
import mineplex.core.shop.item.IDisplayPackage;

public enum BalloonType implements IDisplayPackage
{

	BABY_COW(DisguiseCow.class, "Baby Cow", Material.MONSTER_EGG, UtilEnt.getEntityEggData(EntityType.COW)),
	BABY_PIG(DisguisePig.class, "Baby Pig", Material.MONSTER_EGG, UtilEnt.getEntityEggData(EntityType.PIG)),
	BABY_ZOMBIE(DisguiseZombie.class, "Baby Zombie", Material.MONSTER_EGG, UtilEnt.getEntityEggData(EntityType.ZOMBIE)),
	BABY_MUSHROOM(DisguiseMooshroom.class, "Baby Mushroom Cow", Material.MONSTER_EGG, UtilEnt.getEntityEggData(EntityType.MUSHROOM_COW)),
	BABY_OCELOT(DisguiseCat.class, "Baby Ocelot", Material.MONSTER_EGG, UtilEnt.getEntityEggData(EntityType.OCELOT)),
	BABY_WOLF(DisguiseWolf.class, "Baby Wolf", Material.MONSTER_EGG, UtilEnt.getEntityEggData(EntityType.WOLF)),
	BABY_SHEEP(DisguiseSheep.class, "Baby Sheep", Material.MONSTER_EGG, UtilEnt.getEntityEggData(EntityType.SHEEP)),
	BABY_VILLAGER(DisguiseVillager.class, "Baby Villager", Material.MONSTER_EGG, UtilEnt.getEntityEggData(EntityType.VILLAGER)),
	BABY_SLIME(DisguiseSlime.class, "Baby Slime", Material.MONSTER_EGG, UtilEnt.getEntityEggData(EntityType.SLIME)),

	SQUID(DisguiseSquid.class, "Squid", Material.MONSTER_EGG, UtilEnt.getEntityEggData(EntityType.SQUID)),
	BAT(DisguiseBat.class, "Bat", Material.MONSTER_EGG, UtilEnt.getEntityEggData(EntityType.BAT)),
	SILVERFISH(DisguiseSilverFish.class, "Silverfish", Material.MONSTER_EGG, UtilEnt.getEntityEggData(EntityType.SILVERFISH)),
	GUARDIAN(DisguiseGuardian.class, "Guardian", Material.MONSTER_EGG, UtilEnt.getEntityEggData(EntityType.GUARDIAN)),

	DRAGON_EGG(new ItemStack(Material.DRAGON_EGG), "Dragon Egg", Material.DRAGON_EGG, 0),
	DIAMOND_BLOCK(new ItemStack(Material.DIAMOND_BLOCK), "Diamond Block", Material.DIAMOND_BLOCK, 0),
	IRON_BLOCK(new ItemStack(Material.IRON_BLOCK), "Iron Block", Material.IRON_BLOCK, 0),
	GOLD_BLOCK(new ItemStack(Material.GOLD_BLOCK), "Gold Block", Material.GOLD_BLOCK, 0),
	EMERALD_BLOCK(new ItemStack(Material.EMERALD_BLOCK), "Emerald Block", Material.EMERALD_BLOCK, 0),
	RED_BLOCK(new ItemStack(Material.STAINED_CLAY, 1, (short) 0, (byte) 14), "Red", Material.STAINED_CLAY, 14);

	private final Class<? extends DisguiseBase> _clazz;
	private final ItemStack _helmet;
	private final String _name;
	private final String[] _description;
	private final int _cost;
	private final Material _material;
	private final byte _materialData;

	BalloonType(Class<? extends DisguiseBase> clazz, String name, Material material, int materialData)
	{
		this(clazz, null, name, null, CostConstants.FOUND_IN_TREASURE_CHESTS, material, materialData);
	}

	BalloonType(Class<? extends DisguiseBase> clazz, String name, String[] description, int cost, Material material, int materialData)
	{
		this(clazz, null, name, description, cost, material, materialData);
	}

	BalloonType(ItemStack helmet, String name, Material material, int materialData)
	{
		this(null, helmet, name, null, CostConstants.FOUND_IN_TREASURE_CHESTS, material, materialData);
	}

	BalloonType(ItemStack helmet, String name, String[] description, int cost, Material material, int materialData)
	{
		this(null, helmet, name, description, cost, material, materialData);
	}

	BalloonType(Class<? extends DisguiseBase> clazz, ItemStack helmet, String name, String[] description, int cost, Material material, int materialData)
	{
		_clazz = clazz;
		_helmet = helmet;
		_name = name;
		_description = description == null ? UtilText.splitLineToArray(C.cGray + "A floating " + F.name(getName() + " Balloon") + " that appears above your head!", LineFormat.LORE) : description;
		_cost = cost;
		_material = material;
		_materialData = (byte) materialData;
	}

	public Class<? extends DisguiseBase> getClazz()
	{
		return _clazz;
	}

	public ItemStack getHelmet()
	{
		return _helmet;
	}

	public String getName()
	{
		return _name;
	}

	@Override
	public String[] getDescription()
	{
		return _description;
	}

	public int getCost()
	{
		return _cost;
	}

	@Override
	public Material getDisplayMaterial()
	{
		return _material;
	}

	@Override
	public byte getDisplayData()
	{
		return _materialData;
	}
}
