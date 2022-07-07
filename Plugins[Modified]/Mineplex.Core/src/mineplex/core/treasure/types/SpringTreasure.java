package mineplex.core.treasure.types;

import mineplex.core.gadget.gadgets.arrowtrail.ArrowTrailSpring;
import mineplex.core.gadget.gadgets.death.DeathSpring;
import mineplex.core.gadget.gadgets.doublejump.DoubleJumpSpring;
import mineplex.core.gadget.gadgets.morph.MorphAwkwardRabbit;
import mineplex.core.gadget.gadgets.particle.spring.ParticleSpringHalo;
import mineplex.core.pet.PetType;
import mineplex.core.reward.RewardType;
import mineplex.core.treasure.animation.animations.SpringChestAnimation;
import mineplex.core.treasure.reward.RewardRarity;

public class SpringTreasure extends Treasure
{

	public SpringTreasure()
	{
		super(TreasureType.SPRING);

		setAnimation(treasureLocation -> new SpringChestAnimation(this, treasureLocation));
		setRewards(RewardType.SPRING);
		setRewardsPerChest(1);
	}

	@Override
	protected void addRare(RewardRarity rarity)
	{
		addGadgetReward(getGadget(ArrowTrailSpring.class), rarity, 100);
		addGadgetReward(getGadget(DeathSpring.class), rarity, 100);
		addGadgetReward(getGadget(DoubleJumpSpring.class), rarity, 100);
	}

	@Override
	protected void addLegendary(RewardRarity rarity)
	{
		addGadgetReward(getGadget(ParticleSpringHalo.class), rarity, 100);
		addGadgetReward(getGadget(MorphAwkwardRabbit.class), rarity, 25);
		addPetReward(PetType.KILLER_BUNNY, rarity, 10);
	}

	@Override
	protected void addMythical(RewardRarity rarity)
	{
		addGadgetReward(getGadget(ParticleSpringHalo.class), rarity, 100);
		addGadgetReward(getGadget(MorphAwkwardRabbit.class), rarity, 25);
		addPetReward(PetType.KILLER_BUNNY, rarity, 50);
	}
}
