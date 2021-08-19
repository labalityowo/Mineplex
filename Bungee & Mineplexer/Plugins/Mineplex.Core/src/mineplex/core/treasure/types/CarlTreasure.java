package mineplex.core.treasure.types;

import org.bukkit.entity.Player;

import mineplex.core.reward.Reward;
import mineplex.core.reward.RewardType;

public class CarlTreasure extends NormalTreasure
{

	public CarlTreasure()
	{
		super(TreasureType.CARL_SPINNER);

		setRewardsPerChest(1);
		allowDuplicates();
		enabledByDefault();
	}

	public Reward nextReward(Player player, boolean filler)
	{
		if (filler)
		{
			setRewards(RewardType.SPINNER_FILLER);
		}
		else
		{
			setRewards(RewardType.SPINNER_REAL);
		}

		return Treasure.getRewardManager().getRewards(player, this).get(0);
	}
}
