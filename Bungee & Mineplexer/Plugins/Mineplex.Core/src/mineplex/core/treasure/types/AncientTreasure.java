package mineplex.core.treasure.types;

import mineplex.core.reward.RewardType;
import mineplex.core.treasure.animation.animations.AncientChestAnimation;

public class AncientTreasure extends NormalTreasure
{

	public AncientTreasure()
	{
		super(TreasureType.ANCIENT);

		setAnimation(treasureLocation -> new AncientChestAnimation(this, treasureLocation));
		setRewards(RewardType.ANCIENT_CHEST);
		setRewardsPerChest(4);
		allowDuplicates();
		setPurchasable(5000);
		purchasableFromStore();
		enabledByDefault();
	}
}
