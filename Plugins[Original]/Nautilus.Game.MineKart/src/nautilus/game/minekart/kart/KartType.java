package nautilus.game.minekart.kart;

import mineplex.core.common.util.NautHashMap;
import nautilus.game.minekart.item.KartItemType;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;

public enum KartType 
{
	//Light
	Pig("Pig", EntityType.PIG, Material.GRILLED_PORK, Material.GRILLED_PORK,  
			0.9, 6, 5, 3, 
			Sound.PIG_IDLE, Sound.PIG_DEATH, Sound.PIG_WALK, 
			KartItemType.Pig),
	
	Chicken("Chicken", EntityType.CHICKEN, Material.FEATHER, Material.POTATO_ITEM,   
			0.85, 8, 6, 1, 
			Sound.CHICKEN_IDLE, Sound.CHICKEN_HURT, Sound.CHICKEN_WALK, 
			KartItemType.Chicken),

	Wolf("Wolf", EntityType.WOLF, Material.SUGAR, Material.BAKED_POTATO,   
			1.00, 5, 3, 2, 
			Sound.WOLF_BARK, Sound.WOLF_HURT, Sound.WOLF_WALK, 
			KartItemType.Wolf),
	
	//Med
	Sheep("Sheep", EntityType.SHEEP, Material.WHEAT, Material.WHEAT,   
			0.95, 4, 6, 5, 
			Sound.SHEEP_IDLE, Sound.SHEEP_SHEAR, Sound.SHEEP_WALK, 
			KartItemType.Sheep),			
				
	Enderman("Enderman", EntityType.ENDERMAN, Material.FIREBALL, Material.ROTTEN_FLESH, 
			0.90, 6, 5, 4, 
			Sound.ENDERMAN_IDLE, Sound.ENDERMAN_SCREAM, Sound.ENDERMAN_TELEPORT, 
			KartItemType.Enderman),
			
	Spider("Spider", EntityType.SPIDER, Material.STRING, Material.SPIDER_EYE,   
			0.80, 7, 8, 4,  
			Sound.SPIDER_IDLE, Sound.SPIDER_DEATH, Sound.SPIDER_WALK, 
			KartItemType.Spider),
	
	//Heavy
	Cow("Cow", EntityType.COW, Material.MILK_BUCKET, Material.MILK_BUCKET,   
			1.0, 3, 5, 7, 
			Sound.COW_IDLE, Sound.COW_HURT, Sound.COW_WALK, 
			KartItemType.Cow),
			
	Blaze("Blaze", EntityType.BLAZE, Material.BLAZE_ROD, Material.COOKED_BEEF, 
			1.0, 7, 1, 6, 
			Sound.BLAZE_BREATH, Sound.BLAZE_BREATH, Sound.BLAZE_BREATH, 
			KartItemType.Blaze),
	
	Golem("Golem", EntityType.IRON_GOLEM, Material.IRON_INGOT, Material.NETHER_BRICK_ITEM, 
			1.05, 1, 7, 8, 
			Sound.IRONGOLEM_THROW, Sound.IRONGOLEM_HIT, Sound.IRONGOLEM_WALK, 
			KartItemType.Golem);
	
	private static NautHashMap<EntityType, KartType> _kartTypes;
	
	private String _name;
	private EntityType _type;
	private Material _kartAvatar;
	private Material _grayAvatar;
	
	private double _topSpeed;
	private double _acceleration;
	private double _handling;
	private double _stability;
	
	private Sound _soundUse;
	private Sound _soundCrash;
	private Sound _soundEngine;
	
	private KartItemType _kartItem;
	
	KartType(String name, EntityType type, Material kartAvatar, Material grayAvatar, double topSpeed, double acceleration, double handling, double stability, Sound use, Sound crash, Sound engine, KartItemType item)
	{
		_name = name;
		_type = type;
		_kartAvatar = kartAvatar;
		_grayAvatar = grayAvatar;
		
		_topSpeed = topSpeed;
		_acceleration = acceleration;
		_handling = handling;
		_stability = stability;
		
		_soundUse = use;
		_soundCrash = crash;
		_soundEngine = engine;
		
		_kartItem = item;
		
		GetMap().put(type, this);
	}
	
	private static NautHashMap<EntityType, KartType> GetMap()
	{
		if (_kartTypes == null)
			_kartTypes = new NautHashMap<EntityType, KartType>();
			
		return _kartTypes;
	}
	
	public static KartType GetByEntityType(EntityType entityType)
	{
		return _kartTypes.get(entityType);
	}
	
	public String GetName()
	{
		return _name;
	}
	
	public EntityType GetType()
	{
		return _type;
	}
	
	public double GetTopSpeed()
	{
		return _topSpeed;
	}
	
	public double GetAcceleration()
	{
		return 10 + _acceleration;
	}
	
	public double GetHandling()
	{
		return 10 + _handling;
	}
	
	public double GetStability()
	{
		return 10 + _stability;
	}
	
	public Sound GetSoundMain()
	{
		return _soundUse;
	}
	
	public Sound GetSoundCrash()
	{
		return _soundCrash;
	}
	
	public Sound GetSoundEngine()
	{
		return _soundEngine;
	}
	
	public KartItemType GetKartItem() 
	{
		return _kartItem;
	}

	public String[] GetDescription()
	{
		return new String[] {};
	}
	
	public Material GetAvatar()
	{
		return _kartAvatar;
	}
	
	public Material GetAvatarGray()
	{
		return _grayAvatar;
	}
}
