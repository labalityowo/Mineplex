package mineplex.core.treasure.types;

import mineplex.core.gadget.gadgets.arrowtrail.*;
import mineplex.core.gadget.gadgets.balloons.BalloonType;
import mineplex.core.gadget.gadgets.death.*;
import mineplex.core.gadget.gadgets.doublejump.*;
import mineplex.core.gadget.gadgets.item.*;
import mineplex.core.gadget.gadgets.kitselector.HaloKitSelector;
import mineplex.core.gadget.gadgets.kitselector.RainbowDanceKitSelector;
import mineplex.core.gadget.gadgets.kitselector.ShimmeringRingKitSelector;
import mineplex.core.gadget.gadgets.kitselector.SingleParticleKitSelector;
import mineplex.core.gadget.gadgets.morph.*;
import mineplex.core.gadget.gadgets.mount.types.*;
import mineplex.core.gadget.gadgets.outfit.ravesuit.OutfitRaveSuitBoots;
import mineplex.core.gadget.gadgets.outfit.ravesuit.OutfitRaveSuitChestplate;
import mineplex.core.gadget.gadgets.outfit.ravesuit.OutfitRaveSuitHelmet;
import mineplex.core.gadget.gadgets.outfit.ravesuit.OutfitRaveSuitLeggings;
import mineplex.core.gadget.gadgets.outfit.spacesuit.OutfitSpaceSuitBoots;
import mineplex.core.gadget.gadgets.outfit.spacesuit.OutfitSpaceSuitChestplate;
import mineplex.core.gadget.gadgets.outfit.spacesuit.OutfitSpaceSuitHelmet;
import mineplex.core.gadget.gadgets.outfit.spacesuit.OutfitSpaceSuitLeggings;
import mineplex.core.gadget.gadgets.particle.*;
import mineplex.core.gadget.gadgets.taunts.EmojiTaunt;
import mineplex.core.gadget.gadgets.weaponname.WeaponNameType;
import mineplex.core.gadget.util.CostConstants;
import mineplex.core.pet.PetType;
import mineplex.core.treasure.reward.RewardRarity;

public class NormalTreasure extends Treasure
{

	NormalTreasure(TreasureType treasureType)
	{
		super(treasureType);
	}

	@Override
	protected void addCommon(RewardRarity rarity)
	{
		// Gadgets
		addGadgetReward(getGadget(ItemBatGun.class), rarity, 10, 4, 10);
		addGadgetReward(getGadget(ItemEtherealPearl.class), rarity, 10, 6, 15);
		addGadgetReward(getGadget(ItemFirework.class), rarity, 10, 10, 25);
		addGadgetReward(getGadget(ItemFleshHook.class), rarity, 10, 8, 20);
		addGadgetReward(getGadget(ItemMelonLauncher.class), rarity, 10, 10, 25);
		addGadgetReward(getGadget(ItemPaintballGun.class), rarity, 10, 20, 50);
		addGadgetReward(getGadget(ItemTNT.class), rarity, 10, 4, 10);
	}

	@Override
	protected void addUncommon(RewardRarity rarity)
	{
		// Gadgets
		addGadgetReward(getGadget(ItemBatGun.class), rarity, 250, 20, 40);
		addGadgetReward(getGadget(ItemEtherealPearl.class), rarity, 250, 30, 60);
		addGadgetReward(getGadget(ItemFirework.class), rarity, 250, 25, 50);
		addGadgetReward(getGadget(ItemFleshHook.class), rarity, 250, 40, 80);
		addGadgetReward(getGadget(ItemMelonLauncher.class), rarity, 250, 25, 50);
		addGadgetReward(getGadget(ItemPaintballGun.class), rarity, 250, 100, 200);
		addGadgetReward(getGadget(ItemTNT.class), rarity, 250, 20, 40);

		// Pets
		addPetReward(PetType.CHICKEN, rarity, 143);
		addPetReward(PetType.COW, rarity, 500);
		addPetReward(PetType.MUSHROOM_COW, rarity, 200);
		addPetReward(PetType.OCELOT, rarity, 167);
		addPetReward(PetType.PIG, rarity, 200);
		addPetReward(PetType.SHEEP, rarity, 333);
		addPetReward(PetType.WOLF, rarity, 125);

		// Music Discs
		addMusicReward("Blocks", rarity, 25);
		addMusicReward("Cat", rarity, 25);
		addMusicReward("Chirp", rarity, 25);
		addMusicReward("Far", rarity, 25);
		addMusicReward("Mall", rarity, 25);
		addMusicReward("Mellohi", rarity, 25);
		addMusicReward("Stal", rarity, 25);
		addMusicReward("Strad", rarity, 25);
		addMusicReward("Wait", rarity, 25);
		addMusicReward("Ward", rarity, 25);

		// Balloons
		addBalloonReward(BalloonType.BABY_COW, rarity, 10, 100);
		addBalloonReward(BalloonType.BABY_PIG, rarity, 10, 100);
		addBalloonReward(BalloonType.BABY_SHEEP, rarity, 15, 100);
	}

	@Override
	protected void addRare(RewardRarity rarity)
	{
		//Morphs
		addGadgetReward(getGadget(MorphVillager.class), rarity, 83);
		addGadgetReward(getGadget(MorphCow.class), rarity, 167);
		addGadgetReward(getGadget(MorphChicken.class), rarity, 50);
		addGadgetReward(getGadget(MorphEnderman.class), rarity, 33);
		addGadgetReward(getGadget(MorphSkeleton.class), rarity, 40);
		addGadgetReward(getGadget(MorphWolf.class), rarity, 25);

		//Mounts
		addGadgetReward(getGadget(MountFrost.class), rarity, 50);
		addGadgetReward(getGadget(MountSlime.class), rarity, 67);
		addGadgetReward(getGadget(MountCart.class), rarity, 100);
		addGadgetReward(getGadget(MountMule.class), rarity, 200);

		// Outfit Rave
		addGadgetReward(getGadget(OutfitRaveSuitHelmet.class), rarity, 30);
		addGadgetReward(getGadget(OutfitRaveSuitChestplate.class), rarity, 30);
		addGadgetReward(getGadget(OutfitRaveSuitLeggings.class), rarity, 30);
		addGadgetReward(getGadget(OutfitRaveSuitBoots.class), rarity, 30);
		// Outfit Space
		addGadgetReward(getGadget(OutfitSpaceSuitHelmet.class), rarity, 50);
		addGadgetReward(getGadget(OutfitSpaceSuitChestplate.class), rarity, 50);
		addGadgetReward(getGadget(OutfitSpaceSuitLeggings.class), rarity, 50);
		addGadgetReward(getGadget(OutfitSpaceSuitBoots.class), rarity, 50);

		// Arrow Trails
		addGadgetReward(getGadget(ArrowTrailConfetti.class), rarity, 27);
		addGadgetReward(getGadget(ArrowTrailBlood.class), rarity, 50);
		addGadgetReward(getGadget(ArrowTrailEmerald.class), rarity, 25);
		addGadgetReward(getGadget(ArrowTrailMusic.class), rarity, 27);
		addGadgetReward(getGadget(ArrowTrailStorm.class), rarity, 30);
		addGadgetReward(getGadget(ArrowTrailShadow.class), rarity, 15);
		addGadgetReward(getGadget(ArrowTrailBalance.class), rarity, 16);

		// Double Jumps
		addGadgetReward(getGadget(DoubleJumpFirecracker.class), rarity, 33);
		addGadgetReward(getGadget(DoubleJumpEmerald.class), rarity, 25);
		addGadgetReward(getGadget(DoubleJumpShadow.class), rarity, 15);
		addGadgetReward(getGadget(DoubleJumpStorm.class), rarity, 30);
		addGadgetReward(getGadget(DoubleJumpBlood.class), rarity, 50);
		addGadgetReward(getGadget(DoubleJumpMusic.class), rarity, 20);
		addGadgetReward(getGadget(DoubleJumpBalance.class), rarity, 20);

		// Death Effects
		addGadgetReward(getGadget(DeathPinataBurst.class), rarity, 27);
		addGadgetReward(getGadget(DeathEmerald.class), rarity, 25);
		addGadgetReward(getGadget(DeathShadow.class), rarity, 15);
		addGadgetReward(getGadget(DeathStorm.class), rarity, 30);
		addGadgetReward(getGadget(DeathBlood.class), rarity, 50);
		addGadgetReward(getGadget(DeathMusic.class), rarity, 20);
		addGadgetReward(getGadget(DeathBalance.class), rarity, 20);

		// Particles
		addGadgetReward(getGadget(ParticlePartyTime.class), rarity, 12);

		// Titles
		addTitleReward("shrug", rarity, 10, 500);
		addTitleReward("tableflip", rarity, 10, 500);
		addTitleReward("tablerespecter", rarity, 15, 500);
		addTitleReward("tableflip-disgusted", rarity, 15, 500);
		addTitleReward("tableflip-enraged", rarity, 15, 500);
		addTitleReward("tableflip-riot", rarity, 10, 500);
		addTitleReward("teddy-bear", rarity, 10, 500);
		addTitleReward("disgust", rarity, 10, 500);
		addTitleReward("old-man", rarity, 5, 500);
		addTitleReward("jake", rarity, 5, 500);
		addTitleReward("finn", rarity, 5, 500);
		addTitleReward("finn-and-jake", rarity, 5, 500);
		addTitleReward("boxer", rarity, 5, 500);
		addTitleReward("zoidberg", rarity, 5, 500);

		// Balloons
		addBalloonReward(BalloonType.BABY_ZOMBIE, rarity, 25, 500);
		addBalloonReward(BalloonType.BABY_MUSHROOM, rarity, 50, 500);
		addBalloonReward(BalloonType.BABY_OCELOT, rarity, 50, 500);
		addBalloonReward(BalloonType.BABY_WOLF, rarity, 75, 500);
		addBalloonReward(BalloonType.BABY_VILLAGER, rarity, 25, 500);
		addBalloonReward(BalloonType.BABY_SLIME, rarity, 25, 500);
		addBalloonReward(BalloonType.BAT, rarity, 50, 500);
		addBalloonReward(BalloonType.SQUID, rarity, 10);
		addBalloonReward(BalloonType.SILVERFISH, rarity, 30);
		addBalloonReward(BalloonType.GUARDIAN, rarity, 30);
		addBalloonReward(BalloonType.DRAGON_EGG, rarity, 15);
		addBalloonReward(BalloonType.DIAMOND_BLOCK, rarity, 15);
		addBalloonReward(BalloonType.IRON_BLOCK, rarity, 15);
		addBalloonReward(BalloonType.GOLD_BLOCK, rarity, 15);
		addBalloonReward(BalloonType.EMERALD_BLOCK, rarity, 15);
		addBalloonReward(BalloonType.RED_BLOCK, rarity, 15);

		addGadgetReward(getGadget(HaloKitSelector.class), rarity, 20);
		addGadgetReward(getGadget(RainbowDanceKitSelector.class), rarity, 20);
		addGadgetReward(getGadget(ShimmeringRingKitSelector.class), rarity, 20);
		addGadgetReward(getKitSelector(SingleParticleKitSelector.SingleParticleSelectors.FLAMES_OF_FURY), rarity, 20);
		addGadgetReward(getKitSelector(SingleParticleKitSelector.SingleParticleSelectors.EMBER), rarity, 20);
		addGadgetReward(getKitSelector(SingleParticleKitSelector.SingleParticleSelectors.LOVE), rarity, 20);

		for (WeaponNameType type : WeaponNameType.values())
		{
			if (type.getCost() == CostConstants.FOUND_IN_TREASURE_CHESTS)
			{
				addWeaponNameReward(type, rarity, 10);
			}
		}
	}

	@Override
	protected void addLegendary(RewardRarity rarity)
	{
		// Enchant set
		addGadgetReward(getGadget(ArrowTrailEnchant.class), rarity, 10);
		addGadgetReward(getGadget(DeathEnchant.class), rarity, 10);
		addGadgetReward(getGadget(DoubleJumpEnchant.class), rarity, 10);

		// Morphs
		addGadgetReward(getGadget(MorphSlime.class), rarity, 10);
		addGadgetReward(getGadget(MorphBat.class), rarity, 25);
		addGadgetReward(getGadget(MorphBlock.class), rarity, 20);

		// Mounts
		addGadgetReward(getGadget(MountUndead.class), rarity, 33);

		// Particle Trails
		addGadgetReward(getGadget(ParticleWingsAngel.class), rarity, 15);
		addGadgetReward(getGadget(ParticleBlood.class), rarity, 10);
		addGadgetReward(getGadget(ParticleWingsDemons.class), rarity, 15);
		addGadgetReward(getGadget(ParticleEnchant.class), rarity, 25);
		addGadgetReward(getGadget(ParticleFairy.class), rarity, 4);
		addGadgetReward(getGadget(ParticleFireRings.class), rarity, 17);
		addGadgetReward(getGadget(ParticleEmerald.class), rarity, 8);
		addGadgetReward(getGadget(ParticleHeart.class), rarity, 2);
		addGadgetReward(getGadget(ParticleWingsInfernal.class), rarity, 4);
		addGadgetReward(getGadget(ParticleMusic.class), rarity, 15);
		addGadgetReward(getGadget(ParticleWingsPixie.class), rarity, 4);
		addGadgetReward(getGadget(ParticleRain.class), rarity, 13);
		addGadgetReward(getGadget(ParticleFoot.class), rarity, 33);
		addGadgetReward(getGadget(ParticleYinYang.class), rarity, 20);
		addGadgetReward(getGadget(ParticleChickenWings.class), rarity, 15);
		addGadgetReward(getGadget(ParticleFoxTail.class), rarity, 15);
		addGadgetReward(getGadget(ParticleWolfTail.class), rarity, 15);
		addGadgetReward(getGadget(ParticleJetPack.class), rarity, 15);
		addGadgetReward(getGadget(ParticleCape.class), rarity, 15);
		addGadgetReward(getGadget(ParticleLegendaryHero.class), rarity, 10);
		addGadgetReward(getGadget(ParticleRainbowTrail.class), rarity, 10);
		addGadgetReward(getGadget(ParticleDeepSeaSwirl.class), rarity, 10);
		addGadgetReward(getGadget(ParticleInfused.class), rarity, 10);

		// Win Effects
		/*
		addGadgetReward(getGadget(WinEffectBabyChicken.class), rarity, 10);
		addGadgetReward(getGadget(WinEffectLavaTrap.class), rarity, 20);
		addGadgetReward(getGadget(WinEffectLightningStrike.class), rarity, 20);
		addGadgetReward(getGadget(WinEffectMrPunchMan.class), rarity, 33);
		addGadgetReward(getGadget(WinEffectRiseOfTheElderGuardian.class), rarity, 4);
		addGadgetReward(getGadget(WinEffectEarthquake.class), rarity, 10);
		addGadgetReward(getGadget(WinEffectPartyAnimal.class), rarity, 10);

		 */

		// Titles
		addTitleReward("ayyye", rarity, 25);
		addTitleReward("ameno", rarity, 15);
		addTitleReward("magician", rarity, 25);
		addTitleReward("fireball", rarity, 75);
		addTitleReward("magic-missile", rarity, 75);
		addTitleReward("pewpewpew", rarity, 75);
		addTitleReward("stardust", rarity, 60);
		addTitleReward("blow-a-kiss", rarity, 60);
		addTitleReward("cool-guy", rarity, 60);
		addTitleReward("deal-with-it", rarity, 60);
		addTitleReward("party-time", rarity, 55);
		addTitleReward("lalala", rarity, 30);
		addTitleReward("gotta-go", rarity, 30);
		addTitleReward("whaaat", rarity, 30);

		addGadgetReward(getGadget(ItemTrampoline.class), rarity, 10);

		addGadgetReward(getGadget(EmojiTaunt.class), rarity, 25);
	}
}
