package mineplex.core.treasure.types;

import mineplex.core.reward.Reward;
import mineplex.core.reward.RewardType;
import mineplex.core.reward.rewards.InventoryReward;
import mineplex.core.treasure.animation.animations.IlluminatedChestAnimation;

public class IlluminatedTreasure extends NormalTreasure
{

	public IlluminatedTreasure()
	{
		super(TreasureType.ILLUMINATED);

		setAnimation(treasureLocation -> new IlluminatedChestAnimation(this, treasureLocation));
		setRewards(RewardType.ILLUMINATED_CHEST);
		setRewardsPerChest(1);
		setPurchasable(20000);
		purchasableFromStore();
		enabledByDefault();
	}

	@Override
	protected void addReward(Reward reward, int weight)
	{
		// Illuminated chests have everything in the normal pool expect item gadgets.
		// To avoid copying code and making it easy to maintain, we can just ignore all
		// inventory rewards here.
		if (reward instanceof InventoryReward)
		{
			return;
		}

		super.addReward(reward, weight);
	}
}
