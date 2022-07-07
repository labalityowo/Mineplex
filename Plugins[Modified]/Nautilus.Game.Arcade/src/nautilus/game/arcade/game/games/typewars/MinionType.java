package nautilus.game.arcade.game.games.typewars;

import mineplex.core.disguise.disguises.DisguiseBase;
import mineplex.core.disguise.disguises.DisguiseChicken;
import mineplex.core.disguise.disguises.DisguiseCow;
import mineplex.core.disguise.disguises.DisguiseCreeper;
import mineplex.core.disguise.disguises.DisguiseEnderman;
import mineplex.core.disguise.disguises.DisguiseHorse;
import mineplex.core.disguise.disguises.DisguiseIronGolem;
import mineplex.core.disguise.disguises.DisguiseMagmaCube;
import mineplex.core.disguise.disguises.DisguisePig;
import mineplex.core.disguise.disguises.DisguiseSkeleton;
import mineplex.core.disguise.disguises.DisguiseSlime;
import mineplex.core.disguise.disguises.DisguiseSpider;
import mineplex.core.disguise.disguises.DisguiseWither;
import mineplex.core.disguise.disguises.DisguiseWolf;
import mineplex.core.disguise.disguises.DisguiseZombie;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;

@SuppressWarnings("unchecked")
public enum MinionType
{

	CHICKEN(10, MinionSize.EASY, EntityType.CHICKEN, 3, 3, (float) (0.7 + (0.1*7)), 1, -0.5D, Material.EGG, DisguiseChicken.class),
	PIG(10, MinionSize.EASY, EntityType.PIG, 3, 6, (float) (0.7 + (0.1*7)), 1, -0.5D, Material.PORK, DisguisePig.class),
	COW(10, MinionSize.EASY, EntityType.COW, 6, 9, (float) (0.7 + (0.1*7)), 1, -0.5D, Material.COOKED_BEEF, DisguiseCow.class),
	
	ZOMBIE(10, MinionSize.MEDIUM, EntityType.ZOMBIE, 9, 12, (float) (0.7 + (0.1*2)), 2, 0D, Material.ROTTEN_FLESH, DisguiseZombie.class),
	SPIDER(10, MinionSize.MEDIUM, EntityType.SPIDER, 6, 9, (float) (0.7 + (0.1*5)), 2, 0D, Material.SPIDER_EYE, DisguiseSpider.class),
	WOLF(10, MinionSize.MEDIUM, EntityType.WOLF, 3, 6, (float) (0.7 + (0.1*8)), 2, -1D, Material.COOKED_BEEF, DisguiseWolf.class),
	
	IRON_GOLEM(10, MinionSize.HARD, EntityType.IRON_GOLEM, 10, 13,(float) (0.7 + (0.1*3)), 3, 0.5D, Material.IRON_INGOT, DisguiseIronGolem.class),
	HORSE(10, MinionSize.HARD, EntityType.HORSE, 6, 9, (float) (0.7 + (0.1*10)), 3, 0D, Material.APPLE, DisguiseHorse.class),
	ENDERMAN(10, MinionSize.HARD, EntityType.ENDERMAN, 9, 12, (float) (0.7 + (0.1*4)), 3, 1D, Material.ENDER_STONE, DisguiseEnderman.class),
	
	WITHER(1, MinionSize.BOSS, EntityType.WITHER, 5, 5, (float) (0.7 + (0.1*2)), 1, 2D, Material.NETHER_STAR, DisguiseWither.class),
	SLIME(1, MinionSize.BOSS, EntityType.SLIME, 5, 5, (float) (0.7 + (0.1*2)), 1, 3D, Material.SLIME_BALL, DisguiseSlime.class),
	
	SPIDER_JOKEY(1, MinionSize.FREAK, EntityType.SPIDER, 10, 13, (float) (0.7 + (0.1*7)), 10, 1D, Material.APPLE, DisguiseSpider.class, DisguiseSkeleton.class),
	CHICKEN_JOKEY(1, MinionSize.FREAK, EntityType.CHICKEN, 10, 13, (float) (0.7 + (0.1*7)), 10, 1D, Material.APPLE, DisguiseChicken.class, DisguiseZombie.class);
	
	private Class<? extends DisguiseBase>[] _disguiseClasses;
	
	private EntityType _type;
	private int _minName;
	private int _maxName;
	private float _walkSpeed;
	private int _money;
	
	private MinionSize _size;
	
	private double _tagHight;
	private Material _displayItem;
	
	private int _chance;
	
	private MinionType(int chance, MinionSize size, EntityType type, int minName, int maxName, float walkSpeed, int money, double tagHight, Material displayItem,Class<? extends DisguiseBase>... disguiseClasses)
	{
		_disguiseClasses = disguiseClasses;
		_type = type;
		_minName = minName;
		_maxName = maxName;
		_walkSpeed = walkSpeed; 
		_money = money;
		_displayItem = displayItem;
		_tagHight = tagHight;
		_chance = chance;
		_size = size;
	}
	
	public EntityType getType()
	{
		return _type;
	}

	public int getMinName()
	{
		return _minName;
	}

	public int getMaxName()
	{
		return _maxName;
	}

	public float getWalkSpeed()
	{
		return _walkSpeed;
	}

	public int getMoney()
	{
		return _money;
	}
	
	public Material getDisplayItem()
	{
		return _displayItem;
	}
	
	public Class<? extends DisguiseBase>[] getDisguiseClasses()
	{
		return _disguiseClasses;	
	}
	
	public double getTagHight()
	{
		return _tagHight;
	}
	
	public int getChance()
	{
		return _chance;
	}
	
	public MinionSize getSize()
	{
		return _size;
	}
	
}
