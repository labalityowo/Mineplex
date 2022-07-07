package mineplex.core.treasure.types;

import mineplex.core.gadget.gadgets.arrowtrail.ArrowTrailPresent;
import mineplex.core.gadget.gadgets.death.DeathPresentDanger;
import mineplex.core.gadget.gadgets.doublejump.DoubleJumpPresent;
import mineplex.core.gadget.gadgets.flag.FlagType;
import mineplex.core.gadget.gadgets.morph.MorphFrostGolem;
import mineplex.core.gadget.gadgets.outfit.freezesuit.OutfitFreezeSuitBoots;
import mineplex.core.gadget.gadgets.outfit.freezesuit.OutfitFreezeSuitChestplate;
import mineplex.core.gadget.gadgets.outfit.freezesuit.OutfitFreezeSuitHelmet;
import mineplex.core.gadget.gadgets.outfit.freezesuit.OutfitFreezeSuitLeggings;
import mineplex.core.gadget.gadgets.outfit.reindeer.OutfitReindeerAntlers;
import mineplex.core.gadget.gadgets.outfit.reindeer.OutfitReindeerChest;
import mineplex.core.gadget.gadgets.outfit.reindeer.OutfitReindeerHooves;
import mineplex.core.gadget.gadgets.outfit.reindeer.OutfitReindeerLegs;
import mineplex.core.gadget.gadgets.particle.christmas.ParticleBlizzard;
import mineplex.core.gadget.gadgets.particle.christmas.ParticleChristmasTree;
import mineplex.core.gadget.gadgets.particle.christmas.ParticleFidgetSpinner;
import mineplex.core.gadget.gadgets.particle.christmas.ParticleWingsChristmas;
import mineplex.core.gadget.gadgets.taunts.FrostBreathTaunt;
import mineplex.core.gadget.gadgets.wineffect.WinEffectWinterWarfare;
import mineplex.core.pet.PetType;
import mineplex.core.reward.RewardType;
import mineplex.core.treasure.animation.animations.GingerbreadChestAnimation;
import mineplex.core.treasure.reward.RewardRarity;

public class GingerbreadTreasure extends Treasure
{

	public GingerbreadTreasure()
	{
		super(TreasureType.GINGERBREAD);

		setAnimation(treasureLocation -> new GingerbreadChestAnimation(this, treasureLocation));
		setRewards(RewardType.GINGERBREAD_CHEST);
		setRewardsPerChest(1);
	}

	@Override
	protected void addRare(RewardRarity rarity)
	{
		// Outfits
		addGadgetReward(getGadget(OutfitFreezeSuitHelmet.class), rarity, 50);
		addGadgetReward(getGadget(OutfitFreezeSuitChestplate.class), rarity, 100);
		addGadgetReward(getGadget(OutfitFreezeSuitLeggings.class), rarity, 100);
		addGadgetReward(getGadget(OutfitFreezeSuitBoots.class), rarity, 50);

		addGadgetReward(getGadget(OutfitReindeerAntlers.class), rarity, 50);
		addGadgetReward(getGadget(OutfitReindeerChest.class), rarity, 100);
		addGadgetReward(getGadget(OutfitReindeerLegs.class), rarity, 100);
		addGadgetReward(getGadget(OutfitReindeerHooves.class), rarity, 50);

		// Particle
		addGadgetReward(getGadget(ParticleChristmasTree.class), rarity, 25);

		// Morph
		addGadgetReward(getGadget(MorphFrostGolem.class), rarity, 35);

		// Flags
		addFlagReward(FlagType.MINEPLEX, rarity, 30);
		addFlagReward(FlagType.RUDOLPH, rarity, 30);
		addFlagReward(FlagType.CHRISTMAS_TREE, rarity, 30);
		addFlagReward(FlagType.PRESENT, rarity, 30);
		addFlagReward(FlagType.WREATH, rarity, 30);
		addFlagReward(FlagType.SNOW_FLAKE, rarity, 30);
	}

	@Override
	protected void addLegendary(RewardRarity rarity)
	{
		// Present Set
		addGadgetReward(getGadget(DeathPresentDanger.class), rarity, 25);
		addGadgetReward(getGadget(DoubleJumpPresent.class), rarity, 25);
		addGadgetReward(getGadget(ArrowTrailPresent.class), rarity, 25);

		// Particle
		addGadgetReward(getGadget(ParticleWingsChristmas.class), rarity, 20);
		addGadgetReward(getGadget(ParticleBlizzard.class), rarity, 20);
		addGadgetReward(getGadget(ParticleFidgetSpinner.class), rarity, 20);

		// Pet
		addPetReward(PetType.GINGERBREAD_MAN, rarity, 20);

		// Win Effect
		addGadgetReward(getGadget(WinEffectWinterWarfare.class), rarity, 25);

		// Taunts
		addGadgetReward(getGadget(FrostBreathTaunt.class), rarity, 20);
	}

	@Override
	protected void addMythical(RewardRarity rarity)
	{
	}
}
