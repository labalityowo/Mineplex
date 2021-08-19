package mineplex.core.treasure.types;

import java.util.List;

import mineplex.core.gadget.gadgets.arrowtrail.ArrowTrailBalance;
import mineplex.core.gadget.gadgets.arrowtrail.ArrowTrailBlood;
import mineplex.core.gadget.gadgets.arrowtrail.ArrowTrailCandyCane;
import mineplex.core.gadget.gadgets.arrowtrail.ArrowTrailConfetti;
import mineplex.core.gadget.gadgets.arrowtrail.ArrowTrailCupid;
import mineplex.core.gadget.gadgets.arrowtrail.ArrowTrailEmerald;
import mineplex.core.gadget.gadgets.arrowtrail.ArrowTrailEnchant;
import mineplex.core.gadget.gadgets.arrowtrail.ArrowTrailFreedom;
import mineplex.core.gadget.gadgets.arrowtrail.ArrowTrailFrostLord;
import mineplex.core.gadget.gadgets.arrowtrail.ArrowTrailHalloween;
import mineplex.core.gadget.gadgets.arrowtrail.ArrowTrailMusic;
import mineplex.core.gadget.gadgets.arrowtrail.ArrowTrailRedWhite;
import mineplex.core.gadget.gadgets.arrowtrail.ArrowTrailShadow;
import mineplex.core.gadget.gadgets.arrowtrail.ArrowTrailSpring;
import mineplex.core.gadget.gadgets.arrowtrail.ArrowTrailStorm;
import mineplex.core.gadget.gadgets.balloons.BalloonType;
import mineplex.core.gadget.gadgets.death.DeathBalance;
import mineplex.core.gadget.gadgets.death.DeathBlood;
import mineplex.core.gadget.gadgets.death.DeathCandyCane;
import mineplex.core.gadget.gadgets.death.DeathCupidsBrokenHeart;
import mineplex.core.gadget.gadgets.death.DeathEmerald;
import mineplex.core.gadget.gadgets.death.DeathEnchant;
import mineplex.core.gadget.gadgets.death.DeathFreedom;
import mineplex.core.gadget.gadgets.death.DeathFrostLord;
import mineplex.core.gadget.gadgets.death.DeathMapleLeaf;
import mineplex.core.gadget.gadgets.death.DeathMusic;
import mineplex.core.gadget.gadgets.death.DeathPinataBurst;
import mineplex.core.gadget.gadgets.death.DeathPresentDanger;
import mineplex.core.gadget.gadgets.death.DeathShadow;
import mineplex.core.gadget.gadgets.death.DeathSpring;
import mineplex.core.gadget.gadgets.death.DeathStorm;
import mineplex.core.gadget.gadgets.doublejump.DoubleJumpBalance;
import mineplex.core.gadget.gadgets.doublejump.DoubleJumpBlood;
import mineplex.core.gadget.gadgets.doublejump.DoubleJumpCandyCane;
import mineplex.core.gadget.gadgets.doublejump.DoubleJumpCupidsWings;
import mineplex.core.gadget.gadgets.doublejump.DoubleJumpEmerald;
import mineplex.core.gadget.gadgets.doublejump.DoubleJumpEnchant;
import mineplex.core.gadget.gadgets.doublejump.DoubleJumpFirecracker;
import mineplex.core.gadget.gadgets.doublejump.DoubleJumpFreedom;
import mineplex.core.gadget.gadgets.doublejump.DoubleJumpFrostLord;
import mineplex.core.gadget.gadgets.doublejump.DoubleJumpHalloween;
import mineplex.core.gadget.gadgets.doublejump.DoubleJumpMaple;
import mineplex.core.gadget.gadgets.doublejump.DoubleJumpMusic;
import mineplex.core.gadget.gadgets.doublejump.DoubleJumpShadow;
import mineplex.core.gadget.gadgets.doublejump.DoubleJumpSpring;
import mineplex.core.gadget.gadgets.doublejump.DoubleJumpStorm;
import mineplex.core.gadget.gadgets.flag.FlagType;
import mineplex.core.gadget.gadgets.hat.HatType;
import mineplex.core.gadget.gadgets.item.ItemTrampoline;
import mineplex.core.gadget.gadgets.kitselector.HaloKitSelector;
import mineplex.core.gadget.gadgets.kitselector.RainbowDanceKitSelector;
import mineplex.core.gadget.gadgets.kitselector.ShimmeringRingKitSelector;
import mineplex.core.gadget.gadgets.kitselector.SingleParticleKitSelector.SingleParticleSelectors;
import mineplex.core.gadget.gadgets.morph.MorphAwkwardRabbit;
import mineplex.core.gadget.gadgets.morph.MorphBat;
import mineplex.core.gadget.gadgets.morph.MorphBlock;
import mineplex.core.gadget.gadgets.morph.MorphBunny;
import mineplex.core.gadget.gadgets.morph.MorphChicken;
import mineplex.core.gadget.gadgets.morph.MorphCow;
import mineplex.core.gadget.gadgets.morph.MorphEnderman;
import mineplex.core.gadget.gadgets.morph.MorphGrimReaper;
import mineplex.core.gadget.gadgets.morph.MorphLoveDoctor;
import mineplex.core.gadget.gadgets.morph.MorphPumpkinKing;
import mineplex.core.gadget.gadgets.morph.MorphSkeleton;
import mineplex.core.gadget.gadgets.morph.MorphSlime;
import mineplex.core.gadget.gadgets.morph.MorphSnowman;
import mineplex.core.gadget.gadgets.morph.MorphUncleSam;
import mineplex.core.gadget.gadgets.morph.MorphVillager;
import mineplex.core.gadget.gadgets.morph.MorphWolf;
import mineplex.core.gadget.gadgets.mount.types.MountBabyReindeer;
import mineplex.core.gadget.gadgets.mount.types.MountCake;
import mineplex.core.gadget.gadgets.mount.types.MountCart;
import mineplex.core.gadget.gadgets.mount.types.MountChicken;
import mineplex.core.gadget.gadgets.mount.types.MountFreedomHorse;
import mineplex.core.gadget.gadgets.mount.types.MountFrost;
import mineplex.core.gadget.gadgets.mount.types.MountLoveTrain;
import mineplex.core.gadget.gadgets.mount.types.MountMule;
import mineplex.core.gadget.gadgets.mount.types.MountNightmareSteed;
import mineplex.core.gadget.gadgets.mount.types.MountSlime;
import mineplex.core.gadget.gadgets.mount.types.MountSpider;
import mineplex.core.gadget.gadgets.mount.types.MountStPatricksHorse;
import mineplex.core.gadget.gadgets.mount.types.MountUndead;
import mineplex.core.gadget.gadgets.mount.types.MountValentinesSheep;
import mineplex.core.gadget.gadgets.mount.types.MountZombie;
import mineplex.core.gadget.gadgets.outfit.freezesuit.OutfitFreezeSuitBoots;
import mineplex.core.gadget.gadgets.outfit.freezesuit.OutfitFreezeSuitChestplate;
import mineplex.core.gadget.gadgets.outfit.freezesuit.OutfitFreezeSuitHelmet;
import mineplex.core.gadget.gadgets.outfit.freezesuit.OutfitFreezeSuitLeggings;
import mineplex.core.gadget.gadgets.outfit.ravesuit.OutfitRaveSuitBoots;
import mineplex.core.gadget.gadgets.outfit.ravesuit.OutfitRaveSuitChestplate;
import mineplex.core.gadget.gadgets.outfit.ravesuit.OutfitRaveSuitHelmet;
import mineplex.core.gadget.gadgets.outfit.ravesuit.OutfitRaveSuitLeggings;
import mineplex.core.gadget.gadgets.outfit.spacesuit.OutfitSpaceSuitBoots;
import mineplex.core.gadget.gadgets.outfit.spacesuit.OutfitSpaceSuitChestplate;
import mineplex.core.gadget.gadgets.outfit.spacesuit.OutfitSpaceSuitHelmet;
import mineplex.core.gadget.gadgets.outfit.spacesuit.OutfitSpaceSuitLeggings;
import mineplex.core.gadget.gadgets.outfit.stpatricks.OutfitStPatricksBoots;
import mineplex.core.gadget.gadgets.outfit.stpatricks.OutfitStPatricksChestplate;
import mineplex.core.gadget.gadgets.outfit.stpatricks.OutfitStPatricksHat;
import mineplex.core.gadget.gadgets.outfit.stpatricks.OutfitStPatricksLeggings;
import mineplex.core.gadget.gadgets.particle.ParticleBlood;
import mineplex.core.gadget.gadgets.particle.ParticleCandyCane;
import mineplex.core.gadget.gadgets.particle.ParticleCape;
import mineplex.core.gadget.gadgets.particle.ParticleChickenWings;
import mineplex.core.gadget.gadgets.particle.ParticleCoalFumes;
import mineplex.core.gadget.gadgets.particle.ParticleDeepSeaSwirl;
import mineplex.core.gadget.gadgets.particle.ParticleEmerald;
import mineplex.core.gadget.gadgets.particle.ParticleEnchant;
import mineplex.core.gadget.gadgets.particle.ParticleFairy;
import mineplex.core.gadget.gadgets.particle.ParticleFireRings;
import mineplex.core.gadget.gadgets.particle.ParticleFoot;
import mineplex.core.gadget.gadgets.particle.ParticleFoxTail;
import mineplex.core.gadget.gadgets.particle.ParticleFrostLord;
import mineplex.core.gadget.gadgets.particle.ParticleHeart;
import mineplex.core.gadget.gadgets.particle.ParticleInfused;
import mineplex.core.gadget.gadgets.particle.ParticleJetPack;
import mineplex.core.gadget.gadgets.particle.ParticleLegendaryHero;
import mineplex.core.gadget.gadgets.particle.ParticleMusic;
import mineplex.core.gadget.gadgets.particle.ParticlePartyTime;
import mineplex.core.gadget.gadgets.particle.ParticleRain;
import mineplex.core.gadget.gadgets.particle.ParticleRainbowTrail;
import mineplex.core.gadget.gadgets.particle.ParticleWingsAngel;
import mineplex.core.gadget.gadgets.particle.ParticleWingsDemons;
import mineplex.core.gadget.gadgets.particle.ParticleWingsInfernal;
import mineplex.core.gadget.gadgets.particle.ParticleWingsLove;
import mineplex.core.gadget.gadgets.particle.ParticleWingsPixie;
import mineplex.core.gadget.gadgets.particle.ParticleWolfTail;
import mineplex.core.gadget.gadgets.particle.ParticleYinYang;
import mineplex.core.gadget.gadgets.particle.christmas.ParticleChristmasTree;
import mineplex.core.gadget.gadgets.particle.freedom.ParticleAuraNiceness;
import mineplex.core.gadget.gadgets.particle.freedom.ParticleCanadian;
import mineplex.core.gadget.gadgets.particle.freedom.ParticleFreedom;
import mineplex.core.gadget.gadgets.particle.freedom.ParticleFreedomFireworks;
import mineplex.core.gadget.gadgets.particle.freedom.ParticleStarSpangled;
import mineplex.core.gadget.gadgets.particle.spring.ParticleSpringHalo;
import mineplex.core.gadget.gadgets.taunts.BlowAKissTaunt;
import mineplex.core.gadget.gadgets.taunts.EmojiTaunt;
import mineplex.core.gadget.gadgets.taunts.RainbowTaunt;
import mineplex.core.gadget.gadgets.wineffect.WinEffectBabyChicken;
import mineplex.core.gadget.gadgets.wineffect.WinEffectEarthquake;
import mineplex.core.gadget.gadgets.wineffect.WinEffectFlames;
import mineplex.core.gadget.gadgets.wineffect.WinEffectHalloween;
import mineplex.core.gadget.gadgets.wineffect.WinEffectLavaTrap;
import mineplex.core.gadget.gadgets.wineffect.WinEffectLightningStrike;
import mineplex.core.gadget.gadgets.wineffect.WinEffectMrPunchMan;
import mineplex.core.gadget.gadgets.wineffect.WinEffectPartyAnimal;
import mineplex.core.gadget.gadgets.wineffect.WinEffectRiseOfTheElderGuardian;
import mineplex.core.gadget.gadgets.wineffect.WinEffectSnowTrails;
import mineplex.core.gadget.types.Gadget;
import mineplex.core.pet.PetType;
import mineplex.core.reward.RewardType;
import mineplex.core.treasure.animation.animations.OmegaChestAnimation;
import mineplex.core.treasure.reward.RewardRarity;

public class OmegaTreasure extends Treasure
{

	public OmegaTreasure()
	{
		super(TreasureType.OMEGA);

		setAnimation(treasureLocation -> new OmegaChestAnimation(this, treasureLocation));
		setRewards(RewardType.OMEGA_CHEST);
		setRewardsPerChest(1);
		enabledByDefault();
	}

	@Override
	protected void addUncommon(RewardRarity rarity)
	{
		addGadgetReward(getGadget(ArrowTrailRedWhite.class), rarity, 2);
		addGadgetReward(getGadget(ArrowTrailFreedom.class), rarity, 2);

		addMusicReward("Blocks Disc", rarity, 25);
		addMusicReward("Cat Disc", rarity, 25);
		addMusicReward("Chirp Disc", rarity, 25);
		addMusicReward("Far Disc", rarity, 25);
		addMusicReward("Mall Disc", rarity, 25);
		addMusicReward("Mellohi Disc", rarity, 25);
		addMusicReward("Stal Disc", rarity, 25);
		addMusicReward("Strad Disc", rarity, 25);
		addMusicReward("Wait Disc", rarity, 25);
		addMusicReward("Ward Disc", rarity, 25);

		addGadgetReward(getGadget(ArrowTrailHalloween.class), rarity, 50);

		addHatReward(HatType.PUMPKIN, rarity, 75);
		addHatReward(HatType.PRESENT, rarity, 5);
		addHatReward(HatType.SNOWMAN, rarity, 5);

		addBalloonReward(BalloonType.BABY_COW, rarity, 10, 100);
		addBalloonReward(BalloonType.BABY_PIG, rarity, 10, 100);
		addBalloonReward(BalloonType.BABY_SHEEP, rarity, 15, 100);
	}

	@Override
	protected void addRare(RewardRarity rarity)
	{
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

		addGadgetReward(getGadget(DoubleJumpFreedom.class), rarity, 5);
		addGadgetReward(getGadget(DoubleJumpBlood.class), rarity, 50);
		addGadgetReward(getGadget(DoubleJumpFirecracker.class), rarity, 33);
		addGadgetReward(getGadget(DoubleJumpEmerald.class), rarity, 25);
		addGadgetReward(getGadget(DoubleJumpMusic.class), rarity, 20);
		addGadgetReward(getGadget(DoubleJumpShadow.class), rarity, 15);
		addGadgetReward(getGadget(DoubleJumpStorm.class), rarity, 30);
		addGadgetReward(getGadget(DoubleJumpCandyCane.class), rarity, 20);
		addGadgetReward(getGadget(DoubleJumpHalloween.class), rarity, 50);
		addGadgetReward(getGadget(DoubleJumpSpring.class), rarity, 40);
		addGadgetReward(getGadget(DoubleJumpFreedom.class), rarity, 50);
		addGadgetReward(getGadget(DoubleJumpMaple.class), rarity, 50);
		addGadgetReward(getGadget(DoubleJumpBalance.class), rarity, 20);

		addGadgetReward(getGadget(DeathFreedom.class), rarity, 15);
		addGadgetReward(getGadget(DeathStorm.class), rarity, 30);
		addGadgetReward(getGadget(DeathBlood.class), rarity, 50);
		addGadgetReward(getGadget(DeathEmerald.class), rarity, 25);
		addGadgetReward(getGadget(DeathMusic.class), rarity, 20);
		addGadgetReward(getGadget(DeathPinataBurst.class), rarity, 27);
		addGadgetReward(getGadget(DeathShadow.class), rarity, 15);
		addGadgetReward(getGadget(DeathCandyCane.class), rarity, 25);
		addGadgetReward(getGadget(DeathSpring.class), rarity, 60);
		addGadgetReward(getGadget(DeathMapleLeaf.class), rarity, 10);
		addGadgetReward(getGadget(DeathBalance.class), rarity, 20);

		addGadgetReward(getGadget(ArrowTrailFreedom.class), rarity, 10);
		addGadgetReward(getGadget(ArrowTrailConfetti.class), rarity, 27);
		addGadgetReward(getGadget(ArrowTrailBlood.class), rarity, 50);
		addGadgetReward(getGadget(ArrowTrailEmerald.class), rarity, 25);
		addGadgetReward(getGadget(ArrowTrailMusic.class), rarity, 27);
		addGadgetReward(getGadget(ArrowTrailStorm.class), rarity, 30);
		addGadgetReward(getGadget(ArrowTrailShadow.class), rarity, 15);
		addGadgetReward(getGadget(ArrowTrailCandyCane.class), rarity, 10);
		addGadgetReward(getGadget(ArrowTrailSpring.class), rarity, 60);
		addGadgetReward(getGadget(ArrowTrailBalance.class), rarity, 20);

		addHatReward(HatType.UNCLE_SAM, rarity, 25);
		addHatReward(HatType.COMPANION_BLOCK, rarity, 15);
		addHatReward(HatType.LOVESTRUCK, rarity, 20);
		addHatReward(HatType.SECRET_PACKAGE, rarity, 25);
		addHatReward(HatType.TEDDY_BEAR, rarity, 25);
		addHatReward(HatType.SANTA, rarity, 25);
		addHatReward(HatType.RUDOLPH, rarity, 25);
		addHatReward(HatType.COAL, rarity, 25);
		addHatReward(HatType.AMERICA, rarity, 50);
		addHatReward(HatType.CANADA, rarity, 50);

		addGadgetReward(getGadget(MorphChicken.class), rarity, 50);
		addGadgetReward(getGadget(MorphCow.class), rarity, 167);
		addGadgetReward(getGadget(MorphEnderman.class), rarity, 33);
		addGadgetReward(getGadget(MorphVillager.class), rarity, 83);
		addGadgetReward(getGadget(MorphSkeleton.class), rarity, 40);
		addGadgetReward(getGadget(MorphWolf.class), rarity, 25);

		addGadgetReward(getGadget(WinEffectFlames.class), rarity, 100);
		addGadgetReward(getGadget(WinEffectSnowTrails.class), rarity, 100);

		addGadgetReward(getGadget(MountFrost.class), rarity, 50);
		addGadgetReward(getGadget(MountCart.class), rarity, 100);
		addGadgetReward(getGadget(MountMule.class), rarity, 200);
		addGadgetReward(getGadget(MountSlime.class), rarity, 67);
		addGadgetReward(getGadget(MountLoveTrain.class), rarity, 20);

		addGadgetReward(getGadget(OutfitRaveSuitBoots.class), rarity, 30);
		addGadgetReward(getGadget(OutfitRaveSuitChestplate.class), rarity, 30);
		addGadgetReward(getGadget(OutfitRaveSuitLeggings.class), rarity, 30);
		addGadgetReward(getGadget(OutfitRaveSuitHelmet.class), rarity, 30);
		addGadgetReward(getGadget(OutfitSpaceSuitBoots.class), rarity, 50);
		addGadgetReward(getGadget(OutfitSpaceSuitChestplate.class), rarity, 50);
		addGadgetReward(getGadget(OutfitSpaceSuitLeggings.class), rarity, 50);
		addGadgetReward(getGadget(OutfitSpaceSuitHelmet.class), rarity, 50);
		addGadgetReward(getGadget(OutfitStPatricksChestplate.class), rarity, 50);
		addGadgetReward(getGadget(OutfitStPatricksLeggings.class), rarity, 50);
		addGadgetReward(getGadget(OutfitStPatricksBoots.class), rarity, 50);
		addGadgetReward(getGadget(OutfitFreezeSuitChestplate.class), rarity, 50);
		addGadgetReward(getGadget(OutfitFreezeSuitLeggings.class), rarity, 50);
		addGadgetReward(getGadget(OutfitFreezeSuitBoots.class), rarity, 50);

		addGadgetReward(getGadget(ParticleCandyCane.class), rarity, 20);
		addGadgetReward(getGadget(ParticleChristmasTree.class), rarity, 40);

		addBalloonReward(BalloonType.BABY_ZOMBIE, rarity, 25, 500);
		addBalloonReward(BalloonType.BABY_MUSHROOM, rarity, 50, 500);
		addBalloonReward(BalloonType.BABY_OCELOT, rarity, 50, 500);
		addBalloonReward(BalloonType.BABY_WOLF, rarity, 75, 500);
		addBalloonReward(BalloonType.BABY_VILLAGER, rarity, 25, 500);
		addBalloonReward(BalloonType.BABY_SLIME, rarity, 25, 500);
		addBalloonReward(BalloonType.BAT, rarity, 50, 500);
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

		addGadgetReward(getGadget(HaloKitSelector.class), rarity, 100);
		addGadgetReward(getGadget(RainbowDanceKitSelector.class), rarity, 100);
		addGadgetReward(getGadget(ShimmeringRingKitSelector.class), rarity, 100);
		addGadgetReward(getKitSelector(SingleParticleSelectors.FLAMES_OF_FURY), rarity, 100);
	}

	@Override
	protected void addLegendary(RewardRarity rarity)
	{
		addTitleReward("ayyye", rarity, 25, 500);
		addTitleReward("ameno", rarity, 15, 5000);
		addTitleReward("magician", rarity, 25, 5000);
		addTitleReward("fireball", rarity, 75, 5000);
		addTitleReward("magic-missile", rarity, 75, 5000);
		addTitleReward("pewpewpew", rarity, 75, 5000);
		addTitleReward("stardust", rarity, 60, 5000);
		addTitleReward("blow-a-kiss", rarity, 60, 5000);
		addTitleReward("cool-guy", rarity, 60, 5000);
		addTitleReward("deal-with-it", rarity, 60, 5000);
		addTitleReward("party-time", rarity, 55, 5000);
		addTitleReward("lalala", rarity, 30, 5000);
		addTitleReward("gotta-go", rarity, 30, 5000);
		addTitleReward("whaaat", rarity, 30, 5000);

		addPetReward(PetType.VILLAGER, rarity, 1);
		addPetReward(PetType.ZOMBIE, rarity, 10);
		addPetReward(PetType.PIG_ZOMBIE, rarity, 1);
		addPetReward(PetType.BLAZE, rarity, 2);
		addPetReward(PetType.RABBIT, rarity, 10);
		addPetReward(PetType.KILLER_BUNNY, rarity, 3);
		addPetReward(PetType.CUPID_PET, rarity, 40);
		addPetReward(PetType.LEPRECHAUN, rarity, 8);

		addGadgetReward(getGadget(MorphUncleSam.class), rarity, 5);
		addGadgetReward(getGadget(MorphPumpkinKing.class), rarity, 1);
		addGadgetReward(getGadget(MorphBat.class), rarity, 25);
		addGadgetReward(getGadget(MorphSlime.class), rarity, 10);
		addGadgetReward(getGadget(MorphBlock.class), rarity, 20);
		addGadgetReward(getGadget(MorphSnowman.class), rarity, 10);
		addGadgetReward(getGadget(MorphGrimReaper.class), rarity, 25);
		addGadgetReward(getGadget(MorphAwkwardRabbit.class), rarity, 30);
		addGadgetReward(getGadget(MorphLoveDoctor.class), rarity, 40);

		addGadgetReward(getGadget(ParticleFreedom.class), rarity, 15);
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
		addGadgetReward(getGadget(ParticleCoalFumes.class), rarity, 1);
		addGadgetReward(getGadget(ParticleFrostLord.class), rarity, 10);
		addGadgetReward(getGadget(ParticlePartyTime.class), rarity, 25);
		addGadgetReward(getGadget(ParticleSpringHalo.class), rarity, 8);
		addGadgetReward(getGadget(ParticleWingsLove.class), rarity, 10);
		addGadgetReward(getGadget(ParticleFreedomFireworks.class), rarity, 24);
		addGadgetReward(getGadget(ParticleAuraNiceness.class), rarity, 4);
		addGadgetReward(getGadget(ParticleCanadian.class), rarity, 1);
		addGadgetReward(getGadget(ParticleStarSpangled.class), rarity, 1);
		addGadgetReward(getGadget(ParticleChickenWings.class), rarity, 15);
		addGadgetReward(getGadget(ParticleFoxTail.class), rarity, 15);
		addGadgetReward(getGadget(ParticleWolfTail.class), rarity, 15);
		addGadgetReward(getGadget(ParticleJetPack.class), rarity, 15);
		addGadgetReward(getGadget(ParticleCape.class), rarity, 15);
		addGadgetReward(getGadget(ParticleLegendaryHero.class), rarity, 10);
		addGadgetReward(getGadget(ParticleRainbowTrail.class), rarity, 10);
		addGadgetReward(getGadget(ParticleDeepSeaSwirl.class), rarity, 10);
		addGadgetReward(getGadget(ParticleInfused.class), rarity, 10);

		addGadgetReward(getGadget(MountFreedomHorse.class), rarity, 5);
		addGadgetReward(getGadget(MountZombie.class), rarity, 1);
		addGadgetReward(getGadget(MountSpider.class), rarity, 1);
		addGadgetReward(getGadget(MountUndead.class), rarity, 1);
		addGadgetReward(getGadget(MountValentinesSheep.class), rarity, 20);
		addGadgetReward(getGadget(MountBabyReindeer.class), rarity, 1);
		addGadgetReward(getGadget(MountNightmareSteed.class), rarity, 10);
		addGadgetReward(getGadget(MountChicken.class), rarity, 5);
		addGadgetReward(getGadget(MountCake.class), rarity, 10);
		addGadgetReward(getGadget(MountStPatricksHorse.class), rarity, 3);

		addGadgetReward(getGadget(WinEffectBabyChicken.class), rarity, 10);
		addGadgetReward(getGadget(WinEffectLavaTrap.class), rarity, 20);
		addGadgetReward(getGadget(WinEffectRiseOfTheElderGuardian.class), rarity, 4);
		addGadgetReward(getGadget(WinEffectLightningStrike.class), rarity, 20);
		addGadgetReward(getGadget(WinEffectMrPunchMan.class), rarity, 33);
		addGadgetReward(getGadget(WinEffectHalloween.class), rarity, 75);
		addGadgetReward(getGadget(WinEffectEarthquake.class), rarity, 10);
		addGadgetReward(getGadget(WinEffectPartyAnimal.class), rarity, 10);

		addGadgetReward(getGadget(DeathEnchant.class), rarity, 10);
		addGadgetReward(getGadget(DeathCupidsBrokenHeart.class), rarity, 25);
		addGadgetReward(getGadget(DeathFrostLord.class), rarity, 15);
		addGadgetReward(getGadget(DeathPresentDanger.class), rarity, 27);

		addGadgetReward(getGadget(DoubleJumpEnchant.class), rarity, 10);
		addGadgetReward(getGadget(DoubleJumpCupidsWings.class), rarity, 5);
		addGadgetReward(getGadget(DoubleJumpFrostLord.class), rarity, 10);

		addGadgetReward(getGadget(ArrowTrailEnchant.class), rarity, 10);
		addGadgetReward(getGadget(ArrowTrailFrostLord.class), rarity, 20);
		addGadgetReward(getGadget(ArrowTrailCupid.class), rarity, 15);

		addHatReward(HatType.GRINCH, rarity, 25);

		addGadgetReward(getGadget(OutfitStPatricksHat.class), rarity, 5);
		addGadgetReward(getGadget(OutfitFreezeSuitHelmet.class), rarity, 2);

		addGadgetReward(getGadget(BlowAKissTaunt.class), rarity, 7);
		addGadgetReward(getGadget(RainbowTaunt.class), rarity, 1);
		addGadgetReward(getGadget(EmojiTaunt.class), rarity, 15);

		addFlagReward(FlagType.CANADA, rarity, 35);
		addFlagReward(FlagType.USA, rarity, 35);
		addFlagReward(FlagType.HEART, rarity, 50);

		addGadgetReward(getGadget(ItemTrampoline.class), rarity, 10);

		List<Gadget> powerPlayGadgets = GADGET_MANAGER.getPowerPlayGadgets();

		if (powerPlayGadgets.size() > 6)
		{
			powerPlayGadgets = powerPlayGadgets.subList(0, powerPlayGadgets.size() - 6);
		}

		powerPlayGadgets.forEach(gadget -> addGadgetReward(gadget, rarity, 5));
	}

	@Override
	protected void addMythical(RewardRarity rarity)
	{
	}
}
