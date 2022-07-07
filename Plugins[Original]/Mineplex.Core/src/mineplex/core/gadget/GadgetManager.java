package mineplex.core.gadget;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import mineplex.core.MiniPlugin;
import mineplex.core.ReflectivelyCreateMiniPlugin;
import mineplex.core.account.CoreClientManager;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.achievement.AchievementManager;
import mineplex.core.arcadeevents.CoreGameStartEvent;
import mineplex.core.arcadeevents.CoreGameStopEvent;
import mineplex.core.blockrestore.BlockRestore;
import mineplex.core.common.skin.SkinData;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.cosmetic.ui.page.GadgetPage;
import mineplex.core.cosmetic.ui.page.gamemodifiers.GameCosmeticCategoryPage;
import mineplex.core.cosmetic.ui.page.gamemodifiers.moba.HeroSkinCategoryPage;
import mineplex.core.disguise.DisguiseManager;
import mineplex.core.donation.DonationManager;
import mineplex.core.gadget.commands.AmmoCommand;
import mineplex.core.gadget.commands.LockCosmeticsCommand;
import mineplex.core.gadget.commands.TestTauntCommand;
import mineplex.core.gadget.commands.UnlockCosmeticCommand;
import mineplex.core.gadget.commands.UnlockCosmeticsCommand;
import mineplex.core.gadget.event.GadgetBlockEvent;
import mineplex.core.gadget.event.GadgetChangeEvent;
import mineplex.core.gadget.event.GadgetChangeEvent.GadgetState;
import mineplex.core.gadget.event.GadgetCollideEntityEvent;
import mineplex.core.gadget.event.GadgetEnableEvent;
import mineplex.core.gadget.event.GadgetSelectLocationEvent;
import mineplex.core.gadget.event.PlayerToggleSwimEvent;
import mineplex.core.gadget.event.TauntCommandEvent;
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
import mineplex.core.gadget.gadgets.arrowtrail.ArrowTrailLegend;
import mineplex.core.gadget.gadgets.arrowtrail.ArrowTrailMusic;
import mineplex.core.gadget.gadgets.arrowtrail.ArrowTrailPresent;
import mineplex.core.gadget.gadgets.arrowtrail.ArrowTrailRainbow;
import mineplex.core.gadget.gadgets.arrowtrail.ArrowTrailRedWhite;
import mineplex.core.gadget.gadgets.arrowtrail.ArrowTrailShadow;
import mineplex.core.gadget.gadgets.arrowtrail.ArrowTrailSpring;
import mineplex.core.gadget.gadgets.arrowtrail.ArrowTrailStorm;
import mineplex.core.gadget.gadgets.arrowtrail.ArrowTrailTitan;
import mineplex.core.gadget.gadgets.balloons.BalloonType;
import mineplex.core.gadget.gadgets.chat.LevelPrefixType;
import mineplex.core.gadget.gadgets.death.DeathBalance;
import mineplex.core.gadget.gadgets.death.DeathBlood;
import mineplex.core.gadget.gadgets.death.DeathCandyCane;
import mineplex.core.gadget.gadgets.death.DeathCupidsBrokenHeart;
import mineplex.core.gadget.gadgets.death.DeathEmerald;
import mineplex.core.gadget.gadgets.death.DeathEnchant;
import mineplex.core.gadget.gadgets.death.DeathFreedom;
import mineplex.core.gadget.gadgets.death.DeathFrostLord;
import mineplex.core.gadget.gadgets.death.DeathHalloween;
import mineplex.core.gadget.gadgets.death.DeathLegend;
import mineplex.core.gadget.gadgets.death.DeathMapleLeaf;
import mineplex.core.gadget.gadgets.death.DeathMusic;
import mineplex.core.gadget.gadgets.death.DeathPinataBurst;
import mineplex.core.gadget.gadgets.death.DeathPresentDanger;
import mineplex.core.gadget.gadgets.death.DeathRainbow;
import mineplex.core.gadget.gadgets.death.DeathShadow;
import mineplex.core.gadget.gadgets.death.DeathSpring;
import mineplex.core.gadget.gadgets.death.DeathStorm;
import mineplex.core.gadget.gadgets.death.DeathTitan;
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
import mineplex.core.gadget.gadgets.doublejump.DoubleJumpLegend;
import mineplex.core.gadget.gadgets.doublejump.DoubleJumpMaple;
import mineplex.core.gadget.gadgets.doublejump.DoubleJumpMusic;
import mineplex.core.gadget.gadgets.doublejump.DoubleJumpPresent;
import mineplex.core.gadget.gadgets.doublejump.DoubleJumpRainbow;
import mineplex.core.gadget.gadgets.doublejump.DoubleJumpShadow;
import mineplex.core.gadget.gadgets.doublejump.DoubleJumpSpring;
import mineplex.core.gadget.gadgets.doublejump.DoubleJumpStorm;
import mineplex.core.gadget.gadgets.doublejump.DoubleJumpTitan;
import mineplex.core.gadget.gadgets.flag.FlagType;
import mineplex.core.gadget.gadgets.gamemodifiers.GameCosmeticCategory;
import mineplex.core.gadget.gadgets.gamemodifiers.GameCosmeticManager;
import mineplex.core.gadget.gadgets.gamemodifiers.GameCosmeticType;
import mineplex.core.gadget.gadgets.gamemodifiers.minestrike.GameModifierMineStrikeSkin;
import mineplex.core.gadget.gadgets.gamemodifiers.minestrike.MineStrikeSkin;
import mineplex.core.gadget.gadgets.gamemodifiers.moba.shopmorph.ShopMorphGadget;
import mineplex.core.gadget.gadgets.gamemodifiers.moba.shopmorph.ShopMorphType;
import mineplex.core.gadget.gadgets.gamemodifiers.moba.skins.HeroSkinGadget;
import mineplex.core.gadget.gadgets.hat.HatItem;
import mineplex.core.gadget.gadgets.hat.HatType;
import mineplex.core.gadget.gadgets.item.ItemBallCatch;
import mineplex.core.gadget.gadgets.item.ItemBatGun;
import mineplex.core.gadget.gadgets.item.ItemBow;
import mineplex.core.gadget.gadgets.item.ItemCandy;
import mineplex.core.gadget.gadgets.item.ItemClacker;
import mineplex.core.gadget.gadgets.item.ItemCoal;
import mineplex.core.gadget.gadgets.item.ItemCoinBomb;
import mineplex.core.gadget.gadgets.item.ItemConnect4;
import mineplex.core.gadget.gadgets.item.ItemGrapplingHook;
import mineplex.core.gadget.gadgets.item.ItemDuelingSword;
import mineplex.core.gadget.gadgets.item.ItemEtherealPearl;
import mineplex.core.gadget.gadgets.item.ItemFirework;
import mineplex.core.gadget.gadgets.item.ItemFleshHook;
import mineplex.core.gadget.gadgets.item.ItemFlowerGift;
import mineplex.core.gadget.gadgets.item.ItemFreezeCannon;
import mineplex.core.gadget.gadgets.item.ItemLovePotion;
import mineplex.core.gadget.gadgets.item.ItemMaryPoppins;
import mineplex.core.gadget.gadgets.item.ItemMelonLauncher;
import mineplex.core.gadget.gadgets.item.ItemMobBomb;
import mineplex.core.gadget.gadgets.item.ItemOAndX;
import mineplex.core.gadget.gadgets.item.ItemPaintballGun;
import mineplex.core.gadget.gadgets.item.ItemPaintbrush;
import mineplex.core.gadget.gadgets.item.ItemPartyPopper;
import mineplex.core.gadget.gadgets.item.ItemSnowball;
import mineplex.core.gadget.gadgets.item.ItemSortal;
import mineplex.core.gadget.gadgets.item.ItemTNT;
import mineplex.core.gadget.gadgets.item.ItemTrampoline;
import mineplex.core.gadget.gadgets.kitselector.HalloweenKitSelector;
import mineplex.core.gadget.gadgets.kitselector.HaloKitSelector;
import mineplex.core.gadget.gadgets.kitselector.RainCloudKitSelector;
import mineplex.core.gadget.gadgets.kitselector.RainbowDanceKitSelector;
import mineplex.core.gadget.gadgets.kitselector.ShimmeringRingKitSelector;
import mineplex.core.gadget.gadgets.kitselector.SingleParticleKitSelector;
import mineplex.core.gadget.gadgets.kitselector.WaterWingsKitSelector;
import mineplex.core.gadget.gadgets.morph.MorphAwkwardRabbit;
import mineplex.core.gadget.gadgets.morph.MorphBat;
import mineplex.core.gadget.gadgets.morph.MorphBlaze;
import mineplex.core.gadget.gadgets.morph.MorphBlock;
import mineplex.core.gadget.gadgets.morph.MorphBobRoss;
import mineplex.core.gadget.gadgets.morph.MorphBunny;
import mineplex.core.gadget.gadgets.morph.MorphChicken;
import mineplex.core.gadget.gadgets.morph.MorphChristmasKing;
import mineplex.core.gadget.gadgets.morph.MorphCow;
import mineplex.core.gadget.gadgets.morph.MorphCreeper;
import mineplex.core.gadget.gadgets.morph.MorphDinnerbone;
import mineplex.core.gadget.gadgets.morph.MorphEnderman;
import mineplex.core.gadget.gadgets.morph.MorphFreedomFighter;
import mineplex.core.gadget.gadgets.morph.MorphFrostGolem;
import mineplex.core.gadget.gadgets.morph.MorphGhast;
import mineplex.core.gadget.gadgets.morph.MorphGoldPot;
import mineplex.core.gadget.gadgets.morph.MorphGrimReaper;
import mineplex.core.gadget.gadgets.morph.MorphLoveDoctor;
import mineplex.core.gadget.gadgets.morph.MorphMelonHead;
import mineplex.core.gadget.gadgets.morph.MorphMetalMan;
import mineplex.core.gadget.gadgets.morph.MorphOcelot;
import mineplex.core.gadget.gadgets.morph.MorphPig;
import mineplex.core.gadget.gadgets.morph.MorphPumpkinKing;
import mineplex.core.gadget.gadgets.morph.MorphSanta;
import mineplex.core.gadget.gadgets.morph.MorphSkeleton;
import mineplex.core.gadget.gadgets.morph.MorphSlime;
import mineplex.core.gadget.gadgets.morph.MorphSnowman;
import mineplex.core.gadget.gadgets.morph.MorphSquid;
import mineplex.core.gadget.gadgets.morph.MorphTitan;
import mineplex.core.gadget.gadgets.morph.MorphTurkey;
import mineplex.core.gadget.gadgets.morph.MorphUncleSam;
import mineplex.core.gadget.gadgets.morph.MorphVillager;
import mineplex.core.gadget.gadgets.morph.MorphWitch;
import mineplex.core.gadget.gadgets.morph.MorphWither;
import mineplex.core.gadget.gadgets.morph.MorphWolf;
import mineplex.core.gadget.gadgets.morph.managers.SoulManager;
import mineplex.core.gadget.gadgets.morph.moba.MorphAnath;
import mineplex.core.gadget.gadgets.morph.moba.MorphBardolf;
import mineplex.core.gadget.gadgets.morph.moba.MorphBiff;
import mineplex.core.gadget.gadgets.morph.moba.MorphDana;
import mineplex.core.gadget.gadgets.morph.moba.MorphDevon;
import mineplex.core.gadget.gadgets.morph.moba.MorphHattori;
import mineplex.core.gadget.gadgets.morph.moba.MorphIvy;
import mineplex.core.gadget.gadgets.morph.moba.MorphLarissa;
import mineplex.core.gadget.gadgets.morph.moba.MorphRowena;
import mineplex.core.gadget.gadgets.mount.types.MountBabyReindeer;
import mineplex.core.gadget.gadgets.mount.types.MountCake;
import mineplex.core.gadget.gadgets.mount.types.MountCart;
import mineplex.core.gadget.gadgets.mount.types.MountChicken;
import mineplex.core.gadget.gadgets.mount.types.MountDragon;
import mineplex.core.gadget.gadgets.mount.types.MountFreedomHorse;
import mineplex.core.gadget.gadgets.mount.types.MountFrost;
import mineplex.core.gadget.gadgets.mount.types.MountLoveTrain;
import mineplex.core.gadget.gadgets.mount.types.MountMule;
import mineplex.core.gadget.gadgets.mount.types.MountNightmareSteed;
import mineplex.core.gadget.gadgets.mount.types.MountPumpkin;
import mineplex.core.gadget.gadgets.mount.types.MountSledge;
import mineplex.core.gadget.gadgets.mount.types.MountSlime;
import mineplex.core.gadget.gadgets.mount.types.MountSpider;
import mineplex.core.gadget.gadgets.mount.types.MountStPatricksHorse;
import mineplex.core.gadget.gadgets.mount.types.MountTitan;
import mineplex.core.gadget.gadgets.mount.types.MountUndead;
import mineplex.core.gadget.gadgets.mount.types.MountValentinesSheep;
import mineplex.core.gadget.gadgets.mount.types.MountZombie;
import mineplex.core.gadget.gadgets.outfit.OutfitTeam;
import mineplex.core.gadget.gadgets.outfit.freezesuit.OutfitFreezeSuitBoots;
import mineplex.core.gadget.gadgets.outfit.freezesuit.OutfitFreezeSuitChestplate;
import mineplex.core.gadget.gadgets.outfit.freezesuit.OutfitFreezeSuitHelmet;
import mineplex.core.gadget.gadgets.outfit.freezesuit.OutfitFreezeSuitLeggings;
import mineplex.core.gadget.gadgets.outfit.ravesuit.OutfitRaveSuitBoots;
import mineplex.core.gadget.gadgets.outfit.ravesuit.OutfitRaveSuitChestplate;
import mineplex.core.gadget.gadgets.outfit.ravesuit.OutfitRaveSuitHelmet;
import mineplex.core.gadget.gadgets.outfit.ravesuit.OutfitRaveSuitLeggings;
import mineplex.core.gadget.gadgets.outfit.reindeer.OutfitReindeerAntlers;
import mineplex.core.gadget.gadgets.outfit.reindeer.OutfitReindeerChest;
import mineplex.core.gadget.gadgets.outfit.reindeer.OutfitReindeerHooves;
import mineplex.core.gadget.gadgets.outfit.reindeer.OutfitReindeerLegs;
import mineplex.core.gadget.gadgets.outfit.spacesuit.OutfitSpaceSuitBoots;
import mineplex.core.gadget.gadgets.outfit.spacesuit.OutfitSpaceSuitChestplate;
import mineplex.core.gadget.gadgets.outfit.spacesuit.OutfitSpaceSuitHelmet;
import mineplex.core.gadget.gadgets.outfit.spacesuit.OutfitSpaceSuitLeggings;
import mineplex.core.gadget.gadgets.outfit.stpatricks.OutfitStPatricksBoots;
import mineplex.core.gadget.gadgets.outfit.stpatricks.OutfitStPatricksChestplate;
import mineplex.core.gadget.gadgets.outfit.stpatricks.OutfitStPatricksHat;
import mineplex.core.gadget.gadgets.outfit.stpatricks.OutfitStPatricksLeggings;
import mineplex.core.gadget.gadgets.outfit.windup.OutfitWindupBoots;
import mineplex.core.gadget.gadgets.outfit.windup.OutfitWindupChestplate;
import mineplex.core.gadget.gadgets.outfit.windup.OutfitWindupHelmet;
import mineplex.core.gadget.gadgets.outfit.windup.OutfitWindupLeggings;
import mineplex.core.gadget.gadgets.particle.ParticleBlood;
import mineplex.core.gadget.gadgets.particle.ParticleCandyCane;
import mineplex.core.gadget.gadgets.particle.ParticleCoalFumes;
import mineplex.core.gadget.gadgets.particle.ParticleDeepSeaSwirl;
import mineplex.core.gadget.gadgets.particle.ParticleFiveYear;
import mineplex.core.gadget.gadgets.particle.ParticleInfused;
import mineplex.core.gadget.gadgets.particle.ParticleEmerald;
import mineplex.core.gadget.gadgets.particle.ParticleEnchant;
import mineplex.core.gadget.gadgets.particle.ParticleWitchsCure;
import mineplex.core.gadget.gadgets.particle.ParticleFairy;
import mineplex.core.gadget.gadgets.particle.ParticleFireRings;
import mineplex.core.gadget.gadgets.particle.ParticleFoot;
import mineplex.core.gadget.gadgets.particle.ParticleFrostLord;
import mineplex.core.gadget.gadgets.particle.ParticleHalloween;
import mineplex.core.gadget.gadgets.particle.ParticleHeart;
import mineplex.core.gadget.gadgets.particle.ParticleMacawWings;
import mineplex.core.gadget.gadgets.particle.ParticleChickenWings;
import mineplex.core.gadget.gadgets.particle.ParticleEnderDragonWings;
import mineplex.core.gadget.gadgets.particle.ParticleFoxTail;
import mineplex.core.gadget.gadgets.particle.ParticleJetPack;
import mineplex.core.gadget.gadgets.particle.ParticleCape;
import mineplex.core.gadget.gadgets.particle.ParticleLegendaryHero;
import mineplex.core.gadget.gadgets.particle.ParticleWolfTail;
import mineplex.core.gadget.gadgets.particle.ParticleLegend;
import mineplex.core.gadget.gadgets.particle.ParticleMusic;
import mineplex.core.gadget.gadgets.particle.ParticlePartyTime;
import mineplex.core.gadget.gadgets.particle.ParticleRain;
import mineplex.core.gadget.gadgets.particle.ParticleRainbow;
import mineplex.core.gadget.gadgets.particle.ParticleRainbowTrail;
import mineplex.core.gadget.gadgets.particle.ParticleTitan;
import mineplex.core.gadget.gadgets.particle.ParticleWingsAngel;
import mineplex.core.gadget.gadgets.particle.ParticleWingsBee;
import mineplex.core.gadget.gadgets.particle.ParticleWingsDemons;
import mineplex.core.gadget.gadgets.particle.ParticleWingsInfernal;
import mineplex.core.gadget.gadgets.particle.ParticleWingsLove;
import mineplex.core.gadget.gadgets.particle.ParticleWingsPixie;
import mineplex.core.gadget.gadgets.particle.ParticleYinYang;
import mineplex.core.gadget.gadgets.particle.christmas.ParticleBlizzard;
import mineplex.core.gadget.gadgets.particle.christmas.ParticleChristmasTree;
import mineplex.core.gadget.gadgets.particle.christmas.ParticleFidgetSpinner;
import mineplex.core.gadget.gadgets.particle.christmas.ParticlePumpkinShield;
import mineplex.core.gadget.gadgets.particle.christmas.ParticleWingsChristmas;
import mineplex.core.gadget.gadgets.particle.freedom.ParticleAuraNiceness;
import mineplex.core.gadget.gadgets.particle.freedom.ParticleCanadian;
import mineplex.core.gadget.gadgets.particle.freedom.ParticleFreedom;
import mineplex.core.gadget.gadgets.particle.freedom.ParticleFreedomFireworks;
import mineplex.core.gadget.gadgets.particle.freedom.ParticleStarSpangled;
import mineplex.core.gadget.gadgets.particle.king.CastleManager;
import mineplex.core.gadget.gadgets.particle.king.ParticleKing;
import mineplex.core.gadget.gadgets.particle.spring.ParticleSpringHalo;
import mineplex.core.gadget.gadgets.taunts.BlowAKissTaunt;
import mineplex.core.gadget.gadgets.taunts.ChickenTaunt;
import mineplex.core.gadget.gadgets.taunts.EasyModeTaunt;
import mineplex.core.gadget.gadgets.taunts.EmojiTaunt;
import mineplex.core.gadget.gadgets.taunts.EternalTaunt;
import mineplex.core.gadget.gadgets.taunts.FrostBreathTaunt;
import mineplex.core.gadget.gadgets.taunts.InfernalTaunt;
import mineplex.core.gadget.gadgets.taunts.RainbowTaunt;
import mineplex.core.gadget.gadgets.weaponname.WeaponNameType;
import mineplex.core.gadget.gadgets.wineffect.WinEffectBabyChicken;
import mineplex.core.gadget.gadgets.wineffect.WinEffectEarthquake;
import mineplex.core.gadget.gadgets.wineffect.WinEffectFlames;
import mineplex.core.gadget.gadgets.wineffect.WinEffectHalloween;
import mineplex.core.gadget.gadgets.wineffect.WinEffectLavaTrap;
import mineplex.core.gadget.gadgets.wineffect.WinEffectLightningStrike;
import mineplex.core.gadget.gadgets.wineffect.WinEffectLogo;
import mineplex.core.gadget.gadgets.wineffect.WinEffectLoveIsABattlefield;
import mineplex.core.gadget.gadgets.wineffect.WinEffectMrPunchMan;
import mineplex.core.gadget.gadgets.wineffect.WinEffectPartyAnimal;
import mineplex.core.gadget.gadgets.wineffect.WinEffectPodium;
import mineplex.core.gadget.gadgets.wineffect.WinEffectRiseOfTheElderGuardian;
import mineplex.core.gadget.gadgets.wineffect.WinEffectSnowTrails;
import mineplex.core.gadget.gadgets.wineffect.WinEffectTornado;
import mineplex.core.gadget.gadgets.wineffect.WinEffectWinterWarfare;
import mineplex.core.gadget.gadgets.wineffect.rankrooms.rankwineffects.WinEffectRankEternal;
import mineplex.core.gadget.gadgets.wineffect.rankrooms.rankwineffects.WinEffectRankHero;
import mineplex.core.gadget.gadgets.wineffect.rankrooms.rankwineffects.WinEffectRankLegend;
import mineplex.core.gadget.gadgets.wineffect.rankrooms.rankwineffects.WinEffectRankTitan;
import mineplex.core.gadget.gadgets.wineffect.rankrooms.rankwineffects.WinEffectRankUltra;
import mineplex.core.gadget.mission.GadgetUseTracker;
import mineplex.core.gadget.persistence.UserGadgetPersistence;
import mineplex.core.gadget.set.SetBalance;
import mineplex.core.gadget.set.SetCanadian;
import mineplex.core.gadget.set.SetCandyCane;
import mineplex.core.gadget.set.SetCupidsLove;
import mineplex.core.gadget.set.SetEmerald;
import mineplex.core.gadget.set.SetFreedom;
import mineplex.core.gadget.set.SetFrostLord;
import mineplex.core.gadget.set.SetHalloween;
import mineplex.core.gadget.set.SetHowlingWinds;
import mineplex.core.gadget.set.SetLegend;
import mineplex.core.gadget.set.SetMusic;
import mineplex.core.gadget.set.SetParty;
import mineplex.core.gadget.set.SetRainbow;
import mineplex.core.gadget.set.SetShadow;
import mineplex.core.gadget.set.SetSpring;
import mineplex.core.gadget.set.SetTitan;
import mineplex.core.gadget.set.SetVampire;
import mineplex.core.gadget.set.SetWisdom;
import mineplex.core.gadget.set.suits.SetFreezeSuit;
import mineplex.core.gadget.set.suits.SetRaveSuit;
import mineplex.core.gadget.set.suits.SetReindeerSuit;
import mineplex.core.gadget.set.suits.SetSpaceSuit;
import mineplex.core.gadget.set.suits.SetStPatricksSuit;
import mineplex.core.gadget.set.suits.SetWindup;
import mineplex.core.gadget.types.BalloonGadget;
import mineplex.core.gadget.types.DoubleJumpEffectGadget;
import mineplex.core.gadget.types.FlagGadget;
import mineplex.core.gadget.types.Gadget;
import mineplex.core.gadget.types.GadgetSet;
import mineplex.core.gadget.types.GadgetType;
import mineplex.core.gadget.types.HatGadget;
import mineplex.core.gadget.types.ItemGadget;
import mineplex.core.gadget.types.LevelPrefixGadget;
import mineplex.core.gadget.types.MusicGadget;
import mineplex.core.gadget.types.OutfitGadget;
import mineplex.core.gadget.types.OutfitGadget.ArmorSlot;
import mineplex.core.gadget.types.TauntGadget;
import mineplex.core.gadget.types.WeaponNameGadget;
import mineplex.core.gadget.util.CostConstants;
import mineplex.core.game.GameDisplay;
import mineplex.core.hologram.HologramManager;
import mineplex.core.incognito.IncognitoManager;
import mineplex.core.incognito.events.IncognitoStatusChangeEvent;
import mineplex.core.inventory.InventoryManager;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.mission.MissionManager;
import mineplex.core.mission.MissionTrackerType;
import mineplex.core.packethandler.PacketHandler;
import mineplex.core.pet.PetManager;
import mineplex.core.preferences.PreferencesManager;
import mineplex.core.projectile.ProjectileManager;
import mineplex.core.recharge.Recharge;
import mineplex.core.stats.event.PlayerStatsLoadedEvent;

@ReflectivelyCreateMiniPlugin
public class GadgetManager extends MiniPlugin
{

	public enum Perm implements Permission
	{
		TITAN_ARROW_TRAIL,
		TITAN_DEATH_EFFECT,
		TITAN_DOUBLE_JUMP,
		ETERNAL_TAUNT,
		LEGEND_PARTICLE_EFFECT,
		TITAN_PARTICLE_EFFECT,
		HERO_MORPH_BLAZE,
		HERO_MORPH_CREEPER,
		HERO_MOUNT,
		ULTRA_MORPH,
		TITAN_MORPH,
		TITAN_MOUNT,
		LEGEND_MORPH,
		AMMO_COMMAND,
		LOCK_INFUSED_COMMAND,
		LOCK_COSMETICS_COMMAND,
		UNLOCK_COSMETIC_COMMAND,
		UNLOCK_COSMETICS_COMMAND,
		TEST_TAUNT_COMMAND
	}

	private final CoreClientManager _clientManager;
	private final DonationManager _donationManager;
	private final InventoryManager _inventoryManager;
	private final PetManager _petManager;
	private final PreferencesManager _preferencesManager;
	private final DisguiseManager _disguiseManager;
	private final BlockRestore _blockRestore;
	private final ProjectileManager _projectileManager;
	private final AchievementManager _achievementManager;
	private final PacketHandler _packetManager;
	private final HologramManager _hologramManager;
	private final IncognitoManager _incognitoManager;
	private final GameCosmeticManager _gameCosmeticManager;
	private final MissionManager _missionManager;
	private SoulManager _soulManager;
	private CastleManager _castleManager;

	private Map<GadgetType, List<Gadget>> _gadgets;
	private List<Gadget> _powerPlayGadgets;
	private Map<SingleParticleKitSelector.SingleParticleSelectors, Gadget> _singleParticleSelectors;

	private final Map<Player, Long> _lastMove = new HashMap<>();
	private final Map<Player, Map<GadgetType, Gadget>> _playerActiveGadgetMap = new HashMap<>();

	private final Set<GadgetSet> _sets = new HashSet<>();

	private UserGadgetPersistence _userGadgetPersistence;

	private boolean _hideParticles, _showWeaponNames = true;
	private int _activeItemSlot = 3;
	private boolean _gadgetsEnabled = true, _gameIsLive = false;

	private final Set<Player> _swimmingPlayers = new HashSet<>();

	private GadgetManager()
	{
		super("Gadget");

		_clientManager = require(CoreClientManager.class);
		_donationManager = require(DonationManager.class);
		_inventoryManager = require(InventoryManager.class);
		_petManager = require(PetManager.class);
		_preferencesManager = require(PreferencesManager.class);
		_disguiseManager = require(DisguiseManager.class);
		_blockRestore = require(BlockRestore.class);
		_projectileManager = require(ProjectileManager.class);
		_achievementManager = require(AchievementManager.class);
		_packetManager = require(PacketHandler.class);
		_hologramManager = require(HologramManager.class);
		_userGadgetPersistence = new UserGadgetPersistence(this);
		_incognitoManager = require(IncognitoManager.class);
		_gameCosmeticManager = require(GameCosmeticManager.class);
		_soulManager = new SoulManager();
		_castleManager = require(CastleManager.class);

		createGadgets();
		createSets();

		_missionManager = require(MissionManager.class);

		registerTrackers();
		generatePermissions();
	}

	private void generatePermissions()
	{
		PermissionGroup.TITAN.setPermission(Perm.TITAN_ARROW_TRAIL, true, true);
		PermissionGroup.TITAN.setPermission(Perm.TITAN_DEATH_EFFECT, true, true);
		PermissionGroup.TITAN.setPermission(Perm.TITAN_DOUBLE_JUMP, true, true);
		PermissionGroup.ETERNAL.setPermission(Perm.ETERNAL_TAUNT, true, true);
		PermissionGroup.LEGEND.setPermission(Perm.LEGEND_PARTICLE_EFFECT, true, true);
		PermissionGroup.TITAN.setPermission(Perm.TITAN_PARTICLE_EFFECT, true, true);
		PermissionGroup.HERO.setPermission(Perm.HERO_MORPH_BLAZE, true, true);
		PermissionGroup.HERO.setPermission(Perm.HERO_MORPH_CREEPER, true, true);
		PermissionGroup.HERO.setPermission(Perm.HERO_MOUNT, true, true);
		PermissionGroup.TITAN.setPermission(Perm.TITAN_MOUNT, true, true);
		PermissionGroup.ULTRA.setPermission(Perm.ULTRA_MORPH, true, true);
		PermissionGroup.TITAN.setPermission(Perm.TITAN_MORPH, true, true);
		PermissionGroup.LEGEND.setPermission(Perm.LEGEND_MORPH, true, true);
		PermissionGroup.ADMIN.setPermission(Perm.AMMO_COMMAND, true, true);
		PermissionGroup.ADMIN.setPermission(Perm.LOCK_INFUSED_COMMAND, true, true);
		PermissionGroup.ADMIN.setPermission(Perm.UNLOCK_COSMETIC_COMMAND, true, true);
		PermissionGroup.QA.setPermission(Perm.LOCK_COSMETICS_COMMAND, true, true);
		PermissionGroup.QA.setPermission(Perm.UNLOCK_COSMETICS_COMMAND, true, true);
		PermissionGroup.ADMIN.setPermission(Perm.LOCK_COSMETICS_COMMAND, true, true);
		PermissionGroup.ADMIN.setPermission(Perm.UNLOCK_COSMETICS_COMMAND, true, true);
		PermissionGroup.ADMIN.setPermission(Perm.TEST_TAUNT_COMMAND, true, true);

		if (UtilServer.isTestServer())
		{
			PermissionGroup.QA.setPermission(Perm.TEST_TAUNT_COMMAND, true, true);
		}
	}

	@Override
	public void addCommands()
	{
		addCommand(new UnlockCosmeticCommand(this));
		addCommand(new UnlockCosmeticsCommand(this));
		addCommand(new LockCosmeticsCommand(this));
		addCommand(new AmmoCommand(this));
		addCommand(new TestTauntCommand(this));
	}

	private void createSets()
	{
		//Costumes
		addSet(new SetRaveSuit(this));
		addSet(new SetSpaceSuit(this));
		addSet(new SetFreezeSuit(this));
		addSet(new SetStPatricksSuit(this));
		addSet(new SetReindeerSuit(this));
		addSet(new SetWindup(this));

		// Sets
		addSet(new SetFrostLord(this));
		addSet(new SetCandyCane(this));
		addSet(new SetTitan(this));
		addSet(new SetParty(this));
		addSet(new SetCupidsLove(this));
		addSet(new SetEmerald(this));
		addSet(new SetShadow(this));
		addSet(new SetWisdom(this));
		addSet(new SetHowlingWinds(this));
		addSet(new SetVampire(this));
		addSet(new SetMusic(this));
		addSet(new SetFreedom(this));
		addSet(new SetSpring(this));
		addSet(new SetCanadian(this));
		addSet(new SetHalloween(this));
		addSet(new SetRainbow(this));
		addSet(new SetLegend(this));
		addSet(new SetBalance(this));
	}

	private void createGadgets()
	{
		_gadgets = new HashMap<>(200);
		_powerPlayGadgets = new ArrayList<>();
		_singleParticleSelectors = new HashMap<>();

		// Items
		addGadget(new ItemEtherealPearl(this));
		addGadget(new ItemFirework(this));
		addGadget(new ItemTNT(this));
		addGadget(new ItemMelonLauncher(this));
		addGadget(new ItemFleshHook(this));
		addGadget(new ItemPaintballGun(this));
		addGadget(new ItemBatGun(this));
		addGadget(new ItemCoinBomb(this));
		addGadget(new ItemPaintbrush(this));
		addGadget(new ItemDuelingSword(this));
		addGadget(new ItemPartyPopper(this));
		addGadget(new ItemCoal(this));
		addGadget(new ItemFreezeCannon(this));
		addGadget(new ItemSnowball(this));
		addGadget(new ItemBow(this));
		addGadget(new ItemLovePotion(this));
		addGadget(new ItemFlowerGift(this));
		addGadget(new ItemCandy(this));
		addGadget(new ItemOAndX(this));
		addGadget(new ItemMobBomb(this));
		addGadget(new ItemBallCatch(this));
		addGadget(new ItemTrampoline(this));
		addGadget(new ItemConnect4(this));
		addGadget(new ItemMaryPoppins(this));
		addGadget(new ItemClacker(this));
		addGadget(new ItemSortal(this));
		addGadget(new ItemGrapplingHook(this));

		// Costume
		addGadget(new OutfitRaveSuitHelmet(this));
		addGadget(new OutfitRaveSuitChestplate(this));
		addGadget(new OutfitRaveSuitLeggings(this));
		addGadget(new OutfitRaveSuitBoots(this));

		addGadget(new OutfitSpaceSuitHelmet(this));
		addGadget(new OutfitSpaceSuitChestplate(this));
		addGadget(new OutfitSpaceSuitLeggings(this));
		addGadget(new OutfitSpaceSuitBoots(this));

		addGadget(new OutfitFreezeSuitHelmet(this));
		addGadget(new OutfitFreezeSuitChestplate(this));
		addGadget(new OutfitFreezeSuitLeggings(this));
		addGadget(new OutfitFreezeSuitBoots(this));

		addGadget(new OutfitStPatricksHat(this));
		addGadget(new OutfitStPatricksChestplate(this));
		addGadget(new OutfitStPatricksLeggings(this));
		addGadget(new OutfitStPatricksBoots(this));

		addGadget(new OutfitReindeerAntlers(this));
		addGadget(new OutfitReindeerChest(this));
		addGadget(new OutfitReindeerLegs(this));
		addGadget(new OutfitReindeerHooves(this));

		addGadget(new OutfitWindupHelmet(this));
		addGadget(new OutfitWindupChestplate(this));
		addGadget(new OutfitWindupLeggings(this));
		addGadget(new OutfitWindupBoots(this));

		addGadget(new OutfitTeam(this, "Team Helmet", -1, ArmorSlot.HELMET, Material.LEATHER_HELMET, (byte) 0));
		addGadget(new OutfitTeam(this, "Team Shirt", -1, ArmorSlot.CHEST, Material.LEATHER_CHESTPLATE, (byte) 0));
		addGadget(new OutfitTeam(this, "Team Pants", -1, ArmorSlot.LEGS, Material.LEATHER_LEGGINGS, (byte) 0));
		addGadget(new OutfitTeam(this, "Team Boots", -1, ArmorSlot.BOOTS, Material.LEATHER_BOOTS, (byte) 0));

		// Morphs
		addGadget(new MorphVillager(this));
		addGadget(new MorphCow(this));
		addGadget(new MorphChicken(this));
		addGadget(new MorphBlock(this));
		addGadget(new MorphEnderman(this));
		addGadget(new MorphBat(this));
		addGadget(new MorphPumpkinKing(this));
		addGadget(new MorphPig(this));
		addGadget(new MorphCreeper(this));
		addGadget(new MorphBlaze(this));
		addGadget(new MorphWither(this));
		addGadget(new MorphBunny(this));
		addGadget(new MorphSlime(this, _achievementManager));
		addGadget(new MorphTitan(this));
		addGadget(new MorphSnowman(this));
		addGadget(new MorphUncleSam(this));
		addGadget(new MorphSquid(this));
		addGadget(new MorphWitch(this));
		addGadget(new MorphGrimReaper(this));
		addGadget(new MorphMetalMan(this));
		addGadget(new MorphTurkey(this));
		addGadget(new MorphChristmasKing(this));
		addGadget(new MorphSanta(this));
		addGadget(new MorphDinnerbone(this));
		addGadget(new MorphLoveDoctor(this));
		addGadget(new MorphGoldPot(this));
		addGadget(new MorphAwkwardRabbit(this));
		addGadget(new MorphBobRoss(this, _hologramManager));
		addGadget(new MorphFreedomFighter(this));
		addGadget(new MorphMelonHead(this));
		addGadget(new MorphHattori(this));
		addGadget(new MorphDevon(this));
		addGadget(new MorphAnath(this));
		addGadget(new MorphDana(this));
		addGadget(new MorphBardolf(this));
		addGadget(new MorphRowena(this));
		addGadget(new MorphLarissa(this));
		addGadget(new MorphBiff(this));
		addGadget(new MorphIvy(this));
		addGadget(new MorphGhast(this));
		addGadget(new MorphFrostGolem(this));
		addGadget(new MorphSkeleton(this));
		addGadget(new MorphOcelot(this));
		addGadget(new MorphWolf(this));

		// Mounts
		addGadget(new MountUndead(this));
		addGadget(new MountFrost(this));
		addGadget(new MountMule(this));
		addGadget(new MountDragon(this));
		addGadget(new MountSlime(this));
		addGadget(new MountCart(this));
		addGadget(new MountSpider(this));
		addGadget(new MountZombie(this));
		addGadget(new MountTitan(this));
		addGadget(new MountBabyReindeer(this));
		addGadget(new MountValentinesSheep(this));
		addGadget(new MountFreedomHorse(this));
		addGadget(new MountNightmareSteed(this));
		addGadget(new MountChicken(this));
		addGadget(new MountCake(this));
		addGadget(new MountLoveTrain(this));
		addGadget(new MountStPatricksHorse(this));
		addGadget(new MountPumpkin(this));
		addGadget(new MountSledge(this));

		// Particles
		addGadget(new ParticleFoot(this));
		addGadget(new ParticleEmerald(this));
		addGadget(new ParticleRain(this));
		addGadget(new ParticleBlood(this));
		addGadget(new ParticleEnchant(this));
		addGadget(new ParticleMusic(this));
		addGadget(new ParticlePartyTime(this));

		addGadget(new ParticleHeart(this));
		addGadget(new ParticleCandyCane(this));
		addGadget(new ParticleFrostLord(this));
		addGadget(new ParticleLegend(this));
		addGadget(new ParticleTitan(this));
		addGadget(new ParticleYinYang(this));
		addGadget(new ParticleKing(this, _castleManager));

		addGadget(new ParticleWingsPixie(this));
		addGadget(new ParticleWingsDemons(this));
		addGadget(new ParticleWingsInfernal(this));
		addGadget(new ParticleWingsAngel(this));
		addGadget(new ParticleWingsLove(this));
		addGadget(new ParticleFireRings(this));
		addGadget(new ParticleFairy(this));

		addGadget(new ParticleChristmasTree(this));
		addGadget(new ParticleCoalFumes(this));
		addGadget(new ParticleSpringHalo(this));
		addGadget(new ParticleWingsBee(this));

		addGadget(new ParticleFreedom(this));
		addGadget(new ParticleFreedomFireworks(this));
		addGadget(new ParticleStarSpangled(this));
		addGadget(new ParticleAuraNiceness(this));
		addGadget(new ParticleCanadian(this));

		addGadget(new ParticleHalloween(this));
		addGadget(new ParticleWingsChristmas(this));
		addGadget(new ParticleBlizzard(this));
		addGadget(new ParticleFidgetSpinner(this));
		addGadget(new ParticlePumpkinShield(this));
		addGadget(new ParticleRainbow(this, _achievementManager));
		addGadget(new ParticleMacawWings(this));
		addGadget(new ParticleChickenWings(this));
		addGadget(new ParticleEnderDragonWings(this));
		addGadget(new ParticleFoxTail(this));
		addGadget(new ParticleJetPack(this));
		addGadget(new ParticleCape(this));
		addGadget(new ParticleLegendaryHero(this));
		addGadget(new ParticleWolfTail(this));
		addGadget(new ParticleWitchsCure(this));
		addGadget(new ParticleRainbowTrail(this));
		addGadget(new ParticleDeepSeaSwirl(this));
		addGadget(new ParticleInfused(this));
		addGadget(new ParticleFiveYear(this));

		// Arrow Trails
		addGadget(new ArrowTrailFrostLord(this));
		addGadget(new ArrowTrailTitan(this));
		addGadget(new ArrowTrailCandyCane(this));
		addGadget(new ArrowTrailConfetti(this));
		addGadget(new ArrowTrailCupid(this));
		addGadget(new ArrowTrailEmerald(this));
		addGadget(new ArrowTrailShadow(this));
		addGadget(new ArrowTrailEnchant(this));
		addGadget(new ArrowTrailStorm(this));
		addGadget(new ArrowTrailBlood(this));
		addGadget(new ArrowTrailMusic(this));
		addGadget(new ArrowTrailFreedom(this));
		addGadget(new ArrowTrailHalloween(this));
		addGadget(new ArrowTrailSpring(this));
		addGadget(new ArrowTrailRedWhite(this));
		addGadget(new ArrowTrailPresent(this));
		addGadget(new ArrowTrailRainbow(this, _achievementManager));
		addGadget(new ArrowTrailLegend(this));
		addGadget(new ArrowTrailBalance(this));

		// Death Effect
		addGadget(new DeathFrostLord(this));
		addGadget(new DeathTitan(this));
		addGadget(new DeathCandyCane(this));
		addGadget(new DeathPinataBurst(this));
		addGadget(new DeathCupidsBrokenHeart(this));
		addGadget(new DeathEmerald(this));
		addGadget(new DeathShadow(this));
		addGadget(new DeathEnchant(this));
		addGadget(new DeathStorm(this));
		addGadget(new DeathBlood(this));
		addGadget(new DeathMusic(this));
		addGadget(new DeathFreedom(this));
		addGadget(new DeathPresentDanger(this));
		addGadget(new DeathSpring(this));
		addGadget(new DeathMapleLeaf(this));
		addGadget(new DeathHalloween(this));
		addGadget(new DeathRainbow(this, _achievementManager));
		addGadget(new DeathLegend(this));
		addGadget(new DeathBalance(this));

		// Double Jump
		addGadget(new DoubleJumpFrostLord(this));
		addGadget(new DoubleJumpTitan(this));
		addGadget(new DoubleJumpCandyCane(this));
		addGadget(new DoubleJumpFirecracker(this));
		addGadget(new DoubleJumpCupidsWings(this));
		addGadget(new DoubleJumpEmerald(this));
		addGadget(new DoubleJumpShadow(this));
		addGadget(new DoubleJumpEnchant(this));
		addGadget(new DoubleJumpStorm(this));
		addGadget(new DoubleJumpBlood(this));
		addGadget(new DoubleJumpMusic(this));
		addGadget(new DoubleJumpFreedom(this));
		addGadget(new DoubleJumpHalloween(this));
		addGadget(new DoubleJumpSpring(this));
		addGadget(new DoubleJumpMaple(this));
		addGadget(new DoubleJumpPresent(this));
		addGadget(new DoubleJumpRainbow(this, _achievementManager));
		addGadget(new DoubleJumpLegend(this));
		addGadget(new DoubleJumpBalance(this));

		// Hat
		for (HatType hatType : HatType.values())
		{
			addGadget(new HatItem(this, hatType));
		}

		//Win Effects
		addGadget(new WinEffectPodium(this));
		addGadget(new WinEffectLogo(this));
		addGadget(new WinEffectMrPunchMan(this));
		addGadget(new WinEffectFlames(this));
		addGadget(new WinEffectSnowTrails(this));
		addGadget(new WinEffectBabyChicken(this));
		addGadget(new WinEffectLightningStrike(this));
		addGadget(new WinEffectRiseOfTheElderGuardian(this));
		addGadget(new WinEffectLavaTrap(this));
		addGadget(new WinEffectHalloween(this));
		addGadget(new WinEffectWinterWarfare(this));
		addGadget(new WinEffectLoveIsABattlefield(this));
		addGadget(new WinEffectTornado(this));
		addGadget(new WinEffectEarthquake(this));
		addGadget(new WinEffectPartyAnimal(this));

		// Rank based win effects
		addGadget(new WinEffectRankUltra(this));
		addGadget(new WinEffectRankHero(this));
		addGadget(new WinEffectRankLegend(this));
		addGadget(new WinEffectRankTitan(this));
		addGadget(new WinEffectRankEternal(this));

		// Music
		addGadget(new MusicGadget(this, "13 Disc", new String[]{""}, -2, 2256, 178000));
		addGadget(new MusicGadget(this, "Cat Disc", new String[]{""}, -2, 2257, 185000));
		addGadget(new MusicGadget(this, "Blocks Disc", new String[]{""}, -2, 2258, 345000));
		addGadget(new MusicGadget(this, "Chirp Disc", new String[]{""}, -2, 2259, 185000));
		addGadget(new MusicGadget(this, "Far Disc", new String[]{""}, -2, 2260, 174000));
		addGadget(new MusicGadget(this, "Mall Disc", new String[]{""}, -2, 2261, 197000));
		addGadget(new MusicGadget(this, "Mellohi Disc", new String[]{""}, -2, 2262, 96000));
		addGadget(new MusicGadget(this, "Stal Disc", new String[]{""}, -2, 2263, 150000));
		addGadget(new MusicGadget(this, "Strad Disc", new String[]{""}, -2, 2264, 188000));
		addGadget(new MusicGadget(this, "Ward Disc", new String[]{""}, -2, 2265, 251000));
		addGadget(new MusicGadget(this, "Wait Disc", new String[]{""}, -2, 2267, 238000));

		// Balloons
		for (BalloonType balloonType : BalloonType.values())
		{
			addGadget(new BalloonGadget(this, balloonType));
		}

		// TAUNTS!!!
		addGadget(new EternalTaunt(this));
		addGadget(new BlowAKissTaunt(this));
		addGadget(new RainbowTaunt(this));
		addGadget(new InfernalTaunt(this));
		addGadget(new FrostBreathTaunt(this));
		addGadget(new EmojiTaunt(this));
		addGadget(new EasyModeTaunt(this));
		addGadget(new ChickenTaunt(this));

		// Flags
		addGadget(new FlagGadget(this, FlagType.CANADA));
		addGadget(new FlagGadget(this, FlagType.USA));
		addGadget(new FlagGadget(this, FlagType.MINEPLEX));
		addGadget(new FlagGadget(this, FlagType.RUDOLPH));
		addGadget(new FlagGadget(this, FlagType.CHRISTMAS_TREE));
		addGadget(new FlagGadget(this, FlagType.PRESENT));
		addGadget(new FlagGadget(this, FlagType.WREATH));
		addGadget(new FlagGadget(this, FlagType.SNOW_FLAKE));
		addGadget(new FlagGadget(this, FlagType.HEART));

		// Kit Selectors
		addGadget(new WaterWingsKitSelector(this));
		addGadget(new HaloKitSelector(this));
		addGadget(new RainbowDanceKitSelector(this));
		addGadget(new RainCloudKitSelector(this));
		addGadget(new ShimmeringRingKitSelector(this));
		addGadget(new HalloweenKitSelector(this));

		for (SingleParticleKitSelector.SingleParticleSelectors singleParticleSelectors : SingleParticleKitSelector.SingleParticleSelectors.values())
		{
			Gadget gadget = singleParticleSelectors.getKitSelectorGadget(this);
			addGadget(gadget);
			_singleParticleSelectors.put(singleParticleSelectors, gadget);
		}

		for (WeaponNameType weaponNameType : WeaponNameType.values())
		{
			addGadget(new WeaponNameGadget(this, weaponNameType));
		}

		for (LevelPrefixType prefixType : LevelPrefixType.values())
		{
			addGadget(new LevelPrefixGadget(this, prefixType));
		}

		new GameCosmeticType(this, GameDisplay.MineStrike)
		{
			@Override
			public void addCategories()
			{
				new GameCosmeticCategory(this, "Pistol", new ItemStack(Material.NETHER_STALK), true)
				{
					@Override
					public void addGadgets()
					{
						for (MineStrikeSkin mineStrikeSkin : MineStrikeSkin.getByCategory(1))
						{
							addGameGadget(new GameModifierMineStrikeSkin(getManager(), this, mineStrikeSkin, CostConstants.FOUND_IN_MINESTRIKE_CHESTS));
						}
					}
				};
				new GameCosmeticCategory(this, "Shotgun", new ItemBuilder(Material.INK_SACK, (byte) 14).build(), true)
				{
					@Override
					public void addGadgets()
					{
						for (MineStrikeSkin mineStrikeSkin : MineStrikeSkin.getByCategory(2))
						{
							addGameGadget(new GameModifierMineStrikeSkin(getManager(), this, mineStrikeSkin, CostConstants.FOUND_IN_MINESTRIKE_CHESTS));
						}
					}
				};
				new GameCosmeticCategory(this, "SMG", new ItemStack(Material.INK_SACK), true)
				{
					@Override
					public void addGadgets()
					{
						for (MineStrikeSkin mineStrikeSkin : MineStrikeSkin.getByCategory(3))
						{
							addGameGadget(new GameModifierMineStrikeSkin(getManager(), this, mineStrikeSkin, CostConstants.FOUND_IN_MINESTRIKE_CHESTS));
						}
					}
				};
				new GameCosmeticCategory(this, "Rifle", new ItemBuilder(Material.INK_SACK, (byte) 7).build(), true)
				{
					@Override
					public void addGadgets()
					{
						for (MineStrikeSkin mineStrikeSkin : MineStrikeSkin.getByCategory(4))
						{
							addGameGadget(new GameModifierMineStrikeSkin(getManager(), this, mineStrikeSkin, CostConstants.FOUND_IN_MINESTRIKE_CHESTS));
						}
					}
				};
				new GameCosmeticCategory(this, "Sniper Rifle", new ItemStack(Material.SULPHUR), true)
				{
					@Override
					public void addGadgets()
					{
						for (MineStrikeSkin mineStrikeSkin : MineStrikeSkin.getByCategory(5))
						{
							addGameGadget(new GameModifierMineStrikeSkin(getManager(), this, mineStrikeSkin, CostConstants.FOUND_IN_MINESTRIKE_CHESTS));
						}
					}
				};
				new GameCosmeticCategory(this, "Knife", new ItemStack(Material.DIAMOND_SWORD), false)
				{
					@Override
					public void addGadgets()
					{
						for (MineStrikeSkin mineStrikeSkin : MineStrikeSkin.getByCategory(6))
						{
							addGameGadget(new GameModifierMineStrikeSkin(getManager(), this, mineStrikeSkin, CostConstants.FOUND_IN_MINESTRIKE_CHESTS));
						}
					}
				};
			}
		};

		new GameCosmeticType(this, GameDisplay.MOBA)
		{
			@Override
			public void addCategories()
			{
				new GameCosmeticCategory(this, "Hero Skins", SkinData.HATTORI.getSkull(), true)
				{
					@Override
					public void addGadgets()
					{
						HeroSkinGadget.getSkins().values().forEach(heroSkins -> heroSkins.forEach(skinData -> addGameGadget(new HeroSkinGadget(getManager(), this, skinData))));
					}

					@Override
					public GadgetPage getGadgetPage(GameCosmeticCategoryPage parent)
					{
						return new HeroSkinCategoryPage(parent.getPlugin(), parent.getShop(), parent.getClientManager(), parent.getDonationManager(), getCategoryName(), parent.getClient().GetPlayer(), parent);
					}
				};
				new GameCosmeticCategory(this, "Shop Morph", new ItemStack(Material.GOLD_INGOT), false)
				{
					@Override
					public void addGadgets()
					{
						for (ShopMorphType type : ShopMorphType.values())
						{
							addGameGadget(new ShopMorphGadget(getManager(), this, type));
						}
					}
				};
			}
		};

		for (GadgetType gadgetType : GadgetType.values())
		{
			_gadgets.putIfAbsent(gadgetType, new ArrayList<>());
		}

		for (Gadget gadget : getAllGadgets())
		{
			if (gadget.getYearMonth() != null)
			{
				_powerPlayGadgets.add(gadget);
			}
		}

		// Sort Power Play Gadgets in order of addition
		_powerPlayGadgets.sort((o1, o2) ->
		{
			if (o1.getYearMonth().isAfter(o2.getYearMonth()))
			{
				return 1;
			}
			else if (o1.getYearMonth().equals(o2.getYearMonth()))
			{
				return 0;
			}

			return -1;
		});
	}

	private void registerTrackers()
	{
		_missionManager.registerTrackers(new GadgetUseTracker(_missionManager));
	}

	public <T extends Gadget> T getGadget(Class<T> c)
	{
		for (GadgetType type : GadgetType.values())
		{
			for (Gadget gadget : getGadgets(type))
			{
				if (gadget.getClass().equals(c))
				{
					return (T) gadget;
				}
			}
		}
		return null;
	}

	public GadgetSet getGadgetSet(Class<? extends GadgetSet> c)
	{
		for (GadgetSet set : _sets)
		{
			if (set.getClass().equals(c)) return set;
		}
		return null;
	}

	private void addSet(GadgetSet set)
	{
		_sets.add(set);
	}

	public void addGadget(Gadget gadget)
	{
		_gadgets.computeIfAbsent(gadget.getGadgetType(), k -> new ArrayList<>()).add(gadget);
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event)
	{
		// Fixes win room speed bug
		event.getPlayer().setWalkSpeed(0.2f);
		event.getPlayer().setFlySpeed(0.1f);
	}

	@EventHandler
	public void statsLoaded(PlayerStatsLoadedEvent event)
	{
		_userGadgetPersistence.load(event.getPlayer());
	}

	public List<Gadget> getGadgets(GadgetType gadgetType)
	{
		return _gadgets.get(gadgetType);
	}

	public List<Gadget> getAllGadgets()
	{
		List<Gadget> gadgets = new ArrayList<>();
		for (GadgetType gadgetType : GadgetType.values())
		{
			gadgets.addAll(getGadgets(gadgetType));
		}
		return gadgets;
	}

	public List<Gadget> getPowerPlayGadgets()
	{
		return _powerPlayGadgets;
	}

	public Gadget getGadget(String name, GadgetType gadgetType)
	{
		List<Gadget> gadgets = getGadgets(gadgetType);
		for (Gadget gadget : gadgets)
		{
			if (gadget.getName().equalsIgnoreCase(name) || Arrays.asList(gadget.getAlternativePackageNames()).contains(name))
			{
				return gadget;
			}
		}
		return null;
	}

	public HatGadget getHatGadget(HatType type)
	{
		for (Gadget gadget : getGadgets(GadgetType.HAT))
		{
			if (gadget instanceof HatGadget)
			{
				HatGadget hatGadget = (HatGadget) gadget;
				if (type.equals(hatGadget.getHatType()))
					return hatGadget;
			}
		}
		return null;
	}

	public FlagGadget getFlagGadget(FlagType type)
	{
		for (Gadget gadget : getGadgets(GadgetType.FLAG))
		{
			if (gadget instanceof FlagGadget)
			{
				FlagGadget flagGadget = (FlagGadget) gadget;

				if (type.equals(flagGadget.getFlagType()))
				{
					return flagGadget;
				}
			}
		}
		return null;
	}

	public BalloonGadget getBalloonGadget(BalloonType balloonType)
	{
		return (BalloonGadget) getGadgets(GadgetType.BALLOON).stream()
				.filter(gadget -> ((BalloonGadget) gadget).getBalloonType() == balloonType)
				.findFirst()
				.orElse(null);
	}

	public WeaponNameGadget getWeaponNameGadget(WeaponNameType weaponNameType)
	{
		return (WeaponNameGadget) getGadgets(GadgetType.WEAPON_NAME).stream()
				.filter(gadget -> ((WeaponNameGadget) gadget).getWeaponNameType() == weaponNameType)
				.findFirst()
				.orElse(null);
	}

	public SingleParticleKitSelector getSingleParticleKitSelector(SingleParticleKitSelector.SingleParticleSelectors singleParticleSelectors)
	{
		return (SingleParticleKitSelector) _singleParticleSelectors.get(singleParticleSelectors);
	}

	// Disallows two armor gadgets in same slot.
	public void removeOutfit(Player player, ArmorSlot slot)
	{
		for (GadgetType gadgetType : _gadgets.keySet())
		{
			for (Gadget gadget : _gadgets.get(gadgetType))
			{
				if (gadget instanceof OutfitGadget)
				{
					OutfitGadget armor = (OutfitGadget) gadget;

					if (armor.getSlot() == slot)
					{
						armor.disableCustom(player, true);
					}
				}
			}
		}
	}

	public void removeGadgetType(Player player, GadgetType type)
	{
		List<Gadget> gadgets = _gadgets.get(type);
		if (gadgets == null) return;
		for (Gadget g : gadgets)
		{
			g.disable(player);
		}
	}

	public void removeGadgetType(Player player, GadgetType type, Gadget enabled)
	{
		List<Gadget> gadgets = _gadgets.get(type);
		if (gadgets == null) return;
		for (Gadget g : gadgets)
		{
			if (g != enabled)
				g.disable(player);
		}
	}

	public void disableAll()
	{
		_userGadgetPersistence.setEnabled(false);

		_gadgets.forEach((type, gadgets) ->
		{
			if (type.disableForGame())
			{
				gadgets.forEach(Gadget::disableForAll);
			}
		});
	}

	public void disableAll(Player player)
	{
		for (GadgetType gadgetType : _gadgets.keySet())
		{
			for (Gadget gadget : _gadgets.get(gadgetType))
			{
				gadget.disable(player);
			}
		}
	}

	public void disableAll(Player player, boolean winRooms)
	{
		if (winRooms)
		{
			for (Gadget gadget : _gadgets.get(GadgetType.PARTICLE))
			{
				gadget.disable(player, false);
			}
		}
		else
		{
			for (GadgetType gadgetType : _gadgets.keySet())
			{
				for (Gadget gadget : _gadgets.get(gadgetType))
				{
					gadget.disable(player, false);
				}
			}
		}
	}

	public void disableAll(Player player, List<String> dontDisable)
	{
		for (GadgetType gadgetType : _gadgets.keySet())
		{
			for (Gadget gadget : _gadgets.get(gadgetType))
			{
				if (dontDisable.contains(gadget.getName()))
					continue;

				gadget.disable(player);
			}
		}
	}

	public boolean selectLocation(Gadget gadget, Location location)
	{
		GadgetSelectLocationEvent event = new GadgetSelectLocationEvent(gadget, location);
		UtilServer.CallEvent(event);
		return !event.isCancelled();
	}

	public boolean selectBlocks(Gadget gadget, Block block)
	{
		// Use an ArrayList since removing is unsupported for singletonLists
		return selectBlocks(gadget, new ArrayList<>(Collections.singletonList(block)));
	}

	public boolean selectBlocks(Gadget gadget, Collection<Block> blocks)
	{
		GadgetBlockEvent event = new GadgetBlockEvent(gadget, blocks);
		UtilServer.CallEvent(event);

		if (event.getBlocks().isEmpty())
		{
			event.setCancelled(true);
		}

		return !event.isCancelled();
	}

	public boolean selectEntity(Gadget gadget, Entity entity)
	{
		GadgetCollideEntityEvent event = new GadgetCollideEntityEvent(gadget, entity);
		event.setCancelled(entity instanceof ArmorStand);
		UtilServer.CallEvent(event);
		return !event.isCancelled();
	}

	public void informNoUse(Player player)
	{
		player.sendMessage(F.main(getName(), "You cannot use that cosmetic in this area."));
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void doubleJump(PlayerToggleFlightEvent event)
	{
		playLeapEffect(event.getPlayer());
	}

	public void playLeapEffect(Player player)
	{
		GameMode gameMode = player.getGameMode();

		if (gameMode == GameMode.CREATIVE || gameMode == GameMode.SPECTATOR || UtilPlayer.isSpectator(player) || player.isFlying())
		{
			return;
		}

		if (!UtilServer.isHubServer())
		{
			_missionManager.incrementProgress(player, 1, MissionTrackerType.GAME_LEAP, null, null);
		}

		if (hideParticles())
		{
			return;
		}

		Gadget gadget = getActive(player, GadgetType.DOUBLE_JUMP);

		if (gadget != null)
		{
			((DoubleJumpEffectGadget) gadget).doEffect(player);
		}
	}

	public PetManager getPetManager()
	{
		return _petManager;
	}

	public CoreClientManager getClientManager()
	{
		return _clientManager;
	}

	public DonationManager getDonationManager()
	{
		return _donationManager;
	}

	public PreferencesManager getPreferencesManager()
	{
		return _preferencesManager;
	}

	public ProjectileManager getProjectileManager()
	{
		return _projectileManager;
	}

	public DisguiseManager getDisguiseManager()
	{
		return _disguiseManager;
	}

	public HologramManager getHologramManager()
	{
		return _hologramManager;
	}

	public InventoryManager getInventoryManager()
	{
		return _inventoryManager;
	}

	public BlockRestore getBlockRestore()
	{
		return _blockRestore;
	}

	public PacketHandler getPacketManager()
	{
		return _packetManager;
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void setMoving(PlayerMoveEvent event)
	{
		Location from = event.getFrom(), to = event.getTo();

		if (from.getX() == to.getX() && from.getY() == to.getY() && from.getZ() == to.getZ())
		{
			return;
		}

		_lastMove.put(event.getPlayer(), System.currentTimeMillis());
	}

	public boolean isMoving(Player player)
	{
		return !UtilEnt.isGrounded(player) || !UtilTime.elapsed(_lastMove.getOrDefault(player, Long.MAX_VALUE), 500);
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		event.getPlayer().setWalkSpeed(0.2f);
		event.getPlayer().setFlySpeed(0.1f);
		disableAll(event.getPlayer(), false);
		_lastMove.remove(event.getPlayer());
		_playerActiveGadgetMap.remove(event.getPlayer());
		event.getPlayer().setWalkSpeed(0.2f);
		event.getPlayer().setFlySpeed(0.1f);
		_soulManager.giveSoul(event.getPlayer());
	}

	@EventHandler
	public void death(PlayerDeathEvent event)
	{
		_lastMove.remove(event.getEntity());
	}

	public void setActive(Player player, Gadget gadget)
	{
		_playerActiveGadgetMap.computeIfAbsent(player, k -> new HashMap<>()).put(gadget.getGadgetType(), gadget);
	}

	public Gadget getActive(Player player, GadgetType gadgetType)
	{
		return _playerActiveGadgetMap.computeIfAbsent(player, k -> new HashMap<>()).get(gadgetType);
	}

	public void removeActive(Player player, Gadget gadget)
	{
		_playerActiveGadgetMap.computeIfAbsent(player, k -> new HashMap<>()).remove(gadget.getGadgetType());
	}

	public void setHideParticles(boolean b)
	{
		_hideParticles = b;
	}

	public boolean hideParticles()
	{
		return _hideParticles;
	}

	public void setShowWeaponNames(boolean showWeaponNames)
	{
		_showWeaponNames = showWeaponNames;
	}

	public boolean showWeaponNames()
	{
		return _showWeaponNames;
	}

	public void setActiveItemSlot(int i)
	{
		_activeItemSlot = i;
	}

	public int getActiveItemSlot()
	{
		return _activeItemSlot;
	}

	public void redisplayActiveItem(Player player)
	{
		for (Gadget gadget : _gadgets.get(GadgetType.ITEM))
		{
			if (gadget instanceof ItemGadget)
			{
				if (gadget.isActive(player))
				{
					((ItemGadget) gadget).ApplyItem(player, false);
				}
			}
		}
	}

	public boolean canPlaySongAt(Location location)
	{
		for (Gadget gadget : _gadgets.get(GadgetType.MUSIC_DISC))
		{
			if (gadget instanceof MusicGadget)
			{
				if (!((MusicGadget) gadget).canPlayAt(location))
				{
					return false;
				}
			}
		}

		return true;
	}

	public boolean isGadgetEnabled()
	{
		return _gadgetsEnabled;
	}

	public void toggleGadgetEnabled()
	{
		setGadgetEnabled(!_gadgetsEnabled);
	}

	public void setGadgetEnabled(boolean enabled)
	{
		if (_gadgetsEnabled != enabled)
		{
			_gadgetsEnabled = enabled;
			disableAll();
		}
	}

	@EventHandler
	public void chissMeow(PlayerToggleSneakEvent event)
	{
		if (event.getPlayer().isSneaking())
			return;

		if (event.getPlayer().getName().equalsIgnoreCase("Chiss"))
			event.getPlayer().getWorld().playSound(event.getPlayer().getLocation(), Sound.CAT_MEOW, 1f, 1f);

		if (event.getPlayer().getName().equalsIgnoreCase("defek7"))
			event.getPlayer().getWorld().playSound(event.getPlayer().getLocation(), Sound.WOLF_BARK, 1f, 1f);

		if (event.getPlayer().getName().equalsIgnoreCase("sterling_"))
			event.getPlayer().getWorld().playSound(event.getPlayer().getLocation(), Sound.PIG_IDLE, 1f, 1f);
	}

	@EventHandler
	public void onGadgetEnable(GadgetEnableEvent event)
	{
		if (!_gadgetsEnabled)
			event.setCancelled(true);
		Player player = event.getPlayer();
		if (_incognitoManager.Get(player).Hidden && event.getGadget().getGadgetType() == GadgetType.PARTICLE)
		{
			event.setCancelled(true);
			player.sendMessage(F.main("Cosmetics", "You cannot enable particles while vanished!"));
		}
		if (event.getGadget().getGadgetType() == GadgetType.MORPH)
		{
			if (event.getGadget() instanceof MorphDinnerbone)
			{
				if (getActive(player, GadgetType.MOUNT) != null)
				{
					event.setCancelled(true);
					UtilPlayer.message(player, F.main("Cosmetics", "You cannot morph into " + event.getGadget().getName() + " with an active mount!"));
				}
			}
		}
	}

	@EventHandler
	private void saveGadget(GadgetChangeEvent event)
	{
		Gadget gadget = event.getGadget();

		if (gadget != null)
		{
			_userGadgetPersistence.save(event.getPlayer(), gadget, event.getGadgetState() == GadgetState.ENABLED);
		}
	}

	@EventHandler
	public void onVanish(IncognitoStatusChangeEvent event)
	{
		if (event.getNewState())
		{
			for (Gadget gadget : getGadgets(GadgetType.PARTICLE))
			{
				if (gadget.isActive(event.getPlayer()))
				{
					gadget.disable(event.getPlayer());
				}
			}
		}
	}

	public UserGadgetPersistence getUserGadgetPersistence()
	{
		return _userGadgetPersistence;
	}

	@EventHandler
	public void checkPlayerSwim(PlayerMoveEvent event)
	{
		Player player = event.getPlayer();
		boolean inWater = UtilEnt.isInWater(player);

		if (inWater && _swimmingPlayers.add(player) || !inWater && _swimmingPlayers.remove(player))
		{
			UtilServer.CallEvent(new PlayerToggleSwimEvent(player, inWater));
		}
	}

	public SoulManager getSoulManager()
	{
		return _soulManager;
	}

	@EventHandler
	public void onTauntCommand(TauntCommandEvent event)
	{
		Player player = event.getPlayer();

		Gadget gadget = getActive(player, GadgetType.TAUNT);

		if (gadget == null)
		{
			event.setState(TauntCommandEvent.TauntState.NO_TAUNT);
			UtilPlayer.message(player, F.main("Taunt", event.getState().getMessage()));
			return;
		}

		if (!(gadget instanceof TauntGadget))
		{
			event.setState(TauntCommandEvent.TauntState.NO_TAUNT);
			UtilPlayer.message(player, F.main("Taunt", event.getState().getMessage()));
			return;
		}

		TauntGadget taunt = (TauntGadget) gadget;

		if (!event.isGameInProgress() && event.getState().equals(TauntCommandEvent.TauntState.NONE))
			event.setState(TauntCommandEvent.TauntState.NOT_IN_GAME);

		if (taunt.isGameDisabled(event.getGameDisplay()) && event.getState().equals(TauntCommandEvent.TauntState.NONE))
			event.setState(TauntCommandEvent.TauntState.GAME_DISABLED);

		if (!event.isAlive() && event.getState().equals(TauntCommandEvent.TauntState.NONE))
			event.setState(TauntCommandEvent.TauntState.NOT_ALIVE);

		if (event.isSpectator() && event.getState().equals(TauntCommandEvent.TauntState.NONE))
			event.setState(TauntCommandEvent.TauntState.SPECTATOR);

		if (event.isInPvp(taunt.getPvpCooldown()) && !taunt.canPlayWithPvp()
				&& event.getState().equals(TauntCommandEvent.TauntState.NONE))
			event.setState(TauntCommandEvent.TauntState.PVP);

		if (event.getState() != TauntCommandEvent.TauntState.NONE)
		{
			UtilPlayer.message(player, F.main("Taunt", event.getState().getMessage()));
			return;
		}

		// LCastr0 is an idiot so this is the only solution
		if (Recharge.Instance.usable(player, taunt.getName()))
		{
			_missionManager.incrementProgress(player, 1, MissionTrackerType.GAME_TAUNT, null, gadget.getClass());
		}

		taunt.start(player);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void anvilUse(InventoryClickEvent event)
	{
		if (event.isCancelled() || !showWeaponNames())
		{
			return;
		}

		Inventory inventory = event.getClickedInventory();

		if (inventory == null || !(inventory instanceof AnvilInventory) || event.getRawSlot() != 2)
		{
			return;
		}

		ItemStack itemStack = inventory.getItem(event.getRawSlot());

		if (itemStack == null || !itemStack.hasItemMeta() || !itemStack.getItemMeta().hasDisplayName())
		{
			return;
		}

		event.setCancelled(true);

		Player player = (Player) event.getWhoClicked();

		player.playSound(player.getLocation(), Sound.ITEM_BREAK, 1, 1);
		player.sendMessage(F.main(_moduleName, "You cannot rename an item in an " + F.name("Anvil") + "."));
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void gameStart(CoreGameStartEvent event)
	{
		_gameIsLive = true;
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void gameStop(CoreGameStopEvent event)
	{
		_gameIsLive = false;
	}

	public boolean isGameLive()
	{
		return _gameIsLive;
	}

	public CastleManager getCastleManager()
	{
		return _castleManager;
	}

	public GameCosmeticManager getGameCosmeticManager()
	{
		return _gameCosmeticManager;
	}

	public AchievementManager getAchievementManager()
	{
		return _achievementManager;
	}

	public MissionManager getMissionManager()
	{
		return _missionManager;
	}
}
