package mineplex.core.reward.rewards;

import mineplex.core.treasure.reward.RewardRarity;
import mineplex.core.treasure.types.Treasure;
import mineplex.core.treasure.types.TreasureType;

public class ChestReward extends InventoryReward
{

	public ChestReward(TreasureType type, RewardRarity rarity, int shardValue)
	{
		this(type, 1, rarity, shardValue);
	}

	public ChestReward(TreasureType type, int amount, RewardRarity rarity, int shardValue)
	{
		this(type, amount, amount, rarity, shardValue);
	}

	public ChestReward(TreasureType type, int min, int max, RewardRarity rarity, int shardValue)
	{
		super(type.getName(), type.getItemName(), "Treasure Chest", min, max, type.getItemStack(), rarity, shardValue);
	}
}
