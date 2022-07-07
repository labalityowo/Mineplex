package mineplex.core.treasure.types;

import mineplex.core.gadget.gadgets.mount.types.MountStPatricksHorse;
import mineplex.core.gadget.gadgets.taunts.RainbowTaunt;
import mineplex.core.gadget.gadgets.outfit.stpatricks.OutfitStPatricksHat;
import mineplex.core.gadget.gadgets.outfit.stpatricks.OutfitStPatricksChestplate;
import mineplex.core.gadget.gadgets.outfit.stpatricks.OutfitStPatricksLeggings;
import mineplex.core.gadget.gadgets.outfit.stpatricks.OutfitStPatricksBoots;
import mineplex.core.pet.PetType;
import mineplex.core.reward.RewardType;
import mineplex.core.treasure.animation.animations.StPatricksChestAnimation;
import mineplex.core.treasure.reward.RewardRarity;

public class StPatricksTreasure extends Treasure
{

	public StPatricksTreasure()
	{
		super(TreasureType.ST_PATRICKS);

		setAnimation(treasureLocation -> new StPatricksChestAnimation(this, treasureLocation));
		setRewards(RewardType.ST_PATRICKS);
		setRewardsPerChest(1);
	}

	@Override
	protected void addRare(RewardRarity rarity)
	{
		addGadgetReward(getGadget(OutfitStPatricksChestplate.class), rarity, 50);
		addGadgetReward(getGadget(OutfitStPatricksLeggings.class), rarity, 50);
		addGadgetReward(getGadget(OutfitStPatricksBoots.class), rarity, 50);
	}

	@Override
	protected void addLegendary(RewardRarity rarity)
	{
		addGadgetReward(getGadget(OutfitStPatricksHat.class), rarity, 15);
		addGadgetReward(getGadget(RainbowTaunt.class), rarity, 30);
		addGadgetReward(getGadget(MountStPatricksHorse.class), rarity, 30);
		addPetReward(PetType.LEPRECHAUN, rarity, 30);
	}

	@Override
	protected void addMythical(RewardRarity rarity)
	{
	}
}
