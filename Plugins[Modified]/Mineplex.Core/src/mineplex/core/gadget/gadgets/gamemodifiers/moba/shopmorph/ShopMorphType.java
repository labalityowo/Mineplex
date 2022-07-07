package mineplex.core.gadget.gadgets.gamemodifiers.moba.shopmorph;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import mineplex.core.common.skin.SkinData;
import mineplex.core.disguise.disguises.DisguiseBlaze;
import mineplex.core.disguise.disguises.DisguiseCow;
import mineplex.core.disguise.disguises.DisguiseCreeper;
import mineplex.core.disguise.disguises.DisguiseEnderman;
import mineplex.core.disguise.disguises.DisguiseGuardian;
import mineplex.core.disguise.disguises.DisguiseHorse;
import mineplex.core.disguise.disguises.DisguiseIronGolem;
import mineplex.core.disguise.disguises.DisguiseLiving;
import mineplex.core.disguise.disguises.DisguiseMagmaCube;
import mineplex.core.disguise.disguises.DisguiseMooshroom;
import mineplex.core.disguise.disguises.DisguisePig;
import mineplex.core.disguise.disguises.DisguisePigZombie;
import mineplex.core.disguise.disguises.DisguisePlayer;
import mineplex.core.disguise.disguises.DisguiseSheep;
import mineplex.core.disguise.disguises.DisguiseSkeleton;
import mineplex.core.disguise.disguises.DisguiseSlime;
import mineplex.core.disguise.disguises.DisguiseSnowman;
import mineplex.core.disguise.disguises.DisguiseSpider;
import mineplex.core.disguise.disguises.DisguiseWitch;
import mineplex.core.disguise.disguises.DisguiseZombie;
import mineplex.core.treasure.reward.RewardRarity;

public enum ShopMorphType
{

	SHEEP("Sheep", Material.SHEARS, DisguiseSheep.class, RewardRarity.UNCOMMON),
	COW("Cow", Material.LEATHER, DisguiseCow.class, RewardRarity.UNCOMMON),
	MOOSHROOM("Mooshroom", Material.RED_MUSHROOM, DisguiseMooshroom.class, RewardRarity.UNCOMMON),
	PIG("Pig", Material.GRILLED_PORK, DisguisePig.class, RewardRarity.UNCOMMON),
	HORSE("Horse", Material.SADDLE, DisguiseHorse.class, RewardRarity.UNCOMMON),
	SNOW_GOLEM("Snowman", Material.SNOW_BALL, DisguiseSnowman.class, RewardRarity.UNCOMMON),
	IRON_GOLEM("Iron Golem", Material.IRON_BLOCK, DisguiseIronGolem.class, RewardRarity.UNCOMMON),
	ZOMBIE("Zombie", Material.ROTTEN_FLESH, DisguiseZombie.class, RewardRarity.UNCOMMON),
	SKELETON("Skeleton", Material.BONE, DisguiseSkeleton.class, RewardRarity.UNCOMMON),
	CREEPER("Creeper", Material.SULPHUR, DisguiseCreeper.class, RewardRarity.UNCOMMON),
	SPIDER("Spider", Material.SPIDER_EYE, DisguiseSpider.class, RewardRarity.UNCOMMON),
	SLIME("Slime", Material.SLIME_BALL, DisguiseSlime.class, RewardRarity.UNCOMMON),
	MAGMA_SLIME("Magma Slime", Material.MAGMA_CREAM, DisguiseMagmaCube.class, RewardRarity.UNCOMMON),
	WITHER_SKELETON("Wither Skeleton", Material.SKULL_ITEM, 1, DisguiseSkeleton.class, RewardRarity.UNCOMMON),
	PIG_ZOMBIE("Pig Zombie", Material.GOLD_SWORD, DisguisePigZombie.class, RewardRarity.UNCOMMON),
	WITCH("Witch", Material.CAULDRON_ITEM, DisguiseWitch.class, RewardRarity.UNCOMMON),
	BLAZE("Blaze", Material.BLAZE_POWDER, DisguiseBlaze.class, RewardRarity.UNCOMMON),
	GUARDIAN("Guardian", Material.PRISMARINE_SHARD, DisguiseGuardian.class, RewardRarity.UNCOMMON),
	ENDERMAN("Enderman", Material.ENDER_PEARL, DisguiseEnderman.class, RewardRarity.UNCOMMON),

	SANTA("Santa", Material.WOOL, 14, SkinData.SANTA, RewardRarity.RARE),
	BOB_ROSS("Bob Ross", Material.PAINTING, SkinData.BOB_ROSS, RewardRarity.RARE),
	REVOLUTIONARY("Freedom Fighter", Material.CHAINMAIL_CHESTPLATE, SkinData.REVOLUTIONARY, RewardRarity.RARE)

	;

	private final String _name;
	private final Material _material;
	private final byte _materialData;
	private final Class<? extends DisguiseLiving> _clazz;
	private final SkinData _skinData;
	private final RewardRarity _rarity;

	ShopMorphType(String name, Material material, Class<? extends DisguiseLiving> clazz, RewardRarity rarity)
	{
		this(name, material, 0, clazz, null, rarity);
	}

	ShopMorphType(String name, Material material, int materialData, Class<? extends DisguiseLiving> clazz, RewardRarity rarity)
	{
		this(name, material, materialData, clazz, null, rarity);
	}

	ShopMorphType(String name, Material material, SkinData skinData, RewardRarity rarity)
	{
		this(name, material, 0, DisguisePlayer.class, skinData, rarity);
	}

	ShopMorphType(String name, Material material, int materialData, SkinData skinData, RewardRarity rarity)
	{
		this(name, material, materialData, DisguisePlayer.class, skinData, rarity);
	}

	ShopMorphType(String name, Material material, int materialData, Class<? extends DisguiseLiving> clazz, SkinData skinData, RewardRarity rarity)
	{
		_name = name;
		_material = material;
		_materialData = (byte) materialData;
		_clazz = clazz;
		_skinData = skinData;
		_rarity = rarity;
	}

	public DisguiseLiving createInstance(LivingEntity entity)
	{
		try
		{
			return _clazz.getConstructor(Entity.class).newInstance(entity);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return null;
	}

	public String getName()
	{
		return _name + " Shop Skin";
	}

	public Material getMaterial()
	{
		return _material;
	}

	public byte getMaterialData()
	{
		return _materialData;
	}

	public SkinData getSkinData()
	{
		return _skinData;
	}

	public RewardRarity getRarity()
	{
		return _rarity;
	}
}
