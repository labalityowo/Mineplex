package mineplex.core.game.kit;

import java.util.Optional;

import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Blaze;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Guardian;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Horse.Variant;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.MagmaCube;
import org.bukkit.entity.Pig;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Skeleton.SkeletonType;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Snowman;
import org.bukkit.entity.Spider;
import org.bukkit.entity.Squid;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Witch;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Colorable;

import mineplex.core.Managers;
import mineplex.core.achievement.Achievement;
import mineplex.core.achievement.AchievementCategory;
import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilSkull;
import mineplex.core.common.util.UtilText;
import mineplex.core.common.util.UtilTime;
import mineplex.core.game.GameDisplay;
import mineplex.core.game.MineplexGameManager;
import mineplex.core.game.kit.upgrade.LinearUpgradeTree;
import mineplex.core.game.kit.upgrade.UpgradeTree;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.newnpc.NPC;
import mineplex.core.newnpc.SimpleNPC;

public enum GameKit
{

	/*
		The id passed in through the constructor should be local to that game, starting at 0.
		The id used in the database is derived from GameDisplay#getGameId * 100 + id.
		This makes assigning ids a lot easier while still allowing up to 100 kits per game.
	 */

	// Common
	NULL_PLAYER
			(
					0,
					GameDisplay.MOBA,
					"Player",
					null,
					noLore(),
					new KitEntityData<>
							(
									Pig.class,
									null
							),
					KitAvailability.Hide,
					0
			),

	NULL_SPACER
			(
					1,
					GameDisplay.MOBA,
					"Null",
					null,
					null,
					null,
					KitAvailability.Null,
					0
			),

	// Alien Invasion

	ALIEN_INVASION_PLAYER
			(
					0,
					GameDisplay.AlienInvasion,
					"Player",
					null,
					noLore(),
					new KitEntityData<>
							(
									Skeleton.class,
									new ItemStack(Material.DIAMOND_BARDING)
							)
			),

	// Bacon Brawl

	BACON_BRAWL_PIG
			(
					0,
					GameDisplay.BaconBrawl,
					"El Muchacho Pigo",
					"elmuchachopigo",
					new String[]
							{
									"Such a fat pig. Oink",
									C.blankLine,
									click(false, "Axe to " + C.cGreen + "Body Slam")
							},
					new KitEntityData<>
							(
									Pig.class,
									new ItemStack(Material.PORK)
							)
			),

	BACON_BRAWL_MAMA_PIG
			(
					1,
					GameDisplay.BaconBrawl,
					"Mama Piggles",
					"mamapiggles",
					new String[]
							{
									"Mama & Baby Piggles fight together",
									C.blankLine,
									click(false, "Axe to use " + C.cGreen + "Bacon Blast"),
							},
					new KitEntityData<>
							(
									Pig.class,
									new ItemStack(Material.PORK)
							),
					2000
			),

	BACON_BRAWL_SHEEP
			(
					2,
					GameDisplay.BaconBrawl,
					"Pig",
					"wolfpig",
					new String[]
							{
									"\"...Oink?\"",
									C.blankLine,
									click(false, "Axe to " + C.cGreen + "Cloak"),
									"Deal " + C.cGreen + "+250%" + C.cGray + " Knockback from behind opponents."
							},
					new KitEntityData<>
							(
									Sheep.class,
									createColoured(Material.WOOL, (byte) 6)
							),
					5000
			)
			{
				@Override
				protected void onEntitySpawn(LivingEntity entity)
				{
					((Colorable) entity).setColor(DyeColor.PINK);
				}
			},

	BACON_BRAWL_CHRIS_P_BACON
			(
					3,
					GameDisplay.BaconBrawl,
					"Chris P Bacon",
					null,
					new String[]
							{
									"Guess he burnt the bacon",
									C.blankLine,
									click(false, "Axe to launch " + C.cGreen + "Crispy Bacon"),
									"Players on fire take " + C.cGreen + "+30%" + C.cGray + " knockback."
							},
					new KitEntityData<>
							(
									Pig.class,
									new ItemStack(Material.GRILLED_PORK)
							),
					getAchievementsFor(AchievementCategory.BACON_BRAWL)
			)
			{
				@Override
				protected void onEntitySpawn(LivingEntity entity)
				{
					entity.setFireTicks(Integer.MAX_VALUE);
				}
			},

	// Barbarians

	BARBARIANS_BRUTE
			(
					0,
					GameDisplay.Barbarians,
					"Barbarian Brute",
					"barbarianbrute",
					new String[]
							{
									"A true barbarian, loves to kill people!"
							},
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.IRON_AXE),
									new ItemStack[]
											{
													new ItemStack(Material.IRON_BOOTS),
													new ItemStack(Material.IRON_LEGGINGS),
													new ItemStack(Material.IRON_CHESTPLATE),
													new ItemStack(Material.IRON_HELMET)
											}
							)
			),

	BARBARIANS_ARCHER
			(
					1,
					GameDisplay.Barbarians,
					"Barbarian Archer",
					"barbarianbrute",
					new String[]
							{
									"Uses some kind of less barbaric ranged weapon"
							},
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.BOW),
									new ItemStack[]
											{
													new ItemStack(Material.CHAINMAIL_BOOTS),
													new ItemStack(Material.CHAINMAIL_LEGGINGS),
													new ItemStack(Material.CHAINMAIL_CHESTPLATE),
													new ItemStack(Material.CHAINMAIL_HELMET)
											}
							),
					2000
			),

	BARBARIANS_BOMBER
			(
					2,
					GameDisplay.Barbarians,
					"Bomber",
					"barbarianbomber",
					new String[]
							{
									"Crazy bomb throwing barbarian. BOOM!"
							},
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.TNT),
									new ItemStack[]
											{
													new ItemStack(Material.CHAINMAIL_BOOTS),
													new ItemStack(Material.CHAINMAIL_LEGGINGS),
													new ItemStack(Material.CHAINMAIL_CHESTPLATE),
													new ItemStack(Material.CHAINMAIL_HELMET)
											}
							),
					5000
			),

	// Basket Ball

	BASKET_BALL_PLAYER
			(
					0,
					GameDisplay.Basketball,
					"Basketball Player",
					"bballer",
					new String[]
							{
									"A true Basketball Champion!",
									click(true, "to pass or do a layup"),
									click(false, "to make a distance shot"),
									click(true, "an opposing player to try and steal the ball")
							},
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.SLIME_BALL)
							)
			),

	// Bridges

	BRIDGES_APPLER
			(
					0,
					GameDisplay.Bridge,
					"Apple",
					"bridgeapple",
					new String[]
							{
									"Possess the rare skill of finding apples frequently!",
									C.blankLine,
									receiveItem("apple", 1, 10, 0),
									click(true, "the apple to throw it")
							},
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.APPLE)
							)
			),

	BRIDGES_BERSERKER
			(
					1,
					GameDisplay.Bridge,
					"Berserker",
					"bridgeberserker",
					new String[]
							{
									"Agile warrior trained in the ways of axe combat.",
									" ",
									"Begin with a Stone Axe",
									"Deal " + C.cGreen + "+1" + C.cGray + " damage using axes",
									click(false, " with your axe to use " + C.cGreen + "Berserker Leap")
							},
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.STONE_AXE)
							)
			),

	BRIDGES_BRAWLER
			(
					2,
					GameDisplay.Bridge,
					"Brawler",
					"bridgebrawler",
					new String[]
							{
									"Giant and muscular, easily smacks others around.",
									" ",
									"Begin with an Iron Sword",
									"Take " + C.cGreen + "85%" + C.cGray + " knockback",
									"Deal " + C.cGreen + "115%" + C.cGray + " knockback",
									"Take " + C.cGreen + "-1" + C.cGray + " damage from all attacks"
							},
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.IRON_SWORD)
							),
					2000
			),

	BRIDGES_ARCHER
			(
					3,
					GameDisplay.Bridge,
					"Archer",
					"bridgearcher",
					new String[]
							{
									"Highly trained with a bow, probably an elf or something...",
									" ",
									"Begin the game with a Bow",
									receiveArrow(1, 20, 4),
									"Charge Bow to use " + C.cGreen + "Barrage"
							},
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.BOW)
							),
					2000
			),

	BRIDGES_MINER
			(
					4,
					GameDisplay.Bridge,
					"Miner",
					"bridgeminer",
					new String[]
							{
									"Master of ore prospecting and digging quickly.",
									" ",
									"Begin with a Stone Pickaxe",
									"Receive " + C.cGreen + "Haste 2",
									click(false, "your pickaxe to " + C.cGreen + "find ores"),
							},
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.STONE_PICKAXE)
							),
					5000
			),

	BRIDGES_BOMBER
			(
					5,
					GameDisplay.Bridge,
					"Bomber",
					"bridgebomber",
					new String[]
							{
									"Crazy bomb throwing guy.",
									" ",
									receiveItem("TNT", 1, 25, 2),
									C.cYellow + "Click" + C.cGray + " to throw TNT"
							},
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.TNT)
							),
					5000
			),

	BRIDGES_DESTRUCTOR
			(
					6,
					GameDisplay.Bridge,
					"Destructor",
					"bridgedesctructor",
					new String[]
							{
									"Has the ability to make the world crumble!",
									" ",
									receiveItem("Seismic Charge", 1, 40, 2),
									C.cYellow + "Right-Click" + C.cGray + " with Seismic Charge to " + C.cGreen + "Throw Seismic Charge",
									C.blankLine,
									C.cRed + "Seismic Charges begin after bridges drop"
							},
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.ENDER_PEARL)
							),
					getAchievementsFor(AchievementCategory.BRIDGES)
			),

	// Build (Master Builders)
	BUILD_BUILDER
			(
					0,
					GameDisplay.Build,
					"Builder",
					"buildbuilder",
					new String[]
							{
									"Can I build it!? Yes I can!"
							},
					new KitEntityData<>
							(
									Skeleton.class,
									new ItemStack(Material.WOOD)
							)
			),

	// Cake Wars

	CAKE_WARS_WARRIOR
			(
					0,
					GameDisplay.CakeWars4,
					"Warrior",
					"cakewarswarrior",
					new String[]
							{
									"Slay others to absorb their heart!",
									C.blankLine,
									"Killing a player will give you " + heart(3)
							},
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.IRON_SWORD)
							)
			),

	CAKE_WARS_ARCHER
			(
					1,
					GameDisplay.CakeWars4,
					"Archer",
					"cakewarsarcher",
					new String[]
							{
									"Fully charged arrows break blocks!",
									receiveItem("Arrow", 1) + " for each kill.",
									C.blankLine,
									receiveArrow(1, 6, 3)
							},
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.BOW)
							),
					2000
			),

	CAKE_WARS_BUILDER
			(
					2,
					GameDisplay.CakeWars4,
					"Builder",
					"cakewarsbuilder",
					new String[]
							{
									"Get extra blocks to build with!",
									C.blankLine,
									receiveItem("Knitted Wool", 1, 4, 32),
									receiveItem("Knitted Platform", 1, 10, 5),
							},
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.WOOL)
							),
					2000
			),

	CAKE_WARS_FROSTING
			(
					3,
					GameDisplay.CakeWars4,
					"Frosting",
					"cakewarsfrosting",
					new String[]
							{
									"Every cake needs some frosting.",
									C.blankLine,
									receiveItem("Snowball", 1, 6, 3),
									C.cGray + "Your " + C.cGreen + "Snowballs" + C.cGray + " slow enemies for " + C.cGreen + "2.5 seconds"
							},
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.SNOW_BALL)
							),
					getAchievementsFor(AchievementCategory.CAKE_WARS)
			),

	// Castle Assault

	CASTLE_ASSAULT_ALCHEMIST
			(
					0,
					GameDisplay.CastleAssault,
					"Alchemist",
					"castleassaultalchemist",
					new String[]
							{
									C.cGray + "Diamond Sword",
									C.cGray + "Diamond Helmet, Iron Chestplate, Iron Leggings, Diamond Boots",
									C.cGray + "Speed I Potion",
									C.cGreenB + "Passive Ability:",
									C.cGreen + "Netherborne: Permanent Fire Resistance"
							},
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.POTION)
							)
			),

	CASTLE_ASSAULT_ARCHER
			(
					1,
					GameDisplay.CastleAssault,
					"Archer",
					"castleassaultarcher",
					new String[]
							{
									C.cGray + "Diamond Sword",
									C.cGray + "Bow",
									C.cGray + "Diamond Helmet, Iron Chestplate, Iron Leggings, Diamond Boots",
									C.cGray + "10 Fletched Arrows",
									C.cGreenB + "Starting Ability:",
									C.cGreen + "Fletcher: Obtain 1 Fletched Arrow every 6 seconds (Max of 10)"
							},
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.BOW)
							)
			),

	CASTLE_ASSAULT_DEMO
			(
					2,
					GameDisplay.CastleAssault,
					"Demolitionist",
					"castleassaultdemolitionist",
					new String[]
							{
									C.cGray + "Diamond Sword, Flint and Steel",
									C.cGray + "Diamond Helmet, Iron Chestplate, Iron Leggings, Diamond Boots",
									C.cGray + "Blast Protection IV on all Armor",
									C.cGray + "2 Throwing TNT",
									C.cGreenB + "Passive Ability:",
									C.cGreen + "3 Block Range Increase on TNT Damage",
									C.cGreenB + "Starting Ability:",
									C.cGreen + "Bombmaker: Obtain 1 Throwing TNT every 10 seconds (Max of 2)"
							},
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.TNT)
							)
			),

	CASTLE_ASSAULT_FIGHTER
			(
					3,
					GameDisplay.CastleAssault,
					"Fighter",
					"castleassaultfighter",
					new String[]
							{
									C.cGray + "Diamond Sword",
									C.cGray + "1 Golden Applegate",
									C.cGray + "Diamond Helmet, Iron Chestplate, Iron Leggings, Diamond Boots",
									C.cGreenB + "Starting Ability:",
									C.cGreen + "Bloodlust: Deal half a heart more damage for 3 seconds after killing an enemy"
							},
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.DIAMOND_SWORD)
							)
			),

	CASTLE_ASSAULT_TANK
			(
					4,
					GameDisplay.CastleAssault,
					"Tank",
					"castleassaulttank",
					new String[]
							{
									C.cGray + "Diamond Sword",
									C.cGray + "Diamond Helmet, Iron Chestplate, Iron Leggings, Diamond Boots",
									C.cGray + "Protection I on Iron Chestplate"
							},
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.DIAMOND_CHESTPLATE)
							)
			),

	// Castle Siege

	CASTLE_SIEGE_WOLF
			(
					0,
					GameDisplay.CastleSiege,
					"Castle Wolf",
					"castlewolf",
					new String[]
							{
									"My bark is as strong as my bite.",
									C.blankLine,
									C.cGrayB + "THIS KIT IS GIVEN TO DEFENDERS WHEN THEY " + C.cRedB + "DIE"
							},
					new KitEntityData<>
							(
									Wolf.class,
									new ItemStack(Material.BONE)
							)
			),

	CASTLE_SIEGE_HUMAN_MARKSMAN
			(
					1,
					GameDisplay.CastleSiege,
					"Castle Marksman",
					"castlemarksman",
					new String[]
							{
									"Arms steady; fire at will.",
									C.blankLine,
									receiveArrow(1, 2, 4),
									"Charge your Bow to use " + C.cGreen + "Barrage"
							},
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.BOW),
									new ItemStack[]
											{
													new ItemStack(Material.CHAINMAIL_BOOTS),
													new ItemStack(Material.CHAINMAIL_LEGGINGS),
													new ItemStack(Material.CHAINMAIL_CHESTPLATE),
													new ItemStack(Material.CHAINMAIL_HELMET)
											}
							)
			),

	CASTLE_SIEGE_HUMAN_KNIGHT
			(
					2,
					GameDisplay.CastleSiege,
					"Castle Knight",
					"castleknight",
					new String[]
							{
									"Master fencer at your service; both the sport, and the job..",
									C.blankLine,
									receiveItem("Fence", 1, 40, 2),
									"Take " + C.cGreen + "85%" + C.cGray + " knockback",
									"Deal " + C.cGreen + "115%" + C.cGray + " knockback",
							},
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.IRON_SWORD),
									new ItemStack[]
											{
													new ItemStack(Material.IRON_BOOTS),
													new ItemStack(Material.IRON_LEGGINGS),
													new ItemStack(Material.IRON_CHESTPLATE),
													new ItemStack(Material.IRON_HELMET)
											}
							),
					2000
			),

	CASTLE_SIEGE_HUMAN_PALADIN
			(
					3,
					GameDisplay.CastleSiege,
					"Castle Paladin",
					"castlepaladin",
					new String[]
							{
									"Stand your ground, we got this!",
									C.blankLine,
									click(false, "your sword") + " to give buffs to nearby humans and wolves",
									"Wolves receive " + C.cGreen + "Speed I" + C.cGray + " and Humans receive " + C.cGreen + "Resistance I",
									"Take " + C.cGreen + "10%" + C.cGray + " less damage from attacks",
							},
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.STONE_SWORD),
									new ItemStack[]
											{
													new ItemStack(Material.GOLD_BOOTS),
													new ItemStack(Material.GOLD_LEGGINGS),
													new ItemStack(Material.GOLD_CHESTPLATE),
													new ItemStack(Material.GOLD_HELMET)
											}
							),
					getAchievementsFor(AchievementCategory.CASTLE_SIEGE)
			),

	CASTLE_SIEGE_UNDEAD_GHOUL
			(
					4,
					GameDisplay.CastleSiege,
					"Undead Ghoul",
					"undeadghoul",
					new String[]
							{
									"The walls thought they were too high for me; I proved them wrong.",
									C.blankLine,
									click(false, "your axe to use " + C.cGreen + "Ghoul Leap"),
							},
					new KitEntityData<>
							(
									PigZombie.class,
									new ItemStack(Material.STONE_AXE)
							)
			),

	CASTLE_SIEGE_UNDEAD_ARCHER
			(
					5,
					GameDisplay.CastleSiege,
					"Undead Archer",
					"undeadarcher",
					new String[]
							{
									"I've got a bone to pick with you.",
									C.blankLine,
									"You can pickup arrows shot from Defenders",
									"Take " + C.cGreen + "-1" + C.cGray + " damage from attacks",
									receiveArrow(1, 8, 2)
							},
					new KitEntityData<>
							(
									Skeleton.class,
									new ItemStack(Material.BOW)
							),
					2000
			),

	CASTLE_SIEGE_UNDEAD_ZOMBIE
			(
					6,
					GameDisplay.CastleSiege,
					"Undead Zombie",
					"undeadzombie",
					new String[]
							{
									"Keep those arrows coming.",
									C.blankLine,
									"Receive " + C.cGreen + "Regeneration III"
							},
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.STONE_AXE)
							),
					5000
			),

	CASTLE_SIEGE_UNDEAD_SUMMONER
			(
					7,
					GameDisplay.CastleSiege,
					"Undead Summoner",
					"undeadsummoner",
					new String[]
							{
									"Say hello to my little friend.",
									C.blankLine,
									click(false, "your eggs to spawn undead mobs to help you fight")
							},
					new KitEntityData<>
							(
									Skeleton.class,
									new ItemStack(Material.POTION),
									SkeletonType.WITHER.ordinal()
							),
					getAchievementsFor(AchievementCategory.CASTLE_SIEGE)
			),

	// Champions

	CHAMPIONS_BRUTE
			(
					0,
					GameDisplay.ChampionsDominate,
					"Brute",
					null,
					new String[]
							{
									"A giant of a man, able to smash and",
									"destroy anything in his path.",
									"",
									"Takes additional damage to counter",
									"the strength of Diamond Armor."
							},
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.IRON_SWORD),
									new ItemStack[]
											{
													new ItemStack(Material.DIAMOND_BOOTS),
													new ItemStack(Material.DIAMOND_LEGGINGS),
													new ItemStack(Material.DIAMOND_CHESTPLATE),
													new ItemStack(Material.DIAMOND_HELMET)
											}
							)
			),

	CHAMPIONS_ARCHER
			(
					1,
					GameDisplay.ChampionsDominate,
					"Ranger",
					null,
					new String[]
							{
									"Uses mastery of archery and kinship with",
									"nature to defeat opponents."
							},
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.IRON_SWORD),
									new ItemStack[]
											{
													new ItemStack(Material.CHAINMAIL_BOOTS),
													new ItemStack(Material.CHAINMAIL_LEGGINGS),
													new ItemStack(Material.CHAINMAIL_CHESTPLATE),
													new ItemStack(Material.CHAINMAIL_HELMET)
											}
							)
			),

	CHAMPIONS_KNIGHT
			(
					2,
					GameDisplay.ChampionsDominate,
					"Knight",
					null,
					new String[]
							{
									"Knight of the realm, extremely good at",
									"defending and surviving."
							},
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.IRON_SWORD),
									new ItemStack[]
											{
													new ItemStack(Material.IRON_BOOTS),
													new ItemStack(Material.IRON_LEGGINGS),
													new ItemStack(Material.IRON_CHESTPLATE),
													new ItemStack(Material.IRON_HELMET)
											}
							)
			),

	CHAMPIONS_MAGE
			(
					3,
					GameDisplay.ChampionsDominate,
					"Mage",
					null,
					new String[]
							{
									"Trained in the ways of magic, the mage",
									"can unleash hell upon his opponents."
							},
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.IRON_SWORD),
									new ItemStack[]
											{
													new ItemStack(Material.GOLD_BOOTS),
													new ItemStack(Material.GOLD_LEGGINGS),
													new ItemStack(Material.GOLD_CHESTPLATE),
													new ItemStack(Material.GOLD_HELMET)
											}
							)
			),

	CHAMPIONS_ASSASSIN
			(
					4,
					GameDisplay.ChampionsDominate,
					"Assassin",
					null,
					new String[]
							{
									"Extremely agile warrior, trained in",
									"the mystic arts of assassination.",
									"",
									"Attack Damage increased by 1",
									"Fall Damage reduced by 1",
									"Permanent Speed 2",
							},
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.IRON_SWORD),
									new ItemStack[]
											{
													new ItemStack(Material.LEATHER_BOOTS),
													new ItemStack(Material.LEATHER_LEGGINGS),
													new ItemStack(Material.LEATHER_CHESTPLATE),
													new ItemStack(Material.LEATHER_HELMET)
											}
							)
			),

	// Christmas Chaos

	CHRISTMAS_CHAOS_PLAYER
			(
					0,
					GameDisplay.Christmas,
					"Santa's Helper",
					null,
					new String[]
							{
									"Help Santa retrieve the lost presents!"
							},
					new KitEntityData<>
							(
									Skeleton.class,
									new ItemStack(Material.IRON_SWORD),
									new ItemStack[]
											{
													new ItemBuilder(Material.LEATHER_HELMET).setColor(Color.RED).setUnbreakable(true).build(),
													new ItemBuilder(Material.LEATHER_CHESTPLATE).setColor(Color.RED).build(),
													new ItemBuilder(Material.LEATHER_LEGGINGS).setColor(Color.RED).build(),
													new ItemBuilder(Material.LEATHER_BOOTS).setColor(Color.BLACK).build()
											}
							)
			),

	// Death Tag

	DEATH_TAG_BASHER
			(
					0,
					GameDisplay.DeathTag,
					"Runner Basher",
					"runnerbasher",
					new String[]
							{
									"Your attacks cripple Chasers briefly!",
									C.blankLine,
									"Deal " + C.cGreen + "Slowness III" + C.cGray + " to opponents per attack."
							},
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.IRON_SWORD)
							)
			),

	DEATH_TAG_ARCHER
			(
					1,
					GameDisplay.DeathTag,
					"Runner Archer",
					"runnerarcher",
					new String[]
							{
									"Fight off the Chasers with Arrows!",
									C.blankLine,
									receiveArrow(1, 2, 2),
									"Deal " + C.cGreen + "300%" + C.cGray + " knockback with arrows"
							},
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.BOW)
							),
					2000
			),

	DEATH_TAG_TRAITOR
			(
					2,
					GameDisplay.DeathTag,
					"Runner Traitor",
					"runnertraitor",
					new String[]
							{
									"You can deal knockback to other runners!",
									C.blankLine,
									"Deal " + C.cGreen + "+80%" + C.cGray + " knockback per hit"
							},
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.IRON_AXE)
							),
					5000
			),

	DEATH_TAG_ALPHA_CHASER
			(
					3,
					GameDisplay.DeathTag,
					"Alpha Chaser",
					"alphachaser",
					new String[]
							{
									"Tag! You're it...",
									C.blankLine,
									"Deal " + C.cGreen + 6 + C.cGray + " damage per attack",
									"Take " + C.cGreen + "50%" + C.cGray + " knockback",
									"Take " + C.cGreen + "-4" + C.cGray + " damage from all attacks"
							},
					new KitEntityData<>
							(
									Skeleton.class,
									new ItemStack(Material.IRON_AXE),
									SkeletonType.WITHER.ordinal()
							)
			),

	DEATH_TAG_CHASER
			(
					4,
					GameDisplay.DeathTag,
					"Chaser",
					"chaser",
					new String[]
							{
									"Tag! You're it..."
							},
					new KitEntityData<>
							(
									Skeleton.class,
									new ItemStack(Material.IRON_AXE)
							),
					KitAvailability.Hide,
					0
			),

	// Dragon Escape

	DRAGON_ESCAPE_LEAPER
			(
					0,
					GameDisplay.DragonEscape,
					"Jumper",
					"dragonescapejumper",
					new String[]
							{
									"You get twice as many leaps!",
									C.blankLine,
									leap()
							},
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.IRON_AXE)
							)
			),

	DRAGON_ESCAPE_DISRUPTOR
			(
					1,
					GameDisplay.DragonEscape,
					"Disruptor",
					"dragonescapedisruptor",
					new String[]
							{
									"Place mini-explosives to stop other players!",
									C.blankLine,
									receiveItem("Disruptor", 1, 8, 2),
									C.cYellow + "Click" + C.cGray + " with TNT to " + C.cGreen + "Place Disruptor"
							},
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.TNT)
							),
					2000
			),

	DRAGON_ESCAPE_WARPER
			(
					2,
					GameDisplay.DragonEscape,
					"Warper",
					"dragonescapewarper",
					new String[]
							{
									"Use your Enderpearl to instantly warp",
									"to the player in front of you!",
									C.blankLine,
									leap()
							},
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.ENDER_PEARL)
							),
					5000
			),

	DRAGON_ESCAPE_DIGGER
			(
					3,
					GameDisplay.DragonEscape,
					"Digger",
					"dragonescapedigger",
					new String[]
							{
									"Dig yourself a shortcut, and use the",
									"blocks to create another shortcut!",
									C.blankLine,
									"Does not have any Leaps.",
									"Starts with a " + C.cGreen + "Diamond Pickaxe"
							},
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.DIAMOND_PICKAXE)
							),
					getAchievementsFor(AchievementCategory.DRAGON_ESCAPE)
			),

	// Dragons

	DRAGONS_COWARD
			(
					0,
					GameDisplay.Dragons,
					"Coward",
					"dragonscoward",
					new String[]
							{
									"There's no shame in being afraid of dragons...",
									C.blankLine,
									leap()
							},
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.IRON_AXE),
									new ItemStack[]
											{
													new ItemStack(Material.LEATHER_BOOTS),
													new ItemStack(Material.LEATHER_LEGGINGS),
													new ItemStack(Material.LEATHER_CHESTPLATE),
													new ItemStack(Material.LEATHER_HELMET)
											}
							)
			),

	DRAGONS_MARKSMAN
			(
					1,
					GameDisplay.Dragons,
					"Marksman",
					"dragonsmarksman",
					new String[]
							{
									"Arrows send dragons running to the sky!",
									C.blankLine,
									receiveArrow(1, 4, 4),
									"Charge Bow to use " + C.cGreen + "Barrage"
							},
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.BOW),
									new ItemStack[]
											{
													new ItemStack(Material.CHAINMAIL_BOOTS),
													new ItemStack(Material.CHAINMAIL_LEGGINGS),
													new ItemStack(Material.CHAINMAIL_CHESTPLATE),
													new ItemStack(Material.CHAINMAIL_HELMET)
											}
							),
					2000
			),

	DRAGONS_PYROTECHNIC
			(
					2,
					GameDisplay.Dragons,
					"Pyrotechnic",
					"dragonspyrotechnic",
					new String[]
							{
									"Dragons love sparklers!!",
									C.blankLine,
									receiveItem("Sparkler", 1, 25, 2),
									C.cYellow + "Click" + C.cGray + " with Sparkler to " + C.cGreen + "Throw Sparkler"
							},
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.EMERALD),
									new ItemStack[]
											{
													new ItemStack(Material.GOLD_BOOTS),
													new ItemStack(Material.GOLD_LEGGINGS),
													new ItemStack(Material.GOLD_CHESTPLATE),
													new ItemStack(Material.GOLD_HELMET)
											}
							),
					5000
			),

	// Draw My Thing

	DRAW_ARTIST
			(
					0,
					GameDisplay.Draw,
					"Artist",
					"drawartist",
					new String[]
							{
									"The world is but a canvas to your imagination."
							},
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.STICK)
							)
			),

	// Evolution

	EVOLUTION_ABILITY
			(
					0,
					GameDisplay.Evolution,
					"Darwinist",
					"evolutiondarwinist",
					new String[]
							{
									"Your DNA allows you to chop cooldown times!",
									C.blankLine,
									"All ability cooldowns are reduced by " + C.cGreen + "33%" + C.cGray + "."
							},
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.FEATHER)
							)
			),

	EVOLUTION_EVOLVE_SPEED
			(
					1,
					GameDisplay.Evolution,
					"Quick Evolver",
					"evolutionquickevolver",
					new String[]
							{
									"You always had dreams of growing up...",
									C.blankLine,
									"You evolve " + C.cGreen + "25%" + C.cGray + " faster."
							},
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.LEATHER_BOOTS)
							),
					4000
			),

	EVOLUTION_HEALTH
			(
					2,
					GameDisplay.Evolution,
					"Health Harvester",
					"evolutionhealthharvester",
					new String[]
							{
									"Harvest health every day...",
									C.blankLine,
									"Receive " + C.cGreen + "100%" + C.cGray + " health on kill"
							},
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.BLAZE_POWDER)
							),
					getAchievementsFor(AchievementCategory.EVOLUTION)
			),

	// Gladiators

	GLADIATORS_PLAYER
			(
					0,
					GameDisplay.Gladiators,
					"Gladiator",
					"gladiatorsgladiator",
					new String[]
							{
									"At my signal, unleash hell."
							},
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.IRON_SWORD)
							)
			),

	// Gravity

	GRAVITY_PLAYER
			(
					0,
					GameDisplay.Gravity,
					"Astronaut",
					"gravityastronaut",
					new String[]
							{
									"SPAAAAAAAAAAAAAACE"
							},
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.IRON_AXE),
									new ItemStack[]
											{
													new ItemStack(Material.GOLD_BOOTS),
													new ItemStack(Material.GOLD_LEGGINGS),
													new ItemStack(Material.GOLD_CHESTPLATE),
													new ItemStack(Material.GLASS)
											}
							)
			),

	// Halloween

	HALLOWEEN_FINN
			(
					0,
					GameDisplay.Halloween,
					"Finn the Human",
					"halloweenfinn",
					new String[]
							{
									"Jake is hiding in his pocket.",
									C.blankLine,
									"Nearby allies receive " + C.cGreen + "Speed 1"
							},
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.GOLD_SWORD),
									new ItemStack[]
											{
													new ItemStack(Material.IRON_BOOTS),
													new ItemStack(Material.IRON_LEGGINGS),
													new ItemStack(Material.IRON_CHESTPLATE),
													new ItemStack(Material.JACK_O_LANTERN)
											}
							)
			),

	HALLOWEEN_ROBIN_HOOD
			(
					1,
					GameDisplay.Halloween,
					"Robin Hood",
					"halloweenrobinhood",
					new String[]
							{
									"Trick or treating from the rich...",
									C.blankLine,
									"Nearby allies receive " + C.cGreen + "Regeneration 1"
							},
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.BOW),
									new ItemStack[]
											{
													new ItemStack(Material.CHAINMAIL_BOOTS),
													new ItemStack(Material.CHAINMAIL_LEGGINGS),
													new ItemStack(Material.CHAINMAIL_CHESTPLATE),
													new ItemStack(Material.JACK_O_LANTERN)
											}
							)
			),

	HALLOWEEN_THOR
			(
					2,
					GameDisplay.Halloween,
					"Thor",
					"halloweenthor",
					new String[]
							{
									"Smash and kill with your Thor Hammer!",
									C.blankLine,
									"Nearby allies receive " + C.cGreen + "Strength 1"
							},
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.IRON_SWORD),
									new ItemStack[]
											{
													new ItemStack(Material.DIAMOND_BOOTS),
													new ItemStack(Material.DIAMOND_LEGGINGS),
													new ItemStack(Material.DIAMOND_CHESTPLATE),
													new ItemStack(Material.JACK_O_LANTERN)
											}
							)
			),

	// Hide&Seek

	HIDE_AND_SEEK_SWAPPER
			(
					0,
					GameDisplay.HideSeek,
					"Swapper Hider",
					"hideseekswapperhider",
					new String[]
							{
									"Can change form unlimited times!"
							},
					new KitEntityData<>
							(
									Slime.class,
									new ItemStack(Material.SLIME_BALL)
							)
			),

	HIDE_AND_SEEK_QUICK
			(
					1,
					GameDisplay.HideSeek,
					"Instant Hider",
					"hideseekinstanthider",
					new String[]
							{
									"Changes into solid blocks almost instantly!"
							},
					new KitEntityData<>
							(
									Slime.class,
									new ItemStack(Material.FEATHER)
							),
					2000
			),

	HIDE_AND_SEEK_SHOCKER
			(
					2,
					GameDisplay.HideSeek,
					"Shocking Hider",
					"hideseekshockinghider",
					new String[]
							{
									"Shock and stun seekers!"
							},
					new KitEntityData<>
							(
									Slime.class,
									new ItemStack(Material.REDSTONE_LAMP_OFF)
							),
					5000
			),

	HIDE_AND_SEEK_INFESTOR
			(
					3,
					GameDisplay.HideSeek,
					"Infestor Hider",
					"hideseekinfestorhider",
					new String[]
							{
									"Can instantly infest a target block.",
									"This is the only way you can hide."
							},
					new KitEntityData<>
							(
									Slime.class,
									new ItemStack(Material.MAGMA_CREAM)
							),
					getAchievementsFor(AchievementCategory.BLOCK_HUNT)
			),

	HIDE_AND_SEEK_LEAPER
			(
					4,
					GameDisplay.HideSeek,
					"Leaper Hunter",
					"hideseekleaperhunter",
					new String[]
							{
									"Leap after those pretty blocks!",
									C.blankLine,
									leap()
							},
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.IRON_AXE),
									new ItemStack[]
											{
													new ItemStack(Material.IRON_BOOTS),
													new ItemStack(Material.IRON_LEGGINGS),
													new ItemStack(Material.IRON_CHESTPLATE),
													null
											}
							)
			),

	HIDE_AND_SEEK_TNT
			(
					5,
					GameDisplay.HideSeek,
					"TNT Hunter",
					"hideseektnthunter",
					new String[]
							{
									"Throw TNT to flush out the Hiders!",
									C.blankLine,
									receiveItem("TNT", 1, 15, 2),
									C.cYellow + "Click" + C.cGray + " with TNT to " + C.cGreen + "Throw TNT"
							},
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.TNT),
									new ItemStack[]
											{
													new ItemStack(Material.GOLD_BOOTS),
													new ItemStack(Material.GOLD_LEGGINGS),
													new ItemStack(Material.GOLD_CHESTPLATE),
													null
											}
							),
					2000
			),

	HIDE_AND_SEEK_RADAR
			(
					6,
					GameDisplay.HideSeek,
					"Radar Hunter",
					"hideseekradarhunter",
					new String[]
							{
									"tick......tick...tick.tick.",
									C.blankLine,
									C.cYellow + "Hold Compass" + C.cGray + " to use " + C.cGreen + "Radar Scanner",
									"Ticks get faster when you are near a Hider!"
							},
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.COMPASS),
									new ItemStack[]
											{
													new ItemStack(Material.CHAINMAIL_BOOTS),
													new ItemStack(Material.CHAINMAIL_LEGGINGS),
													new ItemStack(Material.CHAINMAIL_CHESTPLATE),
													null
											}
							),
					5000
			),

	// Bomb Lobbers

	BOMB_LOBBERS_JUMPER
			(
					0,
					GameDisplay.Lobbers,
					"Acrobat",
					"lobbersjumper",
					new String[]
							{
									"Use your sturdy shoes prevent fall damage.",
									C.blankLine,
									"Receive " + C.cGreen + 1 + C.cGray + " TNT every " + C.cGreen + "4-8" + C.cGray + " seconds. Max " + C.cGreen + "3."
							},
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.IRON_AXE)
							)
			),

	BOMB_LOBBERS_ARMORER
			(
					1,
					GameDisplay.Lobbers,
					"Armorer",
					"lobbersarmorer",
					new String[]
							{
									"He uses his expert armor-making",
									"skills to block excess damage!",
									C.blankLine,
									"Receive " + C.cGreen + "Full Gold Armor",
									"Receive " + C.cGreen + 1 + C.cGray + " TNT every " + C.cGreen + "4-8" + C.cGray + " seconds. Max " + C.cGreen + "3."
							},
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.IRON_HELMET),
									new ItemStack[]
											{
													new ItemStack(Material.IRON_BOOTS),
													new ItemStack(Material.IRON_LEGGINGS),
													new ItemStack(Material.IRON_CHESTPLATE),
													new ItemStack(Material.IRON_HELMET)
											}
							),
					2000
			),

	BOMB_LOBBERS_PITCHER
			(
					2,
					GameDisplay.Lobbers,
					"Pitcher",
					"lobberspitcher",
					new String[]
							{
									"He can easily pitch the perfect",
									"shot for any occasion.",
									C.blankLine,
									"Receive " + C.cGreen + 1 + C.cGray + " TNT every " + C.cGreen + "4-8" + C.cGray + " seconds. Max " + C.cGreen + "3.",
									click(true, "lever to " + C.cGreen + "Decrease Velocity. " + C.cGray + "Minimum of" + C.cGreen + " 1."),
									click(false, "lever to " + C.cGreen + "Increase Velocity. " + C.cGray + "Maximum of" + C.cGreen + " 3.")
							},
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.LEVER),
									new ItemStack[]
											{
													new ItemStack(Material.CHAINMAIL_BOOTS),
													new ItemStack(Material.CHAINMAIL_LEGGINGS),
													new ItemStack(Material.CHAINMAIL_CHESTPLATE),
													new ItemStack(Material.CHAINMAIL_HELMET)
											}
							),
					4000
			),

	BOMB_LOBBERS_WALLER
			(
					3,
					GameDisplay.Lobbers,
					"Waller",
					"lobberswalker",
					new String[]
							{
									"When the times get tough,",
									"build yourself a wall!",
									C.blankLine,
									"Receive " + C.cGreen + 1 + C.cGray + " TNT every " + C.cGreen + "4-8" + C.cGray + " seconds. Max " + C.cGreen + "3.",
									C.cYellow + "Click " + C.cGray + "a block with shovel to " + C.cGreen + "Place Wall"
							},
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.SMOOTH_BRICK)
							),
					getAchievementsFor(AchievementCategory.BOMB_LOBBERS)
			),

	// Micro Battles

	MICRO_ARCHER
			(
					0,
					GameDisplay.Micro,
					"Archer",
					"microarcherer",
					new String[]
							{
									"Shoot shoot!",
									" ",
									receiveArrow(1, 20, 3),
									"Start the game with: ",
									C.cGreen + "▪ Wood Sword",
									C.cGreen + "▪ Bow",
									C.cGreen + "▪ 3 Apples."
							},
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.BOW)
							)
			),

	MICRO_WORKER
			(
					1,
					GameDisplay.Micro,
					"Worker",
					"microworker",
					new String[]
							{
									"DIG DIG!",
									" ",
									"Start the game with: ",
									C.cGreen + "▪ Wood Sword",
									C.cGreen + "▪ Stone Spade",
									C.cGreen + "▪ Stone Pickaxe",
									C.cGreen + "▪ Stone Axe",
									C.cGreen + "▪ 4 Apples"
							},
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.STONE_PICKAXE)
							)
			),

	MICRO_FIGHTER
			(
					2,
					GameDisplay.Micro,
					"Fighter",
					"microfighter",
					new String[]
							{
									"HIT HIT!",
									" ",
									"Take " + C.cGreen + "0.5" + C.cGray + " less damage from attacks",
									"Start the game with: ",
									C.cGreen + "▪ Wood Sword",
									C.cGreen + "▪ 5 Apples"
							},
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.WOOD_SWORD)
							)
			),

	// Milk the Cow

	MILK_THE_COW_LEAP
			(
					0,
					GameDisplay.MilkCow,
					"Rabbit Farmer",
					"milkfarmerjump",
					new String[]
							{
									"Learned a thing or two from his rabbits!"
							},
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.IRON_HOE)
							)
			),

	MILK_THE_COW_COW
			(
					2,
					GameDisplay.MilkCow,
					"The Angry Cow",
					"milkcow",
					new String[]
							{
									"MOOOOOOOO"
							},
					new KitEntityData<>
							(
									Cow.class,
									new ItemStack(Material.MILK_BUCKET)
							)
			),

	// MCL

	MC_LEAGUE_PLAYER
			(
					0,
					GameDisplay.Minecraft_League,
					"Player",
					null,
					new String[]
							{
									"Entirely vanilla combat!"
							},
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.DIAMOND_SWORD)
							)
			),

	// Minestrike

	MINESTRIKE_PLAYER
			(
					0,
					GameDisplay.MineStrike,
					"Player",
					null,
					new String[]
							{
									C.cGreen + "Right-Click" + C.cGray + " - " + C.cYellow + "Fire Gun",
									C.cGreen + "Left-Click" + C.cGray + " - " + C.cYellow + "Reload Gun",
									C.cGreen + "Crouch" + C.cGray + " - " + C.cYellow + "Sniper Scope",
									C.blankLine,
									C.cGreen + "Hold Right-Click with Bomb" + C.cGray + " - " + C.cRed + "Plant Bomb",
									C.cGreen + "Hold Right-Click with Knife" + C.cGray + " - " + C.cAqua + "Defuse Bomb",
							},
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.WOOD_HOE)
							)
			),

	// Bawk Bawk Battles

	BAWK_BAWK_PLAYER
			(
					0,
					GameDisplay.BawkBawkBattles,
					"Bawk's Food",
					"bawksfood",
					new String[]
							{
									"Lord Bawk Bawk demands you follow his commands.",
									"If you fail his tasks you will lose a life.",
									"If you run out of lives Bawk Bawk's followers will eat you!"
							},
					new KitEntityData<>
							(
									Skeleton.class,
									new ItemStack(Material.EGG),
									new ItemStack[]
											{
													new ItemBuilder(Material.SKULL_ITEM, (byte) 3).setPlayerHead(UtilSkull.getPlayerHeadName(EntityType.CHICKEN)).build(),
													new ItemBuilder(Material.LEATHER_CHESTPLATE).setColor(Color.WHITE).build(),
													new ItemBuilder(Material.LEATHER_LEGGINGS).setColor(Color.WHITE).build(),
													new ItemBuilder(Material.LEATHER_BOOTS).setColor(Color.WHITE).build()
											}
							)
			),

	// Monster Maze

	MONSTER_MAZE_JUMPER
			(
					0,
					GameDisplay.MonsterMaze,
					"Jumper",
					"monstermazejumper",
					new String[]
							{
									"You have springs attached to your feet!",
									"Bouncy... bouncy... bouncy..."
							},
					new KitEntityData<>
							(
									Skeleton.class,
									new ItemStack(Material.FEATHER)
							)
			),

	MONSTER_MAZE_SLOW_BALL
			(
					1,
					GameDisplay.MonsterMaze,
					"Slowballer",
					"monstermazeslowballer",
					new String[]
							{
									"Slow enemies so they can't get to",
									"their Safe Pad in time!"
							},
					new KitEntityData<>
							(
									Skeleton.class,
									new ItemStack(Material.SNOW_BALL)
							),
					4000
			),

	MONSTER_MAZE_BODY_BUILDER
			(
					2,
					GameDisplay.MonsterMaze,
					"Body Builder",
					"monstermazebodybuilder",
					new String[]
							{
									"Your health just keeps getting better!"
							},
					new KitEntityData<>
							(
									Skeleton.class,
									new ItemStack(Material.IRON_PLATE)
							),
					2000
			),

	MONSTER_MAZE_REPULSOR
			(
					3,
					GameDisplay.MonsterMaze,
					"Repulsor",
					"monstermazerepulsor",
					new String[]
							{
									"You love to watch monsters",
									"fly away... Caw..."
							},
					new KitEntityData<>
							(
									Skeleton.class,
									new ItemStack(Material.COAL)
							),
					getAchievementsFor(AchievementCategory.MONSTER_MAZE)
			),

	// Paintball

	PAINTBALL_RIFLE
			(
					0,
					GameDisplay.Paintball,
					"Rifle",
					"paintballrifle",
					new String[]
							{
									"Semi-automatic paintball rifle.",
									C.blankLine,
									C.cGreen + "2" + C.cGray + " Hit Kill"
							},
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.IRON_BARDING)
							)
			),

	PAINTBALL_SHOTGUN
			(
					1,
					GameDisplay.Paintball,
					"Shotgun",
					"paintballshotgun",
					new String[]
							{
									"Pump action paintball shotgun.",
									C.blankLine,
									C.cGreen + "8" + C.cGray + " Pellets, " + C.cGreen + " 4" + C.cGray + " Pellet Hits Kill"
							},
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.IRON_BARDING)
							),
					2000
			),

	PAINTBALL_MACHINE_GUN
			(
					2,
					GameDisplay.Paintball,
					"Machine Gun",
					"paintballmachinegun",
					new String[]
							{
									"Full-automatic paintball gun.",
									C.blankLine,
									C.cGreen + "4" + C.cGray + " Hit Kill"
							},
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.DIAMOND_BARDING)
							),
					5000
			),

	PAINTBALL_SNIPER
			(
					3,
					GameDisplay.Paintball,
					"Sniper",
					"paintballsniper",
					new String[]
							{
									"Long range sniper rifle",
									C.blankLine,
									C.cGray + "Higher damage the longer scoped"
							},
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.STONE_HOE)
							),
					getAchievementsFor(AchievementCategory.SUPER_PAINTBALL)
			),

	// One In The Quiver

	OITQ_LEAPER
			(
					0,
					GameDisplay.Quiver,
					"Jumper",
					"quiverjumper",
					new String[]
							{
									"Evade and kill using your double jump!",
									C.blankLine,
									doubleJump()
							},
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.STONE_SWORD)
							)
			),

	OITQ_BRAWLER
			(
					1,
					GameDisplay.Quiver,
					"Brawler",
					"quiverbrawler",
					new String[]
							{
									"Missed your arrow? Not a big deal.",
									" ",
									"Deal " + C.cGreen + "+1" + C.cGray + " more damage"
							},
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.IRON_SWORD)
							),
					2000
			),

	OITQ_SLAM_SHOOTER
			(
					2,
					GameDisplay.Quiver,
					"Slam Shooter",
					"quiverslamshooter",
					new String[]
							{
									"Gets 2 arrows for killing slammed players!",
									C.blankLine,
									C.cYellow + "Right-Click" + C.cGray + " with your " + C.cGreen + "Diamond Shovel" + C.cGray + " to " + C.cGreen + "Ground Pound"
							},
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.DIAMOND_SPADE)
							),
					2000
			),

	OITQ_ENCHANTER
			(
					3,
					GameDisplay.Quiver,
					"Enchanter",
					"quiverenchanter",
					new String[]
							{
									"3 Kills, 1 Arrow.",
									C.blankLine,
									"Arrows bounce " + C.cGreen + "once" + C.cGray + " upon hitting your target"
							},
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.BOW)
							),
					5000
			),

	OITQ_NINJA
			(
					4,
					GameDisplay.Quiver,
					"Ninja",
					"quiverninja",
					new String[]
							{
									"You're a sneaky one, you!",
									C.blankLine,
									"Become invisible for " + C.cGreen + "1.2" + C.cGray + " seconds upon killing",
									C.cRed + "Melee attacks remove your invisibility"
							},
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.GOLD_SWORD)
							),
					getAchievementsFor(AchievementCategory.ONE_IN_THE_QUIVER)
			),

	// One In The Quiver Payload

	OITQP_BERSERKER
			(
					0,
					GameDisplay.QuiverPayload,
					"Berserker",
					"quiverbeserker",
					new String[]
							{
									"Missed your arrow? Not a big deal.",
									C.blankLine,
									"Deal " + C.cGreen + "+1" + C.cGray + " more damage",
									C.blankLine,
									C.cGreenB + "ULTIMATE",
									"You equip " + C.cGreen + "iron" + C.cGray + " chestplate, leggings and boots.",
									"Arrows do not one hit you.",
									"Lasts for " + C.cGreen + "6" + C.cGray + " seconds."
							},
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.IRON_SWORD)
							)
			),

	OITQP_NINJA
			(
					1,
					GameDisplay.QuiverPayload,
					"Ninja",
					"quivernewninja",
					new String[]
							{
									"Zip zap boing around the map!",
									"You do not spawn with an arrow!",
									C.blankLine,
									C.cYellow + "Right-click " + C.cGreen + "Sword" + C.cGray + " to dash.",
									C.blankLine,
									C.cGreenB + "ULTIMATE",
									"Your " + C.cGreen + "Gold Sword" + C.cGray + " changes into a " + C.cGreen + "Diamond Sword" + C.cGray + ".",
									"This new sword kills players in " + C.cRedB + "ONE" + C.cGray + " hit!",
									"Lasts for " + C.cGreen + "6" + C.cGray + " seconds."
							},
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.IRON_SWORD)
							)
			),

	OITQP_BARRAGE
			(
					2,
					GameDisplay.QuiverPayload,
					"Barrage",
					"quiverbarrage",
					new String[]
							{
									"Oooo look an arrow... ooo look an arrow...",
									C.blankLine,
									"Gain an arrow every " + C.cGreen + "5" + C.cGray + " seconds.",
									C.blankLine,
									C.cGreenB + "ULTIMATE",
									"When you fire your next arrow you fire an additional",
									C.cGreen + "10" + C.cGray + " arrows."
							},
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.ARROW)
							)
			),

	OITQP_SKY_WARRIOR
			(
					3,
					GameDisplay.QuiverPayload,
					"Sky Warrior",
					"quiverskywarrior",
					new String[]
							{
									"We\'ll see you in the sky.",
									C.blankLine,
									"Gain permanent Speed I",
									C.blankLine,
									C.cGreenB + "ULTIMATE",
									"You fly up into the air and are given a launcher.",
									"Clicking the launcher fires a Astral Arrow.",
									"When it hits a block a pulse of energy is fired",
									"damaging all players within " + C.cGreen + "5" + C.cGray + " blocks",
									"for " + C.cGreen + "10" + C.cGray + " damage.",
									C.blankLine,
									"Total of " + C.cRed + "3" + C.cGray + " uses!",
									C.blankLine,
									"Once all arrows are fired you teleport to a random",
									"teammate dealing damage to all players within",
									C.cGreen + "10" + C.cGray + " blocks for " + C.cGreen + "5" + C.cGray + " damage."
							},
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.DIAMOND_HOE)
							)
			),

	OITQP_PYROMANCER
			(
					4,
					GameDisplay.QuiverPayload,
					"Pyromancer",
					"quiverpyromancer",
					new String[]
							{
									"Hot! Hot! Hot!",
									C.blankLine,
									"When your arrows land, players within " + C.cGreen + "2" + C.cGray + " blocks are set on",
									"fire for " + C.cGreen + "3" + C.cGray + " seconds.",
									C.blankLine,
									C.cGreenB + "ULTIMATE",
									"You begin to fire " + C.cGreen + "20" + C.cGray + " arrows per second in all directions around you.",
									"During this time you have " + C.cGreen + "75%" + C.cGray + " reduced movement speed and are",
									"unable to jump.",
									"Lasts for " + C.cGreen + "5" + C.cGray + " seconds"
							},
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.BLAZE_POWDER)
							)
			),

	OITQP_NECROMANER
			(
					5,
					GameDisplay.QuiverPayload,
					"Necromancer",
					"quivernecromancer",
					new String[]
							{
									"Spooky scary skeletons",
									C.blankLine,
									"Successful arrow hits restore " + C.cGreen + "2" + C.cGray + " hearts.",
									C.blankLine,
									C.cGreenB + "ULTIMATE",
									"Summon " + C.cGreen + "4 Undead Minions" + C.cGray + " that shoot at other players",
									"Lasts for " + C.cGreen + "10" + C.cGray + " seconds."
							},
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.BONE)
							)
			),

	// Runner

	RUNNER_LEAPER
			(
					0,
					GameDisplay.Runner,
					"Jumper",
					"runnerleaper",
					new String[]
							{
									"Leap to avoid falling to your death!",
									C.blankLine,
									leap()
							},
					new KitEntityData<>
							(
									Skeleton.class,
									new ItemStack(Material.IRON_AXE)
							)
			),

	RUNNER_ARCHER
			(
					1,
					GameDisplay.Runner,
					"Archer",
					"runnerarcher",
					new String[]
							{
									"Fire arrows to cause blocks to fall!",
									C.blankLine,
									C.cYellow + "Left-Click" + C.cGray + " with" + C.cGreen + " Bow " + C.cGray + "to use " + C.cGreen + "Quick Shot"
							},
					new KitEntityData<>
							(
									Skeleton.class,
									new ItemStack(Material.BOW)
							),
					2000
			),

	RUNNER_FROSTY
			(
					2,
					GameDisplay.Runner,
					"Frosty",
					"runnyfrosty",
					new String[]
							{
									"Slow enemies to send them to their death!",
									C.blankLine,
									receiveItem("Snowball", 1, 0.5, 16)
							},
					new KitEntityData<>
							(
									Skeleton.class,
									new ItemStack(Material.SNOW_BALL)
							),
					5000
			),

	// Sheep Quest

	SHEEP_QUEST_BESERKER
			(
					0,
					GameDisplay.Sheep,
					"Beserker",
					"sheepbeserker",
					new String[]
							{
									"Agile warrior trained in the ways axe combat.",
									C.blankLine,
									doubleJump()
							},
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.IRON_AXE),
									new ItemStack[]
											{
													new ItemStack(Material.LEATHER_BOOTS),
													new ItemStack(Material.LEATHER_LEGGINGS),
													new ItemStack(Material.LEATHER_CHESTPLATE),
													new ItemStack(Material.LEATHER_HELMET)
											}
							)
			),

	SHEEP_QUEST_ARCHER
			(
					1,
					GameDisplay.Sheep,
					"Archer",
					"sheeparcher",
					new String[]
							{
									"Highly trained with a bow, probably an elf or something...",
									C.blankLine,
									receiveArrow(1, 2, 5),
									"Charge Bow to use " + C.cGreen + "Barrage"
							},
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.BOW),
									new ItemStack[]
											{
													new ItemStack(Material.LEATHER_BOOTS),
													new ItemStack(Material.LEATHER_LEGGINGS),
													new ItemStack(Material.LEATHER_CHESTPLATE),
													new ItemStack(Material.LEATHER_HELMET)
											}
							),
					2000
			),

	SHEEP_QUEST_BRUTE
			(
					2,
					GameDisplay.Sheep,
					"Brute",
					"sheepbrute",
					new String[]
							{
									"Strong enough to throw things around!",
									C.blankLine,
									"You can also pick up team mates!",
									C.cYellow + "Drop Weapon" + C.cGray + " to " + C.cGreen + "Throw Sheep / Players",
							},
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.IRON_SWORD),
									new ItemStack[]
											{
													new ItemStack(Material.LEATHER_BOOTS),
													new ItemStack(Material.LEATHER_LEGGINGS),
													new ItemStack(Material.LEATHER_CHESTPLATE),
													new ItemStack(Material.LEATHER_HELMET)
											}
							),
					5000
			),

	// Skyfall

	SKYFALL_SPEEDER
			(
					0,
					GameDisplay.Skyfall,
					"Speeder",
					"speeder",
					new String[]
							{
									"Increased Speed boosts",
									"due to aerodynamic clothes.",
									"Noire is going to like you."
							},
					new KitEntityData<>
							(
									Skeleton.class,
									new ItemStack(Material.ENDER_PEARL)
							)
			),

	SKYFALL_BOOSTER
			(
					1,
					GameDisplay.Skyfall,
					"Booster",
					"booster",
					new String[]
							{
									"Recieves an upgraded Elytra",
									"which has a built-in Jet Pack thruster"
							},
					new KitEntityData<>
							(
									Skeleton.class,
									new ItemStack(Material.FEATHER)
							)
			),

	SKYFALL_JOUSTER
			(
					2,
					GameDisplay.Skyfall,
					"Jouster",
					"jouster",
					new String[]
							{
									"Your special swords",
									"deal increased knockback"
							},
					new KitEntityData<>
							(
									Skeleton.class,
									new ItemStack(Material.IRON_SWORD)
							)
			),

	SKYFALL_STUNNER
			(
					3,
					GameDisplay.Skyfall,
					"Stunner",
					"stunner",
					new String[]
							{
									"Highly trained in",
									"removing others Elytra"
							},
					new KitEntityData<>
							(
									Skeleton.class,
									new ItemStack(Material.WEB)
							)
			),

	SKYFALL_AERONAUGHT
			(
					4,
					GameDisplay.Skyfall,
					"Aeronaught",
					"aeronaught",
					new String[]
							{
									"You are known for",
									"your highly trained",
									"fly combat skills"
							},
					new KitEntityData<>
							(
									Skeleton.class,
									new ItemStack(Material.DIAMOND_SWORD)
							)
			),

	SKYFALL_DEADEYE
			(
					5,
					GameDisplay.Skyfall,
					"Deadeye",
					"deadeye",
					new String[]
							{
									"You have heat seeking",
									"Arrows which will follow",
									"other players"
							},
					new KitEntityData<>
							(
									Skeleton.class,
									new ItemStack(Material.BOW)
							)
			),

	// Skywars

	SKYWARS_ICE
			(
					0,
					GameDisplay.Skywars,
					"Ice",
					"skywarschickenfarmer",
					new String[]
							{
									"Start with " + C.cGreen + "Wood Sword" + C.cGray + " and " + C.cGreen + "Ice",
									C.blankLine,
									C.cYellow + "Right Click" + C.cGray + " with Ice to create an " + C.cGreen + "Ice Bridge",
									"Lasts for " + C.cGreen + UtilTime.convertString(4000, 0, UtilTime.TimeUnit.SECONDS) + C.cGray + ".",
									"Cooldown " + C.cGreen + UtilTime.convertString(30000, 0, UtilTime.TimeUnit.SECONDS) + C.cGray + "."
							},
					new LinearUpgradeTree
							(
									new String[]
											{
													reduceCooldown("Ice Bridge", 1)
											},
									new String[]
											{
													reduceCooldown("Ice Bridge", 1),
													increase("Ice Bridge", "Uptime", 20)
											},
									new String[]
											{
													reduceCooldown("Ice Bridge", 1)
											},
									new String[]
											{
													reduceCooldown("Ice Bridge", 1),
													increase("Ice Bridge", "Uptime", 20)
											},
									new String[]
											{
													reduceCooldown("Ice Bridge", 1)
											}
							),
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.ICE)
							)
			),

	SKYWARS_FIRE
			(
					1,
					GameDisplay.Skywars,
					"Fire",
					"skywarsminer",
					new String[]
							{
									"Start with " + C.cGreen + "Wood Sword" + C.cGray + " and " + C.cGreen + "Blaze Rod",
									C.blankLine,
									C.cYellow + "Right Click" + C.cGray + " with Blaze Rod to use " + C.cGreen + "Fire Burst",
									"Sends out a pulse of fire that deals " + C.cGreen + 5 + C.cGray + " damage to",
									"all players within " + C.cGreen + 4 + C.cGray + " blocks.",
									"Cooldown " + C.cGreen + UtilTime.convertString(40000, 0, UtilTime.TimeUnit.SECONDS) + C.cGray + "."
							},
					new LinearUpgradeTree
							(
									new String[]
											{
													reduceCooldown("Fire Burst", 1)
											},
									new String[]
											{
													reduceCooldown("Fire Burst", 1)
											},
									new String[]
											{
													reduceCooldown("Fire Burst", 1),
													increase("Fire Burst", "Damage", 25)
											},
									new String[]
											{
													reduceCooldown("Fire Burst", 1)

											},
									new String[]
											{
													reduceCooldown("Fire Burst", 1)
											}
							),
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.BLAZE_ROD)
							),
					4000
			),

	SKYWARS_AIR
			(
					2,
					GameDisplay.Skywars,
					"Air",
					"skywarsmadscientist",
					new String[]
							{
									"Start with " + C.cGreen + "Wood Sword" + C.cGray + " and " + C.cGreen + "Eye of Ender",
									C.blankLine,
									"Right Click " + C.cGreen + "Eye of Ender" + C.cGray + " to teleport back to your",
									"last safe location",
									C.cRedB + "ONE" + C.cGray + " use only.",
									C.blankLine,
									doubleJump()
							},
					new LinearUpgradeTree
							(
									new String[]
											{
													reduceCooldown(doubleJumpRaw(), 1)
											},
									new String[]
											{
													increase(doubleJumpRaw(), "Range", 10)
											},
									new String[]
											{
													reduceCooldown(doubleJumpRaw(), 1)
											},
									new String[]
											{
													increase(doubleJumpRaw(), "Range", 10)
											},
									new String[]
											{
													reduceCooldown(doubleJumpRaw(), 1)
											}
							),
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.EYE_OF_ENDER)
							),
					4000
			),

	SKYWARS_METAL
			(
					3,
					GameDisplay.Skywars,
					"Metal",
					"skywarsdestructor",
					new String[]
							{
									"Start with " + C.cGreen + "Wood Sword" + C.cGray + ", " + C.cGreen + "Stone Pickaxe" + C.cGray + " and " + C.cGreen + "Magnet",
									C.blankLine,
									C.cYellow + "Right Click" + C.cGray + " with Redstone Comparator to activate " + C.cGreen + "Magnet",
									C.blankLine,
									C.cGreen + C.Italics + "Metal" + C.cGray + C.Italics + " means Gold/Chainmail/Iron armor.",
									C.blankLine,
									"Any player in your line of sight is drawn to you.",
									"The velocity that they are drawn towards you is",
									"based on how much metal armor they are wearing",
									"Cooldown " + C.cGreen + UtilTime.convertString(15000, 0, UtilTime.TimeUnit.SECONDS) + C.cGray + ".",
									C.blankLine,
									"For each piece of metal armor you wear you gain",
									"an additional " + C.cGreen + "0.5" + C.cGray + " hearts.",
							},
					new LinearUpgradeTree
							(
									new String[]
											{
													increase("Magnet", "Power", 10)
											},
									new String[]
											{
													increase("Magnet", "Power", 10)
											},
									new String[]
											{
													increase("Magnet", "Power", 10)
											},
									new String[]
											{
													increase("Magnet", "Power", 10)
											},
									new String[]
											{
													increase("Magnet", "Power", 10)
											}
							),
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.REDSTONE_COMPARATOR)
							),
					4000
			),

	SKYWARS_EARTH
			(
					4,
					GameDisplay.Skywars,
					"Earth",
					"skywarsearth",
					new String[]
							{
									"Start with " + C.cGreen + "Wood Sword" + C.cGray + " and " + C.cGreen + "Wood Shovel",
									C.blankLine,
									C.cYellow + "Right-Click" + C.cGray + " to fire " + C.cGreen + "Throwable Dirt",
									"You recieve " + C.cGreen + "1 Throwable Dirt " + C.cGray + "every",
									C.cGreen + UtilTime.convertString(20000, 0, UtilTime.TimeUnit.SECONDS) + C.cGray + ".",
									"You take " + C.cGreen + "25" + C.cGray + "% knockback while on the ground."
							},
					new LinearUpgradeTree
							(
									new String[]
											{
													increase("Throwable Dirt", "Knockback", 10)
											},
									new String[]
											{
													increase("Throwable Dirt", "Knockback", 10)
											},
									new String[]
											{
													increase("Throwable Dirt", "Knockback", 10)
											},
									new String[]
											{
													increase("Throwable Dirt", "Knockback", 10)
											},
									new String[]
											{
													increase("Throwable Dirt", "Knockback", 10)
											}
							),
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.DIRT)
							),
					getAchievementsFor(AchievementCategory.SKYWARS)
			),

	// Super Smash Mobs

	SSM_SKELETON
			(
					0,
					GameDisplay.Smash,
					"Skeleton",
					"smashskeleton",
					noLore(),
					new KitEntityData<>
							(
									Skeleton.class,
									new ItemStack(Material.BOW)
							)
			),

	SSM_GOLEM
			(
					1,
					GameDisplay.Smash,
					"Iron Golem",
					"smashirongolem",
					noLore(),
					new KitEntityData<>
							(
									IronGolem.class,
									new ItemStack(Material.IRON_BLOCK)
							)
			),

	SSM_SPIDER
			(
					2,
					GameDisplay.Smash,
					"Spider",
					"smashspider",
					noLore(),
					new KitEntityData<>
							(
									Spider.class,
									new ItemStack(Material.SPIDER_EYE)
							)
			),

	SSM_SLIME
			(
					3,
					GameDisplay.Smash,
					"Slime",
					"smashslime",
					noLore(),
					new KitEntityData<>
							(
									Slime.class,
									new ItemStack(Material.SLIME_BALL)
							)
			),

	SSM_CREEPER
			(
					4,
					GameDisplay.Smash,
					"Creeper",
					"smashcreeper",
					noLore(),
					new KitEntityData<>
							(
									Creeper.class,
									new ItemStack(Material.SULPHUR)
							),
					4000
			),

	SSM_ENDERMAN
			(
					5,
					GameDisplay.Smash,
					"Enderman",
					"smashenderman",
					noLore(),
					new KitEntityData<>
							(
									Enderman.class,
									new ItemStack(Material.ENDER_PEARL)
							),
					4000
			),

	SSM_SNOWMAN
			(
					6,
					GameDisplay.Smash,
					"Snowman",
					"smashsnowman",
					noLore(),
					new KitEntityData<>
							(
									Snowman.class,
									new ItemStack(Material.SNOW_BALL)
							),
					5000
			),

	SSM_WOLF
			(
					7,
					GameDisplay.Smash,
					"Wolf",
					"smashwolf",
					noLore(),
					new KitEntityData<>
							(
									Wolf.class,
									new ItemStack(Material.BONE)
							),
					5000
			),

	SSM_MAGMA_CUBE
			(
					8,
					GameDisplay.Smash,
					"Magma Cube",
					"smashmagmacube",
					noLore(),
					new KitEntityData<>
							(
									MagmaCube.class,
									new ItemStack(Material.MAGMA_CREAM)
							),
					5000
			),

	SSM_BLAZE
			(
					9,
					GameDisplay.Smash,
					"Blaze",
					"smashblaze",
					noLore(),
					new KitEntityData<>
							(
									Blaze.class,
									new ItemStack(Material.BLAZE_POWDER)
							),
					8000
			),

	SSM_WITCH
			(
					10,
					GameDisplay.Smash,
					"Witch",
					"smashwitch",
					noLore(),
					new KitEntityData<>
							(
									Witch.class,
									new ItemStack(Material.POTION)
							),
					6000
			),

	SSM_CHICKEN
			(
					11,
					GameDisplay.Smash,
					"Chicken",
					"smashchicken",
					noLore(),
					new KitEntityData<>
							(
									Chicken.class,
									new ItemStack(Material.EGG)
							),
					8000
			),

	SSM_SKELETON_HORSE
			(
					12,
					GameDisplay.Smash,
					"Skeletal Horse",
					"smashskeletalhorse",
					noLore(),
					new KitEntityData<>
							(
									Horse.class,
									new ItemStack(Material.BONE),
									Variant.SKELETON_HORSE.ordinal()
							),
					7000
			),

	SSM_PIG
			(
					13,
					GameDisplay.Smash,
					"Pig",
					"smashpig",
					noLore(),
					new KitEntityData<>
							(
									Pig.class,
									new ItemStack(Material.PORK)
							),
					7000
			),

	SSM_SQUID
			(
					14,
					GameDisplay.Smash,
					"Sky Squid",
					"smashskysquid",
					noLore(),
					new KitEntityData<>
							(
									Squid.class,
									new ItemStack(Material.INK_SACK)
							),
					3000
			),

	SSM_WITHER_SKELETON
			(
					15,
					GameDisplay.Smash,
					"Wither Skeleton",
					"smashwitherskeleton",
					noLore(),
					new KitEntityData<>
							(
									Skeleton.class,
									new ItemStack(Material.IRON_SWORD),
									SkeletonType.WITHER.ordinal()
							),
					6000
			),

	SSM_ZOMBIE
			(
					16,
					GameDisplay.Smash,
					"Zombie",
					"smashzombie",
					noLore(),
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.ROTTEN_FLESH)
							),
					6000
			),

	SSM_COW
			(
					17,
					GameDisplay.Smash,
					"Cow",
					"smashcow",
					noLore(),
					new KitEntityData<>
							(
									Cow.class,
									new ItemStack(Material.RAW_BEEF)
							),
					6000
			),

	SSM_GUARDIAN
			(
					18,
					GameDisplay.Smash,
					"Guardian",
					"smashguardian",
					noLore(),
					new KitEntityData<>
							(
									Guardian.class,
									new ItemStack(Material.PRISMARINE_SHARD)
							),
					8000
			),

	SSM_SHEEP
			(
					19,
					GameDisplay.Smash,
					"Sir. Sheep",
					"smashsir.sheep",
					noLore(),
					new KitEntityData<>
							(
									Sheep.class,
									new ItemStack(Material.WOOL)
							),
					getAchievementsFor(AchievementCategory.SMASH_MOBS)
			),

	SSM_VILLAGER
			(
					20,
					GameDisplay.Smash,
					"Villager",
					"smashvillager",
					noLore(),
					new KitEntityData<>
							(
									Villager.class,
									new ItemStack(Material.WHEAT)
							),
					getAchievementsFor(AchievementCategory.SMASH_MOBS)
			),

	// Snake

	SNAKE_SPEED
			(
					0,
					GameDisplay.Snake,
					"Speedy Snake",
					"snakespeedy",
					new String[]
							{

							},
					new KitEntityData<>
							(
									Sheep.class,
									new ItemStack(Material.FEATHER)
							)
			)
			{
				@Override
				protected void onEntitySpawn(LivingEntity entity)
				{
					((Colorable) entity).setColor(DyeColor.YELLOW);
				}
			},

	SNAKE_INVULNERABLE
			(
					1,
					GameDisplay.Snake,
					"Super Snake",
					"snakeinvulnerable",
					new String[]
							{

							},
					new KitEntityData<>
							(
									Sheep.class,
									new ItemStack(Material.NETHER_STAR)
							),
					5000
			)
			{
				@Override
				protected void onEntitySpawn(LivingEntity entity)
				{
					((Colorable) entity).setColor(DyeColor.LIME);
				}
			},

	SNAKE_REVERSE
			(
					2,
					GameDisplay.Snake,
					"Reversal Snake",
					"snakereversal",
					new String[]
							{

							},
					new KitEntityData<>
							(
									Sheep.class,
									new ItemStack(Material.COOKIE)
							),
					getAchievementsFor(AchievementCategory.SNAKE)
			)
			{
				@Override
				protected void onEntitySpawn(LivingEntity entity)
				{
					((Colorable) entity).setColor(DyeColor.MAGENTA);
				}
			},

	// Sneaky Assassins

	SNEAKY_ASSASSINS_ESCAPE_ARTIST
			(
					0,
					GameDisplay.SneakyAssassins,
					"Escape Artist",
					"sneakyassassinsescapeartist",
					new String[]
							{
									"Carries two extra Smoke Bombs!",
									C.blankLine,
									C.cYellow + "Right-Click" + C.cGray + " to " + C.cGreen + "Smoke Bomb"
							},
					new KitEntityData<>
							(
									Villager.class,
									new ItemStack(Material.INK_SACK)
							)
			),

	SNEAKY_ASSASSINS_ASSASSIN
			(
					1,
					GameDisplay.SneakyAssassins,
					"Ranged Assassin",
					"sneakyassassinsrangedassassin",
					new String[]
							{
									"Skilled at ranged assassination!",
									C.blankLine,
									C.cYellow + "Right-Click" + C.cGray + " to " + C.cGreen + "Smoke Bomb"
							},
					new KitEntityData<>
							(
									Villager.class,
									new ItemStack(Material.BOW)
							),
					2000
			),

	SNEAKY_ASSASSINS_REVEALER
			(
					2,
					GameDisplay.SneakyAssassins,
					"Revealer",
					"sneakyassassinsrevealer",
					new String[]
							{
									"Carries three Revealers which explode",
									"and reveal nearby assassins!",
									C.blankLine,
									C.cYellow + "Right-Click" + C.cGray + " to " + C.cGreen + "Smoke Bomb",
									C.cYellow + "Right-Click" + C.cGray + " with Diamond to " + C.cGreen + "Throw Revealer"
							},
					new KitEntityData<>
							(
									Villager.class,
									new ItemStack(Material.DIAMOND)
							),
					5000
			),

	SNEAKY_ASSASSINS_BRIBER
			(
					3,
					GameDisplay.SneakyAssassins,
					"Briber",
					"sneakyassassinsbriber",
					new String[]
							{
									"Pay Villagers to attack other players!",
									C.blankLine,
									C.cYellow + "Right-Click" + C.cGray + " to " + C.cGreen + "Smoke Bomb"
							},
					new KitEntityData<>
							(
									Villager.class,
									new ItemStack(Material.EMERALD)
							),
					getAchievementsFor(AchievementCategory.SNEAKY_ASSASSINS)
			),

	// Snow Fight

	SNOW_FIGHT_SPORTSMAN
			(
					0,
					GameDisplay.SnowFight,
					"Sportsman",
					"snowfightsportsman",
					new String[]
							{
									"Trained to be the fastest on snow and ice.",
									C.blankLine,
									doubleJump()
							},
					new KitEntityData<>
							(
									Skeleton.class,
									new ItemStack(Material.SNOW_BALL),
									new ItemStack[]
											{
													new ItemBuilder(Material.LEATHER_BOOTS).setUnbreakable(true).setColor(Color.RED).build(),
													new ItemBuilder(Material.LEATHER_LEGGINGS).setUnbreakable(true).setColor(Color.RED).build(),
													new ItemBuilder(Material.LEATHER_CHESTPLATE).setUnbreakable(true).setColor(Color.RED).build(),
													new ItemBuilder(Material.LEATHER_HELMET).setUnbreakable(true).setColor(Color.RED).build()
											}
							)
			),

	SNOW_FIGHT_TACTICIAN
			(
					1,
					GameDisplay.SnowFight,
					"Tactician",
					"snowfighttactician",
					new String[]
							{
									"No snow fight is complete without a tactical game!",
									C.blankLine,
									click(false, "Clay to construct a wall of Ice")
							},
					new KitEntityData<>
							(
									Skeleton.class,
									new ItemStack(Material.CLAY_BALL),
									new ItemStack[]
											{
													new ItemBuilder(Material.LEATHER_BOOTS).setUnbreakable(true).setColor(Color.RED).build(),
													new ItemBuilder(Material.LEATHER_LEGGINGS).setUnbreakable(true).setColor(Color.RED).build(),
													new ItemBuilder(Material.LEATHER_CHESTPLATE).setUnbreakable(true).setColor(Color.RED).build(),
													new ItemBuilder(Material.LEATHER_HELMET).setUnbreakable(true).setColor(Color.RED).build()
											}
							)
			),

	SNOW_FIGHT_MEDIC
			(
					2,
					GameDisplay.SnowFight,
					"Medic",
					"snowfightmedic",
					new String[]
							{
									"Throw warmth potions to heal allies!",
									C.blankLine,
									receiveItem("Warmth Potion", 1, 12, 1)
							},
					new KitEntityData<>
							(
									Skeleton.class,
									new ItemStack(Material.POTION),
									new ItemStack[]
											{
													new ItemBuilder(Material.LEATHER_BOOTS).setUnbreakable(true).setColor(Color.RED).build(),
													new ItemBuilder(Material.LEATHER_LEGGINGS).setUnbreakable(true).setColor(Color.RED).build(),
													new ItemBuilder(Material.LEATHER_CHESTPLATE).setUnbreakable(true).setColor(Color.RED).build(),
													new ItemBuilder(Material.LEATHER_HELMET).setUnbreakable(true).setColor(Color.RED).build()
											}
							)
			),

	// Spleef

	SPLEEF_SNOWBALLER
			(
					0,
					GameDisplay.Spleef,
					"Snowballer",
					"spleefsnowballer",
					new String[]
							{
									"Throw snowballs to break blocks!",
									"Receive a Snowball when you punch a block!",
									C.blankLine,
									"Deal " + C.cGray + "+30%" + C.cGray + " knockback per hit",
							},
					new KitEntityData<>
							(
									Skeleton.class,
									new ItemStack(Material.SNOW_BALL)
							)
			),

	SPLEEF_BRAWLER
			(
					1,
					GameDisplay.Spleef,
					"Brawler",
					"spleefbrawler",
					new String[]
							{
									"Very leap. Such knockback. Wow.",
									C.blankLine,
									leap(),
									"Deal " + C.cGray + "+60%" + C.cGray + " knockback per hit",
									"Hitting blocks damages all surrounding blocks",
							},
					new KitEntityData<>
							(
									Skeleton.class,
									new ItemStack(Material.IRON_AXE)
							),
					2000
			),

	SPLEEF_ARCHER
			(
					2,
					GameDisplay.Spleef,
					"Archer",
					"spleefarcher",
					new String[]
							{
									"Arrows will damage spleef blocks in a small radius.",
									C.blankLine,
									receiveArrow(1, 2, 2),
									"Deal " + C.cGreen + "+30%" + C.cGray + " knockback per hit"
							},
					new KitEntityData<>
							(
									Skeleton.class,
									new ItemStack(Material.BOW)
							)
			),

	// Squid Shooter
	SQUID_SHOOTER
			(
				0,
				GameDisplay.SquidShooter,
				"Retro Squid",
				null,
				new String[]
						{

						},
				new KitEntityData<>
						(
								Squid.class,
								new ItemStack(Material.INK_SACK)
						)
			),

	// Survival Games

	SG_AXEMAN
			(
					0,
					GameDisplay.SurvivalGames,
					"Axeman",
					"sgaxeman",
					new String[]
							{
									"Proficient in the art of axe combat!",
									C.blankLine,
									C.cYellow + "Right-Click" + C.cGray + " with Axes to " + C.cGreen + "Throw Axe",
									"Deal" + C.cGreen + " +1" + C.cGray + " Damage with Axes",
							},
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.IRON_AXE)
							)
			),

	SG_KNIGHT
			(
					1,
					GameDisplay.SurvivalGames,
					"Knight",
					"sgknight",
					new String[]
							{
									"A mighty iron-clad knight!",
									C.blankLine,
									"Take " + C.cGreen + "0.5" + C.cGray + " less damage from all attacks",
									C.cYellow + "Block on Player" + C.cGray + " to use " + C.cGreen + "Hilt Smash"
							},
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.IRON_SWORD)
							)
			),

	SG_ARCHER
			(
					2,
					GameDisplay.SurvivalGames,
					"Archer",
					"sgarcher",
					new String[]
							{
									"Passively crafts arrows from surrounding terrain.",
									C.blankLine,
									receiveArrow(1, 20, 3),
									"Charge Bow to use " + C.cGreen + "Barrage"
							},
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.BOW)
							),
					2000
			),

	SG_BRAWLER
			(
					3,
					GameDisplay.SurvivalGames,
					"Brawler",
					"sgbrawler",
					new String[]
							{
									"Giant and muscular, easily smacks others around.",
									C.blankLine,
									"Take " + C.cGreen + "85%" + C.cGray + " knockback",
									"Deal " + C.cGreen + "115%" + C.cGray + " knockback",
									C.cYellow + "Right-Click" + C.cGray + " with Sword/Axe to " + C.cGreen + "Ground Pound"
							},
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.IRON_SWORD)
							),
					2000
			),

	SG_ASSASSIN
			(
					4,
					GameDisplay.SurvivalGames,
					"Assassin",
					"sgassassin",
					new String[]
							{
									"Sneak up on opponents while they're looting chests!",
									"Players can only see your name tag",
									"when you're 8 blocks away!",
									C.blankLine,
									"Deal " + C.cGreen + "+2" + C.cGray + " damage from behind opponents."
							},
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.IRON_SWORD)
							),
					5000
			),

	SG_BEASTMASTER
			(
					5,
					GameDisplay.SurvivalGames,
					"Beastmaster",
					"sgbeastmaster",
					new String[]
							{
									"Woof woof woof!!",
									C.blankLine,
									"Spawn" + C.cGreen + " 1 Wolf " + C.cGray + "every " + C.cGreen + 30 + C.cGray + " seconds. Max " + C.cGreen + 1,
									C.cYellow + "Right-Click" + C.cGray + " with Sword/Axe to use " + C.cGreen + "Wolf Tackle"
							},
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.BONE)
							),
					5000
			),

	SG_BOMBER
			(
					6,
					GameDisplay.SurvivalGames,
					"Bomber",
					"sgbomber",
					new String[]
							{
									"Here comes the BOOM!",
									C.blankLine,
									receiveItem("TNT", 1, 30, 2),
									C.cYellow + "Left-Click" + C.cGray + " with Bow to prepare " + C.cGreen + "Explosive Arrow"
							},
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.TNT)
							),
					5000
			),

	SG_NECROMANCER
			(
					7,
					GameDisplay.SurvivalGames,
					"Necromancer",
					"sgnecromancer",
					new String[]
							{
									"Harness the power of the dark arts.",
									C.blankLine,
									"Killing an opponent summons a skeletal minion."
							},
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.SKULL_ITEM)
							),
					5000
			),

	SG_BARBARIAN
			(
					8,
					GameDisplay.SurvivalGames,
					"Barbarian",
					"sgbarbarian",
					new String[]
							{
									"Skilled at taking out teams!",
									"Abilities disabled for first 30 seconds.",
									C.blankLine,
									"Deal " + C.cGreen + "75" + "% damage to nearby enemies",
									C.cYellow + "Right-Click" + C.cGray + " with Sword/Axe to use " + C.cGreen + "Blade Vortex"
							},
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.DIAMOND_SWORD)
							),
					6000
			),

	SG_HORSEMAN
			(
					9,
					GameDisplay.SurvivalGames,
					"Horseman",
					"sghorseman",
					new String[]
							{
									"Proud owner of a (rapidly growing) horse!",
									C.blankLine,
									"You have a loyal horse companion."
							},
					new KitEntityData<>
							(
									Horse.class,
									new ItemStack(Material.DIAMOND_BARDING)
							),
					getAchievementsFor(AchievementCategory.SURVIVAL_GAMES)
			),

	// Tug of Wool

	TUG_ARCHER
			(
					0,
					GameDisplay.Tug,
					"Farmer Joe",
					null,
					new String[]
							{
									"A skilled bowman!",
									C.blankLine,
									receiveArrow(1, 3, 2),
									"Your arrows are " + C.cGreen + "Explosive" + C.cGray + "."
							},
					new KitEntityData<>
							(
									Skeleton.class,
									new ItemStack(Material.BOW),
									new ItemStack[]
											{
													new ItemStack(Material.CHAINMAIL_BOOTS),
													new ItemStack(Material.CHAINMAIL_LEGGINGS),
													new ItemStack(Material.CHAINMAIL_CHESTPLATE),
													new ItemStack(Material.CHAINMAIL_HELMET)
											}
							)
			),

	TUG_SMASHER
			(
					1,
					GameDisplay.Tug,
					"Butch",
					null,
					new String[]
							{
									"Giant and muscular, easily smacks others around.",
									"Like my dog.",
									C.blankLine,
									"Take " + C.cGreen + "85%" + C.cGray + " knockback",
									"Deal " + C.cGreen + "115%" + C.cGray + " knockback",
									C.cYellow + "Drop Weapon" + C.cGray + " to " + C.cGreen + "Spawn a Pet Dog",
									"Your dog increases the speed of nearby sheep."
							},
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.STONE_SWORD),
									new ItemStack[]
											{
													new ItemStack(Material.IRON_BOOTS),
													new ItemStack(Material.IRON_LEGGINGS),
													new ItemStack(Material.IRON_CHESTPLATE),
													new ItemStack(Material.IRON_HELMET)
											}
							)
			),

	TUG_LEAPER
			(
					2,
					GameDisplay.Tug,
					"Postman Pat",
					null,
					new String[]
							{
									"Easily leaps and jumps around,",
									"because he delivers letters?",
									C.blankLine,
									click(false, "to use " + C.cGreen + "Seismic Slam"),

							},
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.IRON_AXE),
									new ItemStack[]
											{
													new ItemStack(Material.GOLD_BOOTS),
													new ItemStack(Material.GOLD_LEGGINGS),
													new ItemStack(Material.GOLD_CHESTPLATE),
													new ItemStack(Material.GOLD_HELMET)
											}
							)
			),

	// Turf Wars

	TURF_WARS_MARKSMAN
			(
					0,
					GameDisplay.TurfWars,
					"Marksman",
					"turffortsmarksman",
					new String[]
							{
									"Unrivaled in archery. One hit kills anyone.",
									C.blankLine,
									receiveItem("Wool", 1, 4, 8),
									receiveArrow(1, 8, 2)
							},
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.BOW)
							)
			),

	TURF_WARS_INFILTRATOR
			(
					1,
					GameDisplay.TurfWars,
					"Infiltrator",
					"turffortsinfilitrator",
					new String[]
							{
									"Able to travel onto enemy turf, but you",
									"must return to your turf fast, or receive Slow.",
									C.blankLine,
									receiveItem("Wool", 1, 4, 4),
									receiveArrow(1, 8, 1)
							},
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.IRON_SWORD)
							),
					2000
			),

	TURF_WARS_SHREDDER
			(
					2,
					GameDisplay.TurfWars,
					"Shredder",
					"turffortsshredder",
					new String[]
							{
									"Arrows are weaker, but shred through forts.",
									C.blankLine,
									receiveItem("Wool", 1, 4, 6),
									receiveArrow(1, 4, 2),
									C.cYellow + "Charge" + C.cGray + " your Bow to use " + C.cGreen + "Barrage"
							},
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.BOW)
							),
					5000
			),

	// Type Wars

	TYPE_WARS_TYPER
			(
					0,
					GameDisplay.TypeWars,
					"Typer",
					"typewarstyper",
					new String[]
							{
									"This is the fastest typer",
									"in the land of Mineplex"
							},
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.FEATHER)
							)
			),

	// UHC

	UHC_PLAYER
			(
					0,
					GameDisplay.UHC,
					"UHC Player",
					"uhcplayer",
					new String[]
							{
									"A really unfortunate guy, who has been",
									"forced to fight to the death against",
									"a bunch of other guys."
							},
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.GOLD_SWORD)
							)
			),

	// Valentines Vendetta
	VALENTINES_PLAYER
			(
					0,
					GameDisplay.Valentines,
					"Master of Love",
					"valentinesmasteroflove",
					new String[]
							{
									"Ain't no mountain high enough!"
							},
					new KitEntityData<>
							(
									Skeleton.class,
									new ItemStack(Material.BOW)
							)
			),

	// Wither Assault
	WITHER_ASSAULT_ARCHER
			(
					0,
					GameDisplay.WitherAssault,
					"Human Archer",
					"witherhumanarcher",
					new String[]
							{
									"Skilled in the art of long range combat",
									C.blankLine,
									receiveArrow(1, 4, 4),
									"Your arrows give Blindness for " + C.cGreen + 4 + C.cGray + " seconds",
									doubleJump()
							},
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.BOW)
							)
			),

	WITHER_ASSAULT_MEDIC
			(
					1,
					GameDisplay.WitherAssault,
					"Human Medic",
					"witherhumanmedic",
					new String[]
							{
									"Be the hero! Heal other players.",
									C.blankLine,
									receiveItem("Healing Bottle", 1, 45, 1),
									C.cYellow + "Right-Click" + C.cGray + " with Axe to " + C.cGreen + "Throw Repairer",
									doubleJump()
							},
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.POTION)
							),
					2000
			),

	WITHER_ASSAULT_EDITOR
			(
					2,
					GameDisplay.WitherAssault,
					"Human Editor",
					"witherhumaneditor",
					new String[]
							{
									"Can " + C.cYellow + "Edit " + C.cGray + "the terrain to their benefit",
									C.blankLine,
									doubleJump()
							},
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.STONE_PICKAXE)
							),
					5000
			),


	WITHER_ASSAULT_WITHER
			(
					3,
					GameDisplay.WitherAssault,
					"Wither",
					"witherwither",
					new String[]
							{
									"Be the hero! Heal other players.",
									C.blankLine,
									receiveItem("Healing Bottle", 1, 45, 1),
									C.cYellow + "Right-Click" + C.cGray + " with Axe to " + C.cGreen + "Throw Repairer",
									doubleJump()
							},
					new KitEntityData<>
							(
									Skeleton.class,
									createColoured(Material.SKULL_ITEM, (byte) 1),
									SkeletonType.WITHER.ordinal()
							)
			),

	// Wizards

	WIZARDS_MAGE
			(
					0,
					GameDisplay.Wizards,
					"Mage",
					"wizardsmage",
					new String[]
							{
									"Start with two extra spells"
							},
					new KitEntityData<>
							(
									Witch.class,
									new ItemStack(Material.BLAZE_ROD)
							)
			),

	WIZARDS_SORCERER
			(
					1,
					GameDisplay.Wizards,
					"Sorcerer",
					"wizardssorcerer",
					new String[]
							{
									"Start out with an extra wand"
							},
					new KitEntityData<>
							(
									Witch.class,
									new ItemStack(Material.STONE_HOE)
							),
					2000
			),

	WIZARDS_MYSTIC
			(
					2,
					GameDisplay.Wizards,
					"Mystic",
					"wizardsmystic",
					new String[]
							{
									"Mana regeneration increased by 10%"
							},
					new KitEntityData<>
							(
									Witch.class,
									new ItemStack(Material.STONE_HOE)
							),
					2000
			),

	WIZARDS_WITCH_DOCTOR
			(
					3,
					GameDisplay.Wizards,
					"Witch Doctor",
					"wizardswitchdoctor",
					new String[]
							{
									"Max mana increased to 150"
							},
					new KitEntityData<>
							(
									Witch.class,
									new ItemStack(Material.IRON_HOE)
							),
					getAchievementsFor(AchievementCategory.WIZARDS)
			),

	// Zombie Survival

	ZOMBIE_SURVIVAL_KNIGHT
			(
					0,
					GameDisplay.ZombieSurvival,
					"Survivor Knight",
					"zombiesurvivalknight",
					new String[]
							{
									"Smash and kill through Zombies"
							},
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.IRON_SWORD),
									new ItemStack[]
											{
													new ItemStack(Material.IRON_BOOTS),
													new ItemStack(Material.IRON_LEGGINGS),
													new ItemStack(Material.IRON_CHESTPLATE),
													new ItemStack(Material.IRON_HELMET)
											}
							)
			),

	ZOMBIE_SURVIVAL_ROUGE
			(
					1,
					GameDisplay.ZombieSurvival,
					"Survivor Rogue",
					"zombiesurvivalrogue",
					new String[]
							{
									"You are weaker in combat, but very agile."
							},
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.IRON_AXE),
									new ItemStack[]
											{
													new ItemStack(Material.LEATHER_BOOTS),
													new ItemStack(Material.LEATHER_LEGGINGS),
													new ItemStack(Material.LEATHER_CHESTPLATE),
													new ItemStack(Material.LEATHER_HELMET)
											}
							),
					2000
			),

	ZOMBIE_SURVIVAL_ARCHER
			(
					2,
					GameDisplay.ZombieSurvival,
					"Survivor Archer",
					"zombiesurvivalarcher",
					new String[]
							{
									"Survive with the help of your trusty bow!"
							},
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.BOW),
									new ItemStack[]
											{
													new ItemStack(Material.CHAINMAIL_BOOTS),
													new ItemStack(Material.CHAINMAIL_LEGGINGS),
													new ItemStack(Material.CHAINMAIL_CHESTPLATE),
													new ItemStack(Material.CHAINMAIL_HELMET)
											}
							),
					2000
			),

	ZOMBIE_SURVIVAL_ALPHA
			(
					3,
					GameDisplay.ZombieSurvival,
					"Alpha Undead",
					"zombiesurvivalalpha",
					new String[]
							{
									"Leap at those undead"
							},
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.STONE_AXE)
							)
			),

	ZOMBIE_SURVIVAL_UNDEAD
			(
					4,
					GameDisplay.ZombieSurvival,
					"Undead",
					"zombiesurvivalundead",
					new String[]
							{
									"Just a standard Zombie..."
							},
					new KitEntityData<>
							(
									Zombie.class,
									new ItemStack(Material.IRON_SWORD)
							),
					KitAvailability.Hide,
					0
			),;

	public static String[] noLore()
	{
		return new String[0];
	}

	public static String leapRaw()
	{
		return "Leap";
	}

	public static String leap()
	{
		return click(false, "with Axe to " + C.cGreen + "Leap");
	}

	public static String doubleJumpRaw()
	{
		return "Double Jump";
	}

	public static String doubleJump()
	{
		return C.cYellow + "Double tap " + C.cGray + "your jump key to " + C.cGreen + "Double Jump";
	}

	public static String receiveItem(String item, int amount)
	{
		return C.cGray + "Receive " + C.cGreen + (amount == 1 ? (UtilText.startsWithVowel(item) ? "an" : "a") : amount) + " " + item;
	}

	public static String receiveArrow(int amount, double per, int max)
	{
		return receiveItem("Arrow" + (amount == 1 ? "" : "s"), amount, per, max);
	}

	public static String receiveItem(String item, int amount, double time, int max)
	{
		return C.cGray + "Receive " + C.cGreen + amount + C.cGray + " " + item + " every " + C.cGreen + time + C.cGray + " second" + (time == 1 ? "" : "s") +
				(max > 0 ? ". Max " + C.cGreen + max : "");
	}

	public static String click(boolean left, String comp)
	{
		return C.cYellow + (left ? "Left" : "Right") + "-Click " + C.cGray + comp;
	}

	public static String reduceCooldown(String perk, double time)
	{
		return C.cGray + "Reduce the cooldown of " + C.cGreen + perk + C.cGray + " by " + C.cGreen + time + C.cGray + " second" + (time == 1 ? "" : "s") + ".";
	}

	public static String increaseNumber(String perk, String increasing, double value, String data)
	{
		return C.cGray + "Increase the " + C.cGreen + increasing + C.cGray + " of " + C.cGreen + perk + C.cGray + " by " + C.cGreen + value + C.cGray + " " + data + ".";
	}

	public static String increase(String perk, String increasing, double percentage)
	{
		return C.cGray + "Increase the " + C.cGreen + increasing + C.cGray + " of " + C.cGreen + perk + C.cGray + " by " + C.cGreen + percentage + C.cGray + "%.";
	}

	public static String increase(String increasing, double value)
	{
		return C.cGray + "Increase your " + C.cGreen + increasing + C.cGray + " by " + value + C.cGray + ".";
	}

	public static String heart(int amount)
	{
		return C.cGreen + amount + C.cRed + "❤";
	}

	private static ItemStack createColoured(Material material, byte data)
	{
		return new ItemStack(material, 1, (short) 0, data);
	}

	private static Achievement[] getAchievementsFor(AchievementCategory category)
	{
		return Achievement.getByCategory(category).toArray(new Achievement[0]);
	}

	private static final MineplexGameManager MANAGER = Managers.require(MineplexGameManager.class);

	private final int _id;
	private final GameDisplay _display;
	private final String _legacyName;
	private final String _internalName;
	private final String _displayName;
	private final String[] _description;
	private final UpgradeTree _upgradeTree;

	private final KitEntityData<?> _entityData;

	private final KitAvailability _availability;
	private final int _cost;
	private final Achievement[] _achievements;

	GameKit(int id, GameDisplay display, String displayName, String legacyName, String[] description, KitEntityData<?> entityData)
	{
		this(id, display, displayName, legacyName, description, entityData, KitAvailability.Free, 0);
	}

	GameKit(int id, GameDisplay display, String displayName, String legacyName, String[] description, UpgradeTree upgradeTree, KitEntityData<?> entityData)
	{
		this(id, display, displayName, legacyName, description, upgradeTree, entityData, KitAvailability.Free, 0);
	}

	GameKit(int id, GameDisplay display, String displayName, String legacyName, String[] description, KitEntityData<?> entityData, int cost)
	{
		this(id, display, displayName, legacyName, description, entityData, KitAvailability.Gem, cost);
	}

	GameKit(int id, GameDisplay display, String displayName, String legacyName, String[] description, UpgradeTree upgradeTree, KitEntityData<?> entityData, int cost)
	{
		this(id, display, displayName, legacyName, description, upgradeTree, entityData, KitAvailability.Gem, cost);
	}

	GameKit(int id, GameDisplay display, String displayName, String legacyName, String[] description, KitEntityData<?> entityData, Achievement... achievements)
	{
		this(id, display, displayName, legacyName, description, entityData, KitAvailability.Achievement, 0, achievements);
	}

	GameKit(int id, GameDisplay display, String displayName, String legacyName, String[] description, UpgradeTree upgradeTree, KitEntityData<?> entityData, Achievement... achievements)
	{
		this(id, display, displayName, legacyName, description, upgradeTree, entityData, KitAvailability.Achievement, 0, achievements);
	}

	GameKit(int id, GameDisplay display, String displayName, String legacyName, String[] description, KitEntityData<?> entityData, KitAvailability availability, int cost, Achievement... achievements)
	{
		this(id, display, displayName, legacyName, description, null, entityData, availability, cost, achievements);
	}

	GameKit(int id, GameDisplay display, String displayName, String legacyName, String[] description, UpgradeTree upgradeTree, KitEntityData<?> entityData, KitAvailability availability, int cost, Achievement... achievements)
	{
		_id = display.getGameId() * 100 + id;
		_display = display;
		_legacyName = legacyName;
		_internalName = display.getKitGameName() + "." + displayName;
		_displayName = displayName;
		_description = description;
		_upgradeTree = upgradeTree;
		_entityData = entityData;
		_availability = availability;
		_cost = cost;
		_achievements = achievements;
	}

	protected void onEntitySpawn(LivingEntity entity)
	{
	}

	@SuppressWarnings("unchecked")
	public final NPC createNPC(Location location)
	{
		NPC npc = SimpleNPC.of(location, _entityData.getClassOfT(), "KIT_" + getId(), _entityData.getVariant());
		MANAGER.getNpcManager().addNPC(npc);
		MANAGER.addKitNPC(npc, this);

		LivingEntity entity = npc.getEntity();
		EntityEquipment equipment = entity.getEquipment();

		equipment.setItemInHand(_entityData.getInHand());
		equipment.setArmorContents(_entityData.getArmour());

		entity.setCustomName("");
		entity.setCustomNameVisible(true);

		if (entity instanceof Slime)
		{
			((Slime) entity).setSize(2);
		}

		onEntitySpawn(npc.getEntity());
		return npc;
	}

	public final boolean isChampionsKit()
	{
		return _display == GameDisplay.ChampionsDominate;
	}

	public final int getId()
	{
		return _id;
	}

	public final GameDisplay getDisplay()
	{
		return _display;
	}

	public final Optional<String> getLegacyName()
	{
		return Optional.ofNullable(_legacyName);
	}

	public final String getInternalName()
	{
		return _internalName;
	}

	public String getDisplayName()
	{
		return _displayName;
	}

	public String getFormattedName()
	{
		return getAvailability().getColour() + C.Bold + getDisplayName();
	}

	public String[] getDescription()
	{
		return _description;
	}

	public Optional<UpgradeTree> getUpgradeTree()
	{
		return Optional.ofNullable(_upgradeTree);
	}

	public KitEntityData<?> getEntityData()
	{
		return _entityData;
	}

	public KitAvailability getAvailability()
	{
		return _availability;
	}

	public int getCost()
	{
		return _cost;
	}

	public Achievement[] getAchievements()
	{
		return _achievements;
	}
}
