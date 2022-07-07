package mineplex.core.mission;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import mineplex.core.achievement.leveling.rewards.LevelCurrencyReward;
import mineplex.core.achievement.leveling.rewards.LevelExperienceReward;
import mineplex.core.common.currency.GlobalCurrency;
import mineplex.core.game.GameDisplay;

import static mineplex.core.game.GameDisplay.*;
import static mineplex.core.mission.MissionTrackerType.*;

public class MissionPopulator
{

	public static void populateMissions(MissionManager manager)
	{
		/*
			Similar to GameKits, Mission ids should be grouped according to the game. With each group having 100
			ids to use.
		 */

		// Special Event Missions 10000+

//		MissionContext.newBuilder(manager, 10000)
//				.name("Welcome Polly!")
//				.description("Play %s Games of Cake Wars")
//				.event()
//				.games(CakeWars4, CakeWarsDuos)
//				.xRange(25, 25)
//				.tracker(GAME_PLAY)
//				.rewards(
//						new LevelExperienceReward(100),
//						new LevelCurrencyReward(GlobalCurrency.GEM, 50),
//						new LevelCurrencyReward(GlobalCurrency.TREASURE_SHARD, 50)
//				)
//				.build();

//		MissionContext.newBuilder(manager, 10001)
//				.name("Happy Birthday!")
//				.description("Join Mineplex. You've already done it!")
//				.event()
//				.tracker(LOBBY_JOIN)
//				.rewards(
//						new LevelGadgetReward(Managers.require(GadgetManager.class).getGadget(ParticleFiveYear.class)),
//						new LevelTitleReward(Managers.require(TrackManager.class).getTrack(FiveYearTrack.class))
//				)
//				.build();

		// Lobby Missions 0-99

		MissionContext.newBuilder(manager, 0)
				.name("Parkourist")
				.description("Complete any Parkour")
				.tracker(LOBBY_PARKOUR)
				.rewards(
						new LevelExperienceReward(300),
						new LevelCurrencyReward(GlobalCurrency.GEM, 150),
						new LevelCurrencyReward(GlobalCurrency.TREASURE_SHARD, 150)
				)
				.build();

		MissionContext.newBuilder(manager, 1)
				.name("Gadget Crazy")
				.description("Use %s gadgets")
				.xRange(30, 40)
				.tracker(LOBBY_GADGET_USE)
				.rewards(
						new LevelExperienceReward(5),
						new LevelCurrencyReward(GlobalCurrency.GEM, 3),
						new LevelCurrencyReward(GlobalCurrency.TREASURE_SHARD, 3)
				)
				.build();

		MissionContext.newBuilder(manager, 2)
				.name("Not In The Face!")
				.description("Flesh Hook %s players")
				.xRange(15, 25)
				.tracker(LOBBY_FLESH_HOOK)
				.rewards(
						new LevelExperienceReward(5),
						new LevelCurrencyReward(GlobalCurrency.GEM, 3),
						new LevelCurrencyReward(GlobalCurrency.TREASURE_SHARD, 3)
				)
				.build();

		MissionContext.newBuilder(manager, 3)
				.name("Jumper")
				.description("Use a jump pad %s times")
				.xRange(25, 40)
				.tracker(LOBBY_JUMP_PAD)
				.rewards(
						new LevelExperienceReward(4),
						new LevelCurrencyReward(GlobalCurrency.GEM, 2),
						new LevelCurrencyReward(GlobalCurrency.TREASURE_SHARD, 2)
				)
				.build();

		MissionContext.newBuilder(manager, 4)
				.name("Mini Mini Games")
				.description("Play %s Lobby Games")
				.xRange(3, 6)
				.tracker(LOBBY_GAMES_PLAYED)
				.rewards(
						new LevelExperienceReward(20),
						new LevelCurrencyReward(GlobalCurrency.GEM, 10),
						new LevelCurrencyReward(GlobalCurrency.TREASURE_SHARD, 10)
				)
				.build();

		MissionContext.newBuilder(manager, 5)
				.name("Quick Killer")
				.description("Kill someone in under %s seconds in GLD")
				.yRange(45, 60)
				.tracker(LOBBY_GLD_QUICK)
				.rewards(
						new LevelExperienceReward(1000),
						new LevelCurrencyReward(GlobalCurrency.GEM, 300),
						new LevelCurrencyReward(GlobalCurrency.TREASURE_SHARD, 300)
				)
				.build();

		MissionContext.newBuilder(manager, 6)
				.name("Skilled")
				.description("Take no damage in GLD")
				.tracker(LOBBY_GLD_NO_DAMAGE)
				.rewards(
						new LevelExperienceReward(600),
						new LevelCurrencyReward(GlobalCurrency.GEM, 300),
						new LevelCurrencyReward(GlobalCurrency.TREASURE_SHARD, 300)
				)
				.build();

		// Global Game Missions 100-199

		MissionContext.newBuilder(manager, 100)
				.name("Enthusiast")
				.description("Play %s games")
				.games(GameDisplay.values())
				.xRange(10, 15)
				.tracker(GAME_PLAY)
				.rewards(
						new LevelExperienceReward(40),
						new LevelCurrencyReward(GlobalCurrency.GEM, 20),
						new LevelCurrencyReward(GlobalCurrency.TREASURE_SHARD, 20)
				)
				.build();

		MissionContext.newBuilder(manager, 101)
				.name("Winner")
				.description("Win %s games")
				.games(GameDisplay.values())
				.xRange(3, 7)
				.tracker(GAME_WIN)
				.rewards(
						new LevelExperienceReward(100),
						new LevelCurrencyReward(GlobalCurrency.GEM, 50),
						new LevelCurrencyReward(GlobalCurrency.TREASURE_SHARD, 50)
				)
				.build();

		MissionContext.newBuilder(manager, 102)
				.name("Killer")
				.description("Kill %s players in game")
				.games(GameDisplay.values())
				.xRange(15, 25)
				.tracker(GAME_KILL)
				.rewards(
						new LevelExperienceReward(14),
						new LevelCurrencyReward(GlobalCurrency.GEM, 7),
						new LevelCurrencyReward(GlobalCurrency.TREASURE_SHARD, 7)
				)
				.build();

		MissionContext.newBuilder(manager, 103)
				.name("I Shall Taunt You A Second Time")
				.description("Taunt %s times in game")
				.games(GameDisplay.values())
				.xRange(5, 15)
				.tracker(GAME_TAUNT)
				.rewards(
						new LevelExperienceReward(20),
						new LevelCurrencyReward(GlobalCurrency.GEM, 10),
						new LevelCurrencyReward(GlobalCurrency.TREASURE_SHARD, 10)
				)
				.build();

		MissionContext.newBuilder(manager, 104)
				.name("Leap Higher!")
				.description("Leap or Double Jump %s times in game")
				.games(GameDisplay.values())
				.xRange(20, 35)
				.tracker(GAME_LEAP)
				.rewards(
						new LevelExperienceReward(10),
						new LevelCurrencyReward(GlobalCurrency.GEM, 5),
						new LevelCurrencyReward(GlobalCurrency.TREASURE_SHARD, 5)
				)
				.build();

		// Turf Wars 200-299

		MissionContext.newBuilder(manager, 200)
				.name("Juggler")
				.description("Kill %s players while in midair")
				.games(TurfWars)
				.xRange(3, 6)
				.tracker(GAME_KILL_MIDAIR)
				.rewards(
						new LevelExperienceReward(80),
						new LevelCurrencyReward(GlobalCurrency.GEM, 40),
						new LevelCurrencyReward(GlobalCurrency.TREASURE_SHARD, 40)
				)
				.build();

		MissionContext.newBuilder(manager, 201)
				.name("Sneaky")
				.description("Be on the enemy turf for %s seconds")
				.games(TurfWars)
				.xRange(60, 90)
				.tracker(TURF_WARS_ON_ENEMY)
				.rewards(
						new LevelExperienceReward(2),
						new LevelCurrencyReward(GlobalCurrency.GEM, 1),
						new LevelCurrencyReward(GlobalCurrency.TREASURE_SHARD, 1)
				)
				.build();

		MissionContext.newBuilder(manager, 202)
				.name("Better than explosions")
				.description("Break %s wool with your bow")
				.games(TurfWars)
				.xRange(50, 60)
				.tracker(TURF_WARS_BOW_BREAK)
				.rewards(
						new LevelExperienceReward(4),
						new LevelCurrencyReward(GlobalCurrency.GEM, 2),
						new LevelCurrencyReward(GlobalCurrency.TREASURE_SHARD, 2)
				)
				.build();

		// Draw My Thing 300-399

		MissionContext.newBuilder(manager, 300)
				.name("Quick Thinker")
				.description("Guess %s words in under %s seconds")
				.games(Draw)
				.xRange(2, 5)
				.yRange(20, 25)
				.tracker(DMT_GUESS)
				.rewards(
						new LevelExperienceReward(200),
						new LevelCurrencyReward(GlobalCurrency.GEM, 100),
						new LevelCurrencyReward(GlobalCurrency.TREASURE_SHARD, 100)
				)
				.build();

		MissionContext.newBuilder(manager, 301)
				.name("Hard To Beat")
				.description("Guess every word in a game")
				.games(Draw)
				.tracker(DMT_GUESS_ALL)
				.rewards(
						new LevelExperienceReward(1200),
						new LevelCurrencyReward(GlobalCurrency.GEM, 600),
						new LevelCurrencyReward(GlobalCurrency.TREASURE_SHARD, 600)
				)
				.build();

		// Survival Games 400-499

		MissionContext.<String>newBuilder(manager, 400)
				.name("Tickle Master")
				.description("Kill %s players with a feather")
				.games(SurvivalGames, SurvivalGamesTeams)
				.xRange(1, 3)
				.tracker(GAME_KILL)
				.trackerData("Feather")
				.rewards(
						new LevelExperienceReward(300),
						new LevelCurrencyReward(GlobalCurrency.GEM, 150),
						new LevelCurrencyReward(GlobalCurrency.TREASURE_SHARD, 150)
				)
				.build();

		MissionContext.newBuilder(manager, 401)
				.name("Bojack Takedown")
				.description("Kill %s players while riding a horse")
				.games(SurvivalGames, SurvivalGamesTeams)
				.xRange(1, 3)
				.tracker(SG_BOW_HORSE_KILL)
				.rewards(
						new LevelExperienceReward(400),
						new LevelCurrencyReward(GlobalCurrency.GEM, 200),
						new LevelCurrencyReward(GlobalCurrency.TREASURE_SHARD, 200)
				)
				.build();

		MissionContext.newBuilder(manager, 402)
				.name("Not Enough Supply")
				.description("Loot %s supply drops")
				.games(SurvivalGames, SurvivalGamesTeams)
				.xRange(3, 9)
				.tracker(SG_SUPPLY_DROP_OPEN)
				.rewards(
						new LevelExperienceReward(120),
						new LevelCurrencyReward(GlobalCurrency.GEM, 60),
						new LevelCurrencyReward(GlobalCurrency.TREASURE_SHARD, 60)
				)
				.build();

		MissionContext.<Material>newBuilder(manager, 403)
				.name("Diamond Weaponsmith")
				.description("Craft %s diamond swords")
				.games(SurvivalGames, SurvivalGamesTeams)
				.xRange(1, 3)
				.tracker(GAME_CRAFT_ITEM)
				.trackerData(Material.DIAMOND_SWORD)
				.rewards(
						new LevelExperienceReward(250),
						new LevelCurrencyReward(GlobalCurrency.GEM, 125),
						new LevelCurrencyReward(GlobalCurrency.TREASURE_SHARD, 125)
				)
				.build();

		MissionContext.<Material>newBuilder(manager, 404)
				.name("Gold Weaponsmith")
				.description("Craft %s gold swords")
				.games(SurvivalGames, SurvivalGamesTeams)
				.xRange(1, 3)
				.tracker(GAME_CRAFT_ITEM)
				.trackerData(Material.GOLD_SWORD)
				.rewards(
						new LevelExperienceReward(200),
						new LevelCurrencyReward(GlobalCurrency.GEM, 100),
						new LevelCurrencyReward(GlobalCurrency.TREASURE_SHARD, 100)
				)
				.build();

		MissionContext.<Material>newBuilder(manager, 405)
				.name("Iron Weaponsmith")
				.description("Craft %s iron swords")
				.games(SurvivalGames, SurvivalGamesTeams)
				.xRange(1, 3)
				.tracker(GAME_CRAFT_ITEM)
				.trackerData(Material.IRON_SWORD)
				.rewards(
						new LevelExperienceReward(250),
						new LevelCurrencyReward(GlobalCurrency.GEM, 125),
						new LevelCurrencyReward(GlobalCurrency.TREASURE_SHARD, 125)
				)
				.build();

		MissionContext.<Material>newBuilder(manager, 406)
				.name("Why Tho")
				.description("Craft %s gold shovels")
				.games(SurvivalGames, SurvivalGamesTeams)
				.xRange(1, 3)
				.tracker(GAME_CRAFT_ITEM)
				.trackerData(Material.GOLD_SPADE)
				.rewards(
						new LevelExperienceReward(150),
						new LevelCurrencyReward(GlobalCurrency.GEM, 75),
						new LevelCurrencyReward(GlobalCurrency.TREASURE_SHARD, 75)
				)
				.build();

		MissionContext.newBuilder(manager, 407)
				.name("Looter")
				.description("Loot %s chests")
				.games(SurvivalGames, SurvivalGamesTeams)
				.xRange(40, 60)
				.tracker(GAME_CHEST_OPEN)
				.rewards(
						new LevelExperienceReward(4),
						new LevelCurrencyReward(GlobalCurrency.GEM, 2),
						new LevelCurrencyReward(GlobalCurrency.TREASURE_SHARD, 2)
				)
				.build();

		MissionContext.newBuilder(manager, 408)
				.name("Steve Walker")
				.description("Walk %s blocks")
				.games(SurvivalGames, SurvivalGamesTeams)
				.xRange(2000, 4000)
				.tracker(GAME_WALK)
				.scaleDownRewards()
				.rewards(
						new LevelExperienceReward(4),
						new LevelCurrencyReward(GlobalCurrency.GEM, 2),
						new LevelCurrencyReward(GlobalCurrency.TREASURE_SHARD, 2)
				)
				.build();

		MissionContext.newBuilder(manager, 409)
				.name("It Sparkles Now!")
				.description("Enchant %s items")
				.games(SurvivalGames, SurvivalGamesTeams)
				.xRange(2, 5)
				.tracker(GAME_ENCHANT_ITEM)
				.rewards(
						new LevelExperienceReward(100),
						new LevelCurrencyReward(GlobalCurrency.GEM, 50),
						new LevelCurrencyReward(GlobalCurrency.TREASURE_SHARD, 50)
				)
				.build();

		// Block Hunt 500-599

		MissionContext.newBuilder(manager, 500)
				.name("Block Ninja")
				.description("Infest %s blocks")
				.games(HideSeek)
				.xRange(10, 15)
				.tracker(BLOCK_HUNT_INFEST)
				.rewards(
						new LevelExperienceReward(20),
						new LevelCurrencyReward(GlobalCurrency.GEM, 10),
						new LevelCurrencyReward(GlobalCurrency.TREASURE_SHARD, 10)
				)
				.build();

		MissionContext.newBuilder(manager, 501)
				.name("Glitter In The Sky")
				.description("Set off %s fireworks")
				.games(HideSeek)
				.xRange(6, 12)
				.tracker(BLOCK_HUNT_FIREWORK)
				.rewards(
						new LevelExperienceReward(20),
						new LevelCurrencyReward(GlobalCurrency.GEM, 10),
						new LevelCurrencyReward(GlobalCurrency.TREASURE_SHARD, 10)
				)
				.build();

		MissionContext.newBuilder(manager, 502)
				.name("Neko Neko Nee")
				.description("Meow %s times")
				.games(HideSeek)
				.xRange(20, 40)
				.tracker(BLOCK_HUNT_MEOW)
				.rewards(
						new LevelExperienceReward(10),
						new LevelCurrencyReward(GlobalCurrency.GEM, 5),
						new LevelCurrencyReward(GlobalCurrency.TREASURE_SHARD, 5)
				)
				.build();

		MissionContext.<String>newBuilder(manager, 503)
				.name("Hunters Get Hunted")
				.description("Kill %s hunters with the Hyper Axe")
				.games(HideSeek)
				.xRange(1, 3)
				.tracker(GAME_KILL)
				.trackerData("Hyper Axe")
				.rewards(
						new LevelExperienceReward(250),
						new LevelCurrencyReward(GlobalCurrency.GEM, 125),
						new LevelCurrencyReward(GlobalCurrency.TREASURE_SHARD, 125)
				)
				.build();

		MissionContext.<ChatColor>newBuilder(manager, 504)
				.name("Legitimate Strategy")
				.description("Stand perfectly still for %s seconds as a hider")
				.games(HideSeek)
				.xRange(240, 360)
				.tracker(GAME_STAND_STILL)
				.trackerData(ChatColor.AQUA)
				.rewards(
						new LevelExperienceReward(2),
						new LevelCurrencyReward(GlobalCurrency.GEM, 1),
						new LevelCurrencyReward(GlobalCurrency.TREASURE_SHARD, 1)
				)
				.build();

		MissionContext.<ChatColor>newBuilder(manager, 505)
				.name("Who Needs To Hide?")
				.description("Run %s blocks as a hider")
				.games(HideSeek)
				.xRange(500, 600)
				.tracker(GAME_WALK)
				.trackerData(ChatColor.AQUA)
				.scaleDownRewards()
				.rewards(
						new LevelExperienceReward(20),
						new LevelCurrencyReward(GlobalCurrency.GEM, 10),
						new LevelCurrencyReward(GlobalCurrency.TREASURE_SHARD, 10)
				)
				.build();

		MissionContext.<DamageCause>newBuilder(manager, 506)
				.name("Poke!")
				.description("Shoot %s hunters as a hider")
				.games(HideSeek)
				.xRange(10, 20)
				.tracker(BLOCK_HUNT_SHOOT_HUNTER)
				.rewards(
						new LevelExperienceReward(6),
						new LevelCurrencyReward(GlobalCurrency.GEM, 3),
						new LevelCurrencyReward(GlobalCurrency.TREASURE_SHARD, 3)
				)
				.build();

		// Cake Wars 600-699

		MissionContext.<String>newBuilder(manager, 600)
				.name("Golden Warrior")
				.description("Kill %s players with The Golden Pickaxe")
				.games(CakeWars4, CakeWarsDuos)
				.xRange(2, 4)
				.tracker(GAME_KILL)
				.trackerData("The Golden Pickaxe")
				.rewards(
						new LevelExperienceReward(200),
						new LevelCurrencyReward(GlobalCurrency.GEM, 100),
						new LevelCurrencyReward(GlobalCurrency.TREASURE_SHARD, 100)
				)
				.build();

		MissionContext.<String>newBuilder(manager, 601)
				.name("Does This Have Rocks In It?")
				.description("Kill %s players by throwing a Snowball")
				.games(CakeWars4, CakeWarsDuos)
				.xRange(2, 4)
				.tracker(GAME_KILL)
				.trackerData("Frosting")
				.rewards(
						new LevelExperienceReward(200),
						new LevelCurrencyReward(GlobalCurrency.GEM, 100),
						new LevelCurrencyReward(GlobalCurrency.TREASURE_SHARD, 100)
				)
				.build();

		MissionContext.<String>newBuilder(manager, 602)
				.name("The New JoJo")
				.description("Kill %s players with your Fists")
				.games(CakeWars4, CakeWarsDuos)
				.xRange(2, 4)
				.tracker(GAME_KILL)
				.trackerData("Fists")
				.rewards(
						new LevelExperienceReward(200),
						new LevelCurrencyReward(GlobalCurrency.GEM, 100),
						new LevelCurrencyReward(GlobalCurrency.TREASURE_SHARD, 100)
				)
				.build();

		MissionContext.newBuilder(manager, 603)
				.name("The Cake Wars Diet")
				.description("Eat %s cake slices")
				.games(CakeWars4, CakeWarsDuos)
				.xRange(12, 18)
				.tracker(CW_EAT_SLICE)
				.rewards(
						new LevelExperienceReward(30),
						new LevelCurrencyReward(GlobalCurrency.GEM, 15),
						new LevelCurrencyReward(GlobalCurrency.TREASURE_SHARD, 15)
				)
				.build();

		MissionContext.<Material>newBuilder(manager, 604)
				.name("This Is All Mine")
				.description("Place %s wool blocks.")
				.games(CakeWars4, CakeWarsDuos)
				.xRange(100, 150)
				.tracker(GAME_BLOCK_PLACE)
				.trackerData(Material.WOOL)
				.rewards(
						new LevelExperienceReward(2),
						new LevelCurrencyReward(GlobalCurrency.GEM, 1),
						new LevelCurrencyReward(GlobalCurrency.TREASURE_SHARD, 1)
				)
				.build();

		MissionContext.newBuilder(manager, 605)
				.name("Domination")
				.description("Capture %s points.")
				.games(CakeWars4, CakeWarsDuos)
				.xRange(5, 8)
				.tracker(GAME_CAPTURE_POINT)
				.rewards(
						new LevelExperienceReward(60),
						new LevelCurrencyReward(GlobalCurrency.GEM, 30),
						new LevelCurrencyReward(GlobalCurrency.TREASURE_SHARD, 30)
				)
				.build();

		MissionContext.newBuilder(manager, 606)
				.name("It's like Christmas but with Cake")
				.description("Loot %s cake island chests.")
				.games(CakeWars4, CakeWarsDuos)
				.xRange(4, 8)
				.tracker(GAME_CHEST_OPEN)
				.rewards(
						new LevelExperienceReward(60),
						new LevelCurrencyReward(GlobalCurrency.GEM, 30),
						new LevelCurrencyReward(GlobalCurrency.TREASURE_SHARD, 30)
				)
				.build();

		MissionContext.<ChatColor>newBuilder(manager, 607)
				.name("Brick Entrepreneur")
				.description("Spend %s bricks")
				.games(CakeWars4, CakeWarsDuos)
				.xRange(100, 200)
				.tracker(CW_SPEND_RESOURCE)
				.trackerData(ChatColor.RED)
				.scaleDownRewards()
				.rewards(
						new LevelExperienceReward(50),
						new LevelCurrencyReward(GlobalCurrency.GEM, 1),
						new LevelCurrencyReward(GlobalCurrency.TREASURE_SHARD, 1)
				)
				.build();

		MissionContext.<ChatColor>newBuilder(manager, 608)
				.name("Emerald Entrepreneur")
				.description("Spend %s emeralds")
				.games(CakeWars4, CakeWarsDuos)
				.xRange(30, 60)
				.tracker(CW_SPEND_RESOURCE)
				.trackerData(ChatColor.GREEN)
				.rewards(
						new LevelExperienceReward(4),
						new LevelCurrencyReward(GlobalCurrency.GEM, 2),
						new LevelCurrencyReward(GlobalCurrency.TREASURE_SHARD, 2)
				)
				.build();

		MissionContext.<ChatColor>newBuilder(manager, 609)
				.name("Nether Star Entrepreneur")
				.description("Spend %s nether stars")
				.games(CakeWars4, CakeWarsDuos)
				.xRange(6, 12)
				.tracker(CW_SPEND_RESOURCE)
				.trackerData(ChatColor.GOLD)
				.rewards(
						new LevelExperienceReward(20),
						new LevelCurrencyReward(GlobalCurrency.GEM, 10),
						new LevelCurrencyReward(GlobalCurrency.TREASURE_SHARD, 10)
				)
				.build();

		// Super Smash Mobs 700-799

		MissionContext.newBuilder(manager, 700)
				.name("Plus Ultra!")
				.description("Use a Smash Crystal %s times")
				.games(Smash, SmashTeams)
				.xRange(2, 5)
				.tracker(SSM_SMASH)
				.rewards(
						new LevelExperienceReward(100),
						new LevelCurrencyReward(GlobalCurrency.GEM, 50),
						new LevelCurrencyReward(GlobalCurrency.TREASURE_SHARD, 50)
				)
				.build();

		MissionContext.newBuilder(manager, 701)
				.name("Who's Next?")
				.description("Win %s games without dying")
				.games(Smash, SmashTeams)
				.xRange(1, 2)
				.tracker(GAME_WIN_WITHOUT_DYING)
				.rewards(
						new LevelExperienceReward(500),
						new LevelCurrencyReward(GlobalCurrency.GEM, 250),
						new LevelCurrencyReward(GlobalCurrency.TREASURE_SHARD, 250)
				)
				.build();

		MissionContext.<String>newBuilder(manager, 702)
				.name("Bawk Battles")
				.description("Kill %s players with Chicken Missile")
				.games(Smash, SmashTeams)
				.xRange(2, 6)
				.tracker(GAME_KILL)
				.trackerData("Chicken Missile")
				.rewards(
						new LevelExperienceReward(200),
						new LevelCurrencyReward(GlobalCurrency.GEM, 100),
						new LevelCurrencyReward(GlobalCurrency.TREASURE_SHARD, 100)
				)
				.build();

		MissionContext.newBuilder(manager, 703)
				.name("Balancing Trick")
				.description("Be airborne for %s seconds")
				.games(Smash, SmashTeams)
				.xRange(60, 120)
				.tracker(SSM_AIRBORNE)
				.rewards(
						new LevelExperienceReward(4),
						new LevelCurrencyReward(GlobalCurrency.GEM, 2),
						new LevelCurrencyReward(GlobalCurrency.TREASURE_SHARD, 2)
				)
				.build();

		// Micro Battles 800-899

		MissionContext.newBuilder(manager, 800)
				.name("Brock Breaks Blocks")
				.description("Break %s blocks")
				.games(Micro)
				.xRange(30, 60)
				.tracker(GAME_BLOCK_BREAK)
				.rewards(
						new LevelExperienceReward(6),
						new LevelCurrencyReward(GlobalCurrency.GEM, 3),
						new LevelCurrencyReward(GlobalCurrency.TREASURE_SHARD, 3)
				)
				.build();

		MissionContext.newBuilder(manager, 801)
				.name("New Ground")
				.description("Place %s blocks")
				.games(Micro)
				.xRange(30, 60)
				.tracker(GAME_BLOCK_PLACE)
				.rewards(
						new LevelExperienceReward(6),
						new LevelCurrencyReward(GlobalCurrency.GEM, 3),
						new LevelCurrencyReward(GlobalCurrency.TREASURE_SHARD, 3)
				)
				.build();

		MissionContext.newBuilder(manager, 802)
				.name("Still Kickin")
				.description("Be part of the last 2 teams alive in %s games")
				.games(Micro)
				.xRange(2, 5)
				.tracker(MICRO_LAST_TWO)
				.rewards(
						new LevelExperienceReward(50),
						new LevelCurrencyReward(GlobalCurrency.GEM, 25),
						new LevelCurrencyReward(GlobalCurrency.TREASURE_SHARD, 25)
				)
				.build();

		// The Bridges 900-999

		MissionContext.newBuilder(manager, 900)
				.name("You can't hide whats inside!")
				.description("Kill the last player on a team %s times")
				.games(Bridge)
				.xRange(1, 3)
				.tracker(BRIDGES_KILL_LAST)
				.rewards(
						new LevelExperienceReward(150),
						new LevelCurrencyReward(GlobalCurrency.GEM, 75),
						new LevelCurrencyReward(GlobalCurrency.TREASURE_SHARD, 75)
				)
				.build();

		MissionContext.<String>newBuilder(manager, 901)
				.name("An Apple A Day")
				.description("Kill %s players with Thrown Apples")
				.games(Bridge)
				.xRange(5, 10)
				.tracker(GAME_KILL)
				.trackerData("Apple Thrower")
				.rewards(
						new LevelExperienceReward(100),
						new LevelCurrencyReward(GlobalCurrency.GEM, 50),
						new LevelCurrencyReward(GlobalCurrency.TREASURE_SHARD, 50)
				)
				.build();

		MissionContext.newBuilder(manager, 902)
				.name("Resource Miner")
				.description("Break %s blocks")
				.games(Bridge)
				.xRange(50, 100)
				.tracker(GAME_BLOCK_BREAK)
				.rewards(
						new LevelExperienceReward(4),
						new LevelCurrencyReward(GlobalCurrency.GEM, 2),
						new LevelCurrencyReward(GlobalCurrency.TREASURE_SHARD, 2)
				)
				.build();

		MissionContext.newBuilder(manager, 903)
				.name("Smelter")
				.description("Smelt or Cook %s items")
				.games(Bridge)
				.xRange(25, 35)
				.tracker(GAME_FURNACE_SMELT)
				.rewards(
						new LevelExperienceReward(6),
						new LevelCurrencyReward(GlobalCurrency.GEM, 3),
						new LevelCurrencyReward(GlobalCurrency.TREASURE_SHARD, 3)
				)
				.build();

		MissionContext.<Material>newBuilder(manager, 904)
				.name("Going On The Offensive")
				.description("Craft %s Diamond Swords")
				.games(Bridge)
				.xRange(1, 3)
				.tracker(GAME_CRAFT_ITEM)
				.trackerData(Material.DIAMOND_SWORD)
				.rewards(
						new LevelExperienceReward(100),
						new LevelCurrencyReward(GlobalCurrency.GEM, 50),
						new LevelCurrencyReward(GlobalCurrency.TREASURE_SHARD, 50)
				)
				.build();

		// Skywars 1000-1099

		MissionContext.newBuilder(manager, 1000)
				.name("Classicwars")
				.description("Win %s games without using any kit abilities")
				.games(Skywars, SkywarsTeams)
				.xRange(1, 3)
				.tracker(SW_NO_ABILITIES)
				.rewards(
						new LevelExperienceReward(200),
						new LevelCurrencyReward(GlobalCurrency.GEM, 100),
						new LevelCurrencyReward(GlobalCurrency.TREASURE_SHARD, 100)
				)
				.build();

		MissionContext.<String>newBuilder(manager, 1001)
				.name("Spawner")
				.description("Loot %s spawn island chests")
				.games(Skywars, SkywarsTeams)
				.xRange(20, 30)
				.tracker(GAME_CHEST_OPEN)
				.trackerData("Island")
				.rewards(
						new LevelExperienceReward(10),
						new LevelCurrencyReward(GlobalCurrency.GEM, 5),
						new LevelCurrencyReward(GlobalCurrency.TREASURE_SHARD, 5)
				)
				.build();

		MissionContext.<String>newBuilder(manager, 1002)
				.name("Connector")
				.description("Loot %s connector island chests")
				.games(Skywars, SkywarsTeams)
				.xRange(20, 30)
				.tracker(GAME_CHEST_OPEN)
				.trackerData("Connector")
				.rewards(
						new LevelExperienceReward(10),
						new LevelCurrencyReward(GlobalCurrency.GEM, 5),
						new LevelCurrencyReward(GlobalCurrency.TREASURE_SHARD, 5)
				)
				.build();

		MissionContext.<String>newBuilder(manager, 1003)
				.name("Midder")
				.description("Loot %s middle island chests")
				.games(Skywars, SkywarsTeams)
				.xRange(20, 30)
				.tracker(GAME_CHEST_OPEN)
				.trackerData("Middle")
				.rewards(
						new LevelExperienceReward(10),
						new LevelCurrencyReward(GlobalCurrency.GEM, 5),
						new LevelCurrencyReward(GlobalCurrency.TREASURE_SHARD, 5)
				)
				.build();

		MissionContext.newBuilder(manager, 1004)
				.name("EXPLOSION")
				.description("Pickup %s Throwable TNT")
				.games(Skywars, SkywarsTeams)
				.xRange(8, 16)
				.tracker(GAME_GENERATOR_COLLECT)
				.rewards(
						new LevelExperienceReward(12),
						new LevelCurrencyReward(GlobalCurrency.GEM, 6),
						new LevelCurrencyReward(GlobalCurrency.TREASURE_SHARD, 6)
				)
				.build();

		MissionContext.newBuilder(manager, 1005)
				.name("Megimin Approved")
				.description("Throw %s Throwable TNT")
				.games(Skywars, SkywarsTeams)
				.xRange(8, 16)
				.tracker(GAME_THROW_TNT)
				.rewards(
						new LevelExperienceReward(12),
						new LevelCurrencyReward(GlobalCurrency.GEM, 6),
						new LevelCurrencyReward(GlobalCurrency.TREASURE_SHARD, 6)
				)
				.build();

		MissionContext.newBuilder(manager, 1006)
				.name("Destructor")
				.description("Break %s blocks")
				.games(Skywars, SkywarsTeams)
				.xRange(30, 60)
				.tracker(GAME_BLOCK_BREAK)
				.rewards(
						new LevelExperienceReward(6),
						new LevelCurrencyReward(GlobalCurrency.GEM, 3),
						new LevelCurrencyReward(GlobalCurrency.TREASURE_SHARD, 3)
				)
				.build();

		MissionContext.newBuilder(manager, 1007)
				.name("Skilled In The Craft")
				.description("Craft %s items")
				.games(Skywars, SkywarsTeams)
				.xRange(8, 16)
				.tracker(GAME_CRAFT_ITEM)
				.rewards(
						new LevelExperienceReward(12),
						new LevelCurrencyReward(GlobalCurrency.GEM, 6),
						new LevelCurrencyReward(GlobalCurrency.TREASURE_SHARD, 6)
				)
				.build();

		MissionContext.newBuilder(manager, 1008)
				.name("Ella Was Enchanted")
				.description("Enchant %s items")
				.games(Skywars, SkywarsTeams)
				.xRange(6, 12)
				.tracker(GAME_ENCHANT_ITEM)
				.rewards(
						new LevelExperienceReward(16),
						new LevelCurrencyReward(GlobalCurrency.GEM, 8),
						new LevelCurrencyReward(GlobalCurrency.TREASURE_SHARD, 8)
				)
				.build();

		// Dominate 1100-1199

		MissionContext.newBuilder(manager, 1100)
				.name("A Real Champion")
				.description("Earn %s points")
				.games(ChampionsDominate)
				.xRange(15000, 30000)
				.tracker(DOM_POINT)
				.scaleDownRewards()
				.rewards(
						new LevelExperienceReward(2),
						new LevelCurrencyReward(GlobalCurrency.GEM, 1),
						new LevelCurrencyReward(GlobalCurrency.TREASURE_SHARD, 1)
				)
				.build();

		MissionContext.newBuilder(manager, 1101)
				.name("Gem Hunter")
				.description("Collect %s gems")
				.games(ChampionsDominate)
				.xRange(5, 10)
				.tracker(DOM_COLLECT_GEM)
				.rewards(
						new LevelExperienceReward(20),
						new LevelCurrencyReward(GlobalCurrency.GEM, 10),
						new LevelCurrencyReward(GlobalCurrency.TREASURE_SHARD, 10)
				)
				.build();

		MissionContext.newBuilder(manager, 1102)
				.name("Playing Objective")
				.description("Capture %s objectives")
				.games(ChampionsDominate)
				.xRange(10, 15)
				.tracker(GAME_CAPTURE_POINT)
				.rewards(
						new LevelExperienceReward(20),
						new LevelCurrencyReward(GlobalCurrency.GEM, 10),
						new LevelCurrencyReward(GlobalCurrency.TREASURE_SHARD, 10)
				)
				.build();

		// Capture the Flag 1200-1299

		MissionContext.newBuilder(manager, 1200)
				.name("Rally To Me!")
				.description("Take %s flags")
				.games(ChampionsCTF)
				.xRange(10, 15)
				.tracker(CTF_TAKE)
				.rewards(
						new LevelExperienceReward(20),
						new LevelCurrencyReward(GlobalCurrency.GEM, 10),
						new LevelCurrencyReward(GlobalCurrency.TREASURE_SHARD, 10)
				)
				.build();

		MissionContext.newBuilder(manager, 1201)
				.name("Cap'ed!")
				.description("Capture %s flags")
				.games(ChampionsCTF)
				.xRange(4, 8)
				.tracker(CTF_CAPTURE)
				.rewards(
						new LevelExperienceReward(40),
						new LevelCurrencyReward(GlobalCurrency.GEM, 20),
						new LevelCurrencyReward(GlobalCurrency.TREASURE_SHARD, 20)
				)
				.build();

		// Master Builders 1300-1399

		MissionContext.newBuilder(manager, 1300)
				.name("Block Spammer")
				.description("Place %s blocks")
				.games(Build)
				.xRange(250, 500)
				.tracker(GAME_BLOCK_PLACE)
				.scaleDownRewards()
				.rewards(
						new LevelExperienceReward(50),
						new LevelCurrencyReward(GlobalCurrency.GEM, 25),
						new LevelCurrencyReward(GlobalCurrency.TREASURE_SHARD, 25)
				)
				.build();

		MissionContext.newBuilder(manager, 1301)
				.name("Da Bomb")
				.description("Rate %s other builds")
				.games(Build)
				.xRange(20, 30)
				.tracker(BUILD_RATE)
				.rewards(
						new LevelExperienceReward(12),
						new LevelCurrencyReward(GlobalCurrency.GEM, 6),
						new LevelCurrencyReward(GlobalCurrency.TREASURE_SHARD, 6)
				)
				.build();

		MissionContext.newBuilder(manager, 1302)
				.name("Mostly Sparkles")
				.description("Place %s particles")
				.games(Build)
				.xRange(30, 40)
				.tracker(BUILD_PARTICLES)
				.rewards(
						new LevelExperienceReward(8),
						new LevelCurrencyReward(GlobalCurrency.GEM, 4),
						new LevelCurrencyReward(GlobalCurrency.TREASURE_SHARD, 4)
				)
				.build();
	}

}
