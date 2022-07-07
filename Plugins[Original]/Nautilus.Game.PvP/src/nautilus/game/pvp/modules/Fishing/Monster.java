package nautilus.game.pvp.modules.Fishing;

import org.bukkit.entity.EntityType;

public enum Monster
{
	Silverfish(EntityType.SILVERFISH, 1000, Rarity.Common),
	Slime(EntityType.SLIME, 500, Rarity.Common),
	Zombie(EntityType.ZOMBIE, 200, Rarity.Moderate),
	Skeleton(EntityType.SKELETON, 200, Rarity.Moderate),
	Creeper(EntityType.CREEPER, 50, Rarity.Rare),
	TNT(EntityType.PRIMED_TNT, 25, Rarity.Rare),
	Enderman(EntityType.ENDERMAN, 25, Rarity.Rare),
	Wither(EntityType.WITHER, 0, Rarity.Legendary);

	private EntityType type;
	private int scale;
	private Rarity rarity;

	private Monster(EntityType type, int scale, Rarity rarity) 
	{
		this.scale = scale;
		this.type = type;
		this.rarity = rarity;
	}

	public int GetScale()
	{
		return scale;
	}

	public EntityType GetType()
	{
		return type;
	}

	public Rarity GetRarity()
	{
		return rarity;
	}
}
