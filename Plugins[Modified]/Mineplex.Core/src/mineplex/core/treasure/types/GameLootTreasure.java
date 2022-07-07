package mineplex.core.treasure.types;

import org.bukkit.entity.Player;

import mineplex.core.reward.Reward;
import mineplex.core.reward.RewardType;

public class GameLootTreasure extends NormalTreasure
{

	public GameLootTreasure()
	{
		super(TreasureType.GAME_LOOT);

		setRewardsPerChest(1);
		setRewards(RewardType.GAME_LOOT);
		allowDuplicates();
		enabledByDefault();
	}

	public Reward nextReward(Player player)
	{
		return Treasure.getRewardManager().getRewards(player, this).get(0);
	}
}
