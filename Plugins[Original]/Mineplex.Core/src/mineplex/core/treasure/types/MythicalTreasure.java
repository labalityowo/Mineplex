package mineplex.core.treasure.types;

import mineplex.core.gadget.gadgets.item.ItemCoinBomb;
import mineplex.core.gadget.gadgets.kitselector.HaloKitSelector;
import mineplex.core.gadget.gadgets.kitselector.RainbowDanceKitSelector;
import mineplex.core.gadget.gadgets.kitselector.ShimmeringRingKitSelector;
import mineplex.core.gadget.gadgets.kitselector.SingleParticleKitSelector;
import mineplex.core.reward.RewardType;
import mineplex.core.treasure.animation.animations.MythicalChestAnimation;
import mineplex.core.treasure.reward.RewardRarity;

public class MythicalTreasure extends NormalTreasure
{

	public MythicalTreasure()
	{
		super(TreasureType.MYTHICAL);

		setAnimation(treasureLocation -> new MythicalChestAnimation(this, treasureLocation));
		setRewards(RewardType.MYTHICAL_CHEST);
		setRewardsPerChest(4);
		allowDuplicates();
		setPurchasable(10000);
		purchasableFromStore();
		enabledByDefault();
	}

	@Override
	protected void addRare(RewardRarity rarity)
	{
		super.addRare(rarity);

		// Shard bomb
		addGadgetReward(getGadget(ItemCoinBomb.class), rarity, 25, 1, 1);
	}

	@Override
	protected void addMythical(RewardRarity rarity)
	{
		// Rank
		addRankReward(rarity, true, 100);
	}
}
