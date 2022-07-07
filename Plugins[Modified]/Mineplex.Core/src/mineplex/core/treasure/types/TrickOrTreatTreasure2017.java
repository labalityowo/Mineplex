package mineplex.core.treasure.types;

import mineplex.core.gadget.gadgets.arrowtrail.ArrowTrailHalloween;
import mineplex.core.gadget.gadgets.death.DeathHalloween;
import mineplex.core.gadget.gadgets.doublejump.DoubleJumpHalloween;
import mineplex.core.gadget.gadgets.item.ItemCandy;
import mineplex.core.gadget.gadgets.kitselector.HalloweenKitSelector;
import mineplex.core.gadget.gadgets.morph.MorphGrimReaper;
import mineplex.core.gadget.gadgets.mount.types.MountNightmareSteed;
import mineplex.core.gadget.gadgets.particle.ParticleHalloween;
import mineplex.core.gadget.gadgets.taunts.InfernalTaunt;
import mineplex.core.gadget.gadgets.wineffect.WinEffectHalloween;
import mineplex.core.pet.PetType;
import mineplex.core.reward.RewardType;
import mineplex.core.treasure.animation.animations.TrickOrTreatChestAnimation;
import mineplex.core.treasure.reward.RewardRarity;

public class TrickOrTreatTreasure2017 extends Treasure
{

	public TrickOrTreatTreasure2017()
	{
		super(TreasureType.TRICK_OR_TREAT_2017);

		setAnimation(treasureLocation -> new TrickOrTreatChestAnimation(this, treasureLocation));
		setRewards(RewardType.TRICK_OR_TREAT_CHEST);
		setRewardsPerChest(1);
	}

	@Override
	protected void addUncommon(RewardRarity rarity)
	{
		addGadgetReward(getGadget(ItemCandy.class), rarity, 10, 5, 10);
	}

	@Override
	protected void addRare(RewardRarity rarity)
	{
		addGadgetReward(getGadget(ArrowTrailHalloween.class), rarity, 10);
		addGadgetReward(getGadget(DeathHalloween.class), rarity, 10);
		addGadgetReward(getGadget(DoubleJumpHalloween.class), rarity, 10);
		addGadgetReward(getGadget(HalloweenKitSelector.class), rarity, 10);
		addPetReward(PetType.BLAZE, rarity, 10);
		addPetReward(PetType.ZOMBIE, rarity, 10);
	}

	@Override
	protected void addLegendary(RewardRarity rarity)
	{
		addGadgetReward(getGadget(ParticleHalloween.class), rarity, 10);
		addGadgetReward(getGadget(InfernalTaunt.class), rarity, 10);
		addGadgetReward(getGadget(MorphGrimReaper.class), rarity, 10);
		addGadgetReward(getGadget(WinEffectHalloween.class), rarity, 10);
		addGadgetReward(getGadget(MountNightmareSteed.class), rarity, 10);
	}

	@Override
	protected void addMythical(RewardRarity rarity)
	{
	}
}
