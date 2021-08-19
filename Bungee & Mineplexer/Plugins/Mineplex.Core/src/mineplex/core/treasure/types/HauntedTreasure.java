package mineplex.core.treasure.types;

import mineplex.core.gadget.gadgets.arrowtrail.ArrowTrailHalloween;
import mineplex.core.gadget.gadgets.doublejump.DoubleJumpHalloween;
import mineplex.core.gadget.gadgets.morph.MorphGrimReaper;
import mineplex.core.gadget.gadgets.mount.types.MountNightmareSteed;
import mineplex.core.gadget.gadgets.wineffect.WinEffectHalloween;
import mineplex.core.gadget.gadgets.hat.HatType;
import mineplex.core.pet.PetType;
import mineplex.core.reward.RewardType;
import mineplex.core.treasure.animation.animations.TrickOrTreatChestAnimation;
import mineplex.core.treasure.reward.RewardRarity;

public class HauntedTreasure extends Treasure
{

	public HauntedTreasure()
	{
		super(TreasureType.HAUNTED);

		setAnimation(treasureLocation -> new TrickOrTreatChestAnimation(this, treasureLocation));
		setRewards(RewardType.HAUNTED_CHEST);
		setRewardsPerChest(1);
	}

	@Override
	protected void addRare(RewardRarity rarity)
	{
		addGadgetReward(getGadget(ArrowTrailHalloween.class), rarity, 10);
		addGadgetReward(getGadget(DoubleJumpHalloween.class), rarity, 10);
		addHatReward(HatType.PUMPKIN, rarity, 100);
	}

	@Override
	protected void addLegendary(RewardRarity rarity)
	{
		addPetReward(PetType.RABBIT, rarity, 100);
		addGadgetReward(getGadget(MorphGrimReaper.class), rarity, 25);
		addGadgetReward(getGadget(WinEffectHalloween.class), rarity, 50);
		addGadgetReward(getGadget(MountNightmareSteed.class), rarity, 60);
	}

	@Override
	protected void addMythical(RewardRarity rarity)
	{
	}
}
