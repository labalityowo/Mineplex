package mineplex.core.treasure.types;

import mineplex.core.reward.RewardType;
import mineplex.core.treasure.animation.animations.OldChestAnimation;

public class OldTreasure extends NormalTreasure
{

	public OldTreasure()
	{
		super(TreasureType.OLD);

		setAnimation(treasureLocation -> new OldChestAnimation(this, treasureLocation));
		setRewards(RewardType.OLD_CHEST);
		setRewardsPerChest(4);
		allowDuplicates();
		setPurchasable(1000);
		purchasableFromStore();
		enabledByDefault();
	}
}
