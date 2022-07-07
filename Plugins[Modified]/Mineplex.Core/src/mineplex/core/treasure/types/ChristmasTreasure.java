package mineplex.core.treasure.types;

import mineplex.core.gadget.gadgets.arrowtrail.ArrowTrailConfetti;
import mineplex.core.gadget.gadgets.arrowtrail.ArrowTrailFrostLord;
import mineplex.core.gadget.gadgets.death.DeathCandyCane;
import mineplex.core.gadget.gadgets.death.DeathFrostLord;
import mineplex.core.gadget.gadgets.doublejump.DoubleJumpFirecracker;
import mineplex.core.gadget.gadgets.doublejump.DoubleJumpFrostLord;
import mineplex.core.gadget.gadgets.hat.HatType;
import mineplex.core.gadget.gadgets.item.ItemCoal;
import mineplex.core.gadget.gadgets.item.ItemFreezeCannon;
import mineplex.core.gadget.gadgets.item.ItemPartyPopper;
import mineplex.core.gadget.gadgets.item.ItemSnowball;
import mineplex.core.gadget.gadgets.item.ItemTNT;
import mineplex.core.gadget.gadgets.morph.MorphSnowman;
import mineplex.core.gadget.gadgets.mount.types.MountBabyReindeer;
import mineplex.core.gadget.gadgets.particle.ParticleCandyCane;
import mineplex.core.gadget.gadgets.particle.ParticleFrostLord;
import mineplex.core.reward.RewardType;
import mineplex.core.treasure.animation.animations.ChristmasChestAnimation;
import mineplex.core.treasure.reward.RewardRarity;

public class ChristmasTreasure extends Treasure
{

	public ChristmasTreasure()
	{
		super(TreasureType.CHRISTMAS);

		setAnimation(treasureLocation -> new ChristmasChestAnimation(this, treasureLocation));
		setRewards(RewardType.WINTER_CHEST);
		setRewardsPerChest(1);
	}

	@Override
	protected void addCommon(RewardRarity rarity)
	{
		addGadgetReward(getGadget(ItemCoal.class), rarity, 10, 0, 50, 100);
		addGadgetReward(getGadget(ItemSnowball.class), rarity, 10, 0, 5, 20);
		addGadgetReward(getGadget(ItemPartyPopper.class), rarity, 10, 0, 5, 10);
		addGadgetReward(getGadget(ItemFreezeCannon.class), rarity, 10, 0, 5, 10);
		addGadgetReward(getGadget(ItemTNT.class), rarity, 10, 0, 5, 10);
	}

	@Override
	protected void addUncommon(RewardRarity rarity)
	{
		addHatReward(HatType.PRESENT, rarity, 5);
		addHatReward(HatType.SNOWMAN, rarity, 5);
	}

	@Override
	protected void addRare(RewardRarity rarity)
	{
		addGadgetReward(getGadget(ArrowTrailConfetti.class), rarity, 5);
		addGadgetReward(getGadget(DeathCandyCane.class), rarity, 5);
		addGadgetReward(getGadget(DoubleJumpFirecracker.class), rarity, 5);
		addGadgetReward(getGadget(ParticleCandyCane.class), rarity, 5);
	}

	@Override
	protected void addLegendary(RewardRarity rarity)
	{
		addGadgetReward(getGadget(ArrowTrailFrostLord.class), rarity, 5);
		addGadgetReward(getGadget(DeathFrostLord.class), rarity, 5);
		addGadgetReward(getGadget(DoubleJumpFrostLord.class), rarity, 5);
		addGadgetReward(getGadget(ParticleFrostLord.class), rarity, 5);
		addHatReward(HatType.GRINCH, rarity, 5);
		addGadgetReward(getGadget(MorphSnowman.class), rarity, 5);
		addGadgetReward(getGadget(MountBabyReindeer.class), rarity, 5);
	}

	@Override
	protected void addMythical(RewardRarity rarity)
	{
	}
}
