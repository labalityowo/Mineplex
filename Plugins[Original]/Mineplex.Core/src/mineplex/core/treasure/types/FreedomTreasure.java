package mineplex.core.treasure.types;

import mineplex.core.gadget.gadgets.arrowtrail.ArrowTrailFreedom;
import mineplex.core.gadget.gadgets.arrowtrail.ArrowTrailRedWhite;
import mineplex.core.gadget.gadgets.death.DeathFreedom;
import mineplex.core.gadget.gadgets.death.DeathMapleLeaf;
import mineplex.core.gadget.gadgets.doublejump.DoubleJumpFreedom;
import mineplex.core.gadget.gadgets.doublejump.DoubleJumpMaple;
import mineplex.core.gadget.gadgets.flag.FlagType;
import mineplex.core.gadget.gadgets.hat.HatType;
import mineplex.core.gadget.gadgets.morph.MorphUncleSam;
import mineplex.core.gadget.gadgets.mount.types.MountFreedomHorse;
import mineplex.core.gadget.gadgets.particle.freedom.ParticleAuraNiceness;
import mineplex.core.gadget.gadgets.particle.freedom.ParticleCanadian;
import mineplex.core.gadget.gadgets.particle.freedom.ParticleFreedom;
import mineplex.core.gadget.gadgets.particle.freedom.ParticleFreedomFireworks;
import mineplex.core.gadget.gadgets.particle.freedom.ParticleStarSpangled;
import mineplex.core.reward.RewardType;
import mineplex.core.treasure.animation.animations.FreedomChestAnimation;
import mineplex.core.treasure.reward.RewardRarity;

public class FreedomTreasure extends Treasure
{

	public FreedomTreasure()
	{
		super(TreasureType.FREEDOM);

		setAnimation(treasureLocation -> new FreedomChestAnimation(this, treasureLocation));
		setRewards(RewardType.FREEDOM_CHEST);
		setRewardsPerChest(1);
	}

	@Override
	protected void addUncommon(RewardRarity rarity)
	{
		addGadgetReward(getGadget(ArrowTrailRedWhite.class), rarity, 150);
		addGadgetReward(getGadget(ArrowTrailFreedom.class), rarity, 150);
	}

	@Override
	protected void addRare(RewardRarity rarity)
	{
		addHatReward(HatType.UNCLE_SAM, rarity, 100);
		addHatReward(HatType.AMERICA, rarity, 120);
		addHatReward(HatType.CANADA, rarity, 120);
		addGadgetReward(getGadget(DoubleJumpFreedom.class), rarity, 50);
		addGadgetReward(getGadget(DoubleJumpMaple.class), rarity, 50);
		addGadgetReward(getGadget(DeathFreedom.class), rarity, 75);
		addGadgetReward(getGadget(DeathMapleLeaf.class), rarity, 75);

	}

	@Override
	protected void addLegendary(RewardRarity rarity)
	{
		addGadgetReward(getGadget(MountFreedomHorse.class), rarity, 1);
		addGadgetReward(getGadget(MorphUncleSam.class), rarity, 5);
		addGadgetReward(getGadget(ParticleFreedom.class), rarity, 50);
		addGadgetReward(getGadget(ParticleFreedomFireworks.class), rarity, 95);
		addGadgetReward(getGadget(ParticleAuraNiceness.class), rarity, 40);
		addGadgetReward(getGadget(ParticleCanadian.class), rarity, 10);
		addGadgetReward(getGadget(ParticleStarSpangled.class), rarity, 10);
		addFlagReward(FlagType.CANADA, rarity, 35);
		addFlagReward(FlagType.USA, rarity, 35);
	}

	@Override
	protected void addMythical(RewardRarity rarity)
	{
	}
}
