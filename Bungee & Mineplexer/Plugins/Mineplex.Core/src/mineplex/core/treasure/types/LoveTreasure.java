package mineplex.core.treasure.types;

import mineplex.core.gadget.gadgets.morph.MorphLoveDoctor;
import mineplex.core.gadget.gadgets.mount.types.MountLoveTrain;
import mineplex.core.gadget.gadgets.wineffect.WinEffectLoveIsABattlefield;
import mineplex.core.gadget.gadgets.taunts.BlowAKissTaunt;
import mineplex.core.gadget.gadgets.particle.ParticleWingsLove;
import mineplex.core.pet.PetType;
import mineplex.core.reward.RewardType;
import mineplex.core.treasure.animation.animations.LoveChestAnimation;
import mineplex.core.treasure.reward.RewardRarity;

public class LoveTreasure extends Treasure
{

	public LoveTreasure()
	{
		super(TreasureType.LOVE);

		setAnimation(treasureLocation -> new LoveChestAnimation(this, treasureLocation));
		setRewards(RewardType.LOVE_CHEST);
		setRewardsPerChest(1);
	}

	@Override
	protected void addRare(RewardRarity rarity)
	{
		addGadgetReward(getGadget(MountLoveTrain.class), rarity, 30);
		addGadgetReward(getGadget(WinEffectLoveIsABattlefield.class), rarity, 100);
		addPetReward(PetType.CUPID_PET, rarity, 50);
	}

	@Override
	protected void addLegendary(RewardRarity rarity)
	{
		addGadgetReward(getGadget(MorphLoveDoctor.class), rarity, 30);
		addGadgetReward(getGadget(BlowAKissTaunt.class), rarity, 50);
		addGadgetReward(getGadget(ParticleWingsLove.class), rarity, 10);
	}

	@Override
	protected void addMythical(RewardRarity rarity)
	{
	}
}
