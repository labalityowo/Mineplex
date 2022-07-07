package mineplex.core.achievement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mineplex.core.achievement.leveling.LevelingManager;
import mineplex.core.common.util.C;

public enum Achievement
{
	GLOBAL_MINEPLEX_LEVEL("Mineplex Level", 20000,
			new String[]{"Global.ExpEarned"},
			new String[]{"Level up by doing well in games!"},
			getExperienceLevels(),
			AchievementCategory.GLOBAL),
	
	GLOBAL_GEM_HUNTER("Gem Hunter", 10000,
			new String[]{"Global.GemsEarned"},
			new String[]{"+1 for every Gem earned in any game."},
			new int[]{10000, 15000, 20000, 25000, 30000, 35000, 40000, 45000, 50000, 55000, 60000, 65000},
			AchievementCategory.GLOBAL),
			
	GLOBAL_PUMPKIN_SMASHER_2015("2015 Pumpkin Smasher", 4000,
			new String[]{"Global.Halloween Pumpkins 2015"},
			new String[]{"Smash 200 Flaming Pumpkins,", 
			"during Halloween 2015!"},
			new int[]{200},
			AchievementCategory.HOLIDAY),

	GLOBAL_PRESENT_HOARDER_2015("2015 Present Hoarder", 4000,
			new String[]{"Global.Christmas Presents 2015"},
			new String[]{"Open 200 Christmas Presents,", 
			"during Christmas 2015!"},
			new int[]{200},
			AchievementCategory.HOLIDAY),

	GLOBAL_GIFT_GIVER_2016("2016 Gift Giver", 0,
			new String[]{"Global.Valentines2016.GiftsGiven"},
			new String[]{"Give 3 Valentines Gifts",
					"during Valentines 2016!"},
			new int[]{3},
			AchievementCategory.HOLIDAY),

	GLOBAL_GIFT_GETTER_2016("2016 People Love Me", 2000,
			new String[]{"Global.Valentines2016.GiftsReceived"},
			new String[]{"Receive 10 Gifts",
					"during Valentines 2016!"},
			new int[]{10},
			AchievementCategory.HOLIDAY),

	GLOBAL_CHICKEN_CHASER_2016("2016 Chicken Chaser", 4000,
			new String[]{"Global.Thanksgiving Chickens 2016"},
			new String[]{"Catch 200 Thanksgiving Chickens,",
					"during Thanksgiving 2016!"},
			new int[]{200},
			AchievementCategory.HOLIDAY),
	
	GLOBAL_EGG_HUNTER_2017("2017 Egg Hunter", 4000,
			new String[]{"Global.Easter Eggs 2017"},
			new String[]{"Find 35 Easter Egg Baskets,",
					"during Easter 2017"},
			new int[]{35},
			AchievementCategory.HOLIDAY),

	GLOBAL_ALIEN_INVASION("2017 Alien Hub Defender", 4000,
			new String[]{"Global.Alien Invasion 2017"},
			new String[]{"Kill 300 Aliens in the Lobby,",
					"during the Alien Invasion event"},
			new int[]{300},
			AchievementCategory.HOLIDAY),

	GLOBAL_PUMPKIN_SMASHER_2017("2017 Pumpkin Smasher", 0,
			new String[]{"Global.Halloween Pumpkins 2017"},
			new String[]{"Smash 200 Flaming Pumpkins,",
					"during Halloween 2017!"},
			new int[][]{ new int[]{0,0,8000}},
			new int[]{200},
			"",
			new String[0],
			AchievementCategory.HOLIDAY),

	//Bridges
	BRIDGES_WINS("Bridge Champion", 600,
			new String[]{"The Bridges.Wins"},
			new String[]{"Win 30 games of The Bridges"},
			new int[]{30},
			AchievementCategory.BRIDGES),

	BRIDGES_FOOD("Food for the Masses", 600,
			new String[]{"The Bridges.FoodForTheMasses"},
			new String[]{"Get 20 kills with Apples"},
			new int[]{20},
			AchievementCategory.BRIDGES),

	BRIDGES_SNIPER("Sniper", 600,
			new String[]{"The Bridges.Sniper"},
			new String[]{"Kill an enemy with Archery before Bridges fall"},
			new int[]{1},
			AchievementCategory.BRIDGES),

	BRIDGES_FORTUNE_BOMBER("Fortune Bomber", 600,
			new String[]{"The Bridges.FortuneBomber"},
			new String[]{"Mine 30 Diamond Ore using TNT"},
			new int[]{30},
			AchievementCategory.BRIDGES),

	BRIDGES_RAMPAGE("Rampage", 1200,
			new String[]{"The Bridges.Rampage"},
			new String[]{"Get 4 kills in a row, with no more than", "10 seconds between each kill"},
			new int[]{1},
			AchievementCategory.BRIDGES),

	BRIDGES_DEATH_BOMBER("Death Bomber", 1000,
			new String[]{"The Bridges.DeathBomber"},
			new String[]{"Get 5 kills in a single game with TNT"},
			new int[]{1},
			AchievementCategory.BRIDGES),

	//Survival Games
	SURVIVAL_GAMES_WINS("Katniss Everdeen", 600,
			new String[]{"Survival Games.Wins"},
			new String[]{"Win 30 games of Survival Games"},
			new int[]{30},
			AchievementCategory.SURVIVAL_GAMES),

	SURVIVAL_GAMES_LIGHT_WEIGHT("Light Weight", 1000,
			new String[]{"Survival Games.NoArmor"},
			new String[]{"Win a game without wearing any armor"},
			new int[]{1},
			AchievementCategory.SURVIVAL_GAMES),

	SURVIVAL_GAMES_BLOODLUST("Bloodlust", 1200,
			new String[]{"Survival Games.Bloodlust"},
			new String[]{"Kill 3 other players in the first minute"},
			new int[]{1},
			AchievementCategory.SURVIVAL_GAMES),

	SURVIVAL_GAMES_LOOT("Loot Hoarder", 600,
			new String[]{"Survival Games.SupplyDropsOpened"},
			new String[]{"Be the first to open 50 Supply Drops"},
			new int[]{50},
			AchievementCategory.SURVIVAL_GAMES),

	SURVIVAL_GAMES_SKELETONS("Skeletal Army", 1000,
			new String[]{"Survival Games.Skeletons"},
			new String[]{"Have 5 Necromanced Skeletons alive"},
			new int[]{1},
			AchievementCategory.SURVIVAL_GAMES),
	
	//Skywars
	SKYWARS_WINS("Sky King",2000,
			new String[]{"Skywars.Wins"},
			new String[]{"Win 20 Games of Skywars"},
			new int[]{20},
			AchievementCategory.SKYWARS),
			
	SKYWARS_BOMBER("Master Bomber",500,
			new String[]{"Skywars.DeathBomber"},
			new String[]{"Get 3 kills with \"Super Throwing TNT\"", " in a single game."},
			new int[]{1},
			AchievementCategory.SKYWARS),
					
	SKYWARS_TNT("TNT Hoarder",250,
			new String[]{"Skywars.BombPickups"},
			new String[]{"Pickup 100 \"Super Throwing TNT\"s"},
			new int[]{100},
			AchievementCategory.SKYWARS),	
			
	SKYWARS_ZOMBIE_KILLS("Left For Dead",750,
			new String[]{"Skywars.ZombieKills"},
			new String[]{"Kill 120 Zombies"},
			new int[]{120},
			AchievementCategory.SKYWARS),
			
	SKYWARS_PLAYER_KILLS("Cold Blooded Killer",500,
			new String[]{"Skywars.Kills"},
			new String[]{"Kill 80 Players"},
			new int[]{80},
			AchievementCategory.SKYWARS),
			
	SKYWARS_NOCHEST("Survivalist",1000,
			new String[]{"Skywars.NoChest"},
			new String[]{"Win a Game Without Opening a Chest"},
			new int[]{1},
			AchievementCategory.SKYWARS),
			
	SKYWARS_NOARMOR("Bare Minimum",1000,
			new String[]{"Skywars.NoArmor"},
			new String[]{"Win a Game With No Armor"},
			new int[]{1},
			AchievementCategory.SKYWARS),

	//UHC
	UHC_WINS("Ultimate Winner", 600,
			new String[]{"Ultra Hardcore.Wins"},
			new String[]{"Win 10 games of Ultra Hardcore"},
			new int[]{10},
			AchievementCategory.UHC),
	
	UHC_FOOD("Fine Dining", 1200,
			new String[]{"Ultra Hardcore.Food"},
			new String[]{"Collect and eat every type of food in a game", "Foods needed:", "Apple, Mushroom Stew, Bread, Cooked Porkchop", "Golden Apple, Cooked Fish, Cookie, Melon", "Steak, Cooked Chicken, Carrot, Cooked Potato", "Pumpkin Pie, Cooked Rabbit and Cooked Mutton"},
			new int[]{1},
			AchievementCategory.UHC),
	
	UHC_MINER("Lucky Miner", 1200,
			new String[]{"Ultra Hardcore.Miner"},
			new String[]{"Equip a full set of iron armor within 10 minutes of the game starting"},
			new int[]{1},
			AchievementCategory.UHC),
	
	UHC_HOE("I Don\'t Need This", 1200,
			new String[]{"Ultra Hardcore.Hoe"},
			new String[]{"Craft a diamond hoe"},
			new int[]{1},
			AchievementCategory.UHC),
	
	UHC_DIE("I Will Not Die!", 1200,
			new String[]{"Ultra Hardcore.Die"},
			new String[]{"Drop down to half a heart before healing back up"},
			new int[]{1},
			AchievementCategory.UHC),
	
	UHC_SPEED_FOOD("Fine Dining", 1200,
			new String[]{"Ultra Hardcore Speed.Food"},
			new String[]{"Collect and eat every type of food in a game", "Foods needed:", "Apple, Mushroom Stew, Bread, Cooked Porkchop", "Golden Apple, Cooked Fish, Cookie, Melon", "Steak, Cooked Chicken, Carrot, Cooked Potato", "Pumpkin Pie, Cooked Rabbit and Cooked Mutton"},
			new int[]{1},
			AchievementCategory.UHC_SPEED),
	
	UHC_SPEED_MINER("Lucky Miner", 1200,
			new String[]{"Ultra Hardcore Speed.Miner"},
			new String[]{"Equip a full set of iron armor within 10 minutes of the game starting"},
			new int[]{1},
			AchievementCategory.UHC_SPEED),
	
	UHC_SPEED_HOE("I Don\'t Need This", 1200,
			new String[]{"Ultra Hardcore Speed.Hoe"},
			new String[]{"Craft a diamond hoe"},
			new int[]{1},
			AchievementCategory.UHC_SPEED),
	
	UHC_SPEED_DIE("I Will Not Die!", 1200,
			new String[]{"Ultra Hardcore Speed.Die"},
			new String[]{"Drop down to half a heart before healing back up"},
			new int[]{1},
			AchievementCategory.UHC_SPEED),
	
	//MC League
	/*MC_LEAGUE_STRIKE("First Strike", 600,
			new String[] {"MC League.FirstStrike"},
			new String[] {"Earn 30 First Bloods"},
			new int[] {30},
			AchievementCategory.MC_LEAGUE),
	
	MC_LEAGUE_HEAD("Head Hunter", 600,
			new String[] {"MC League.HeadHunter"},
			new String[] {"Grab 25 Wither Skulls"},
			new int[] {25},
			AchievementCategory.MC_LEAGUE),
	
	MC_LEAGUE_ALTAR("Altar Builder", 600,
			new String[] {"MC League.AltarBuilder"},
			new String[] {"Place 50 Wither Skulls", "on your Altar"},
			new int[] {50},
			AchievementCategory.MC_LEAGUE),
			
	MC_LEAGUE_WINS("Mineplex Champion", 900,
			new String[] {"MC League.Wins"},
			new String[] {"Win 25 Games"},
			new int[] {25},
			AchievementCategory.MC_LEAGUE),
			
	MC_LEAGUE_TOWER("Tower Defender", 800,
			new String[] {"MC League.TowerDefender"},
			new String[] {"Get a double kill", "inside your Active Tower"},
			new int[] {1},
			AchievementCategory.MC_LEAGUE),
			
	MC_LEAGUE_SAVING("Saving Up", 900,
			new String[] {"MC League.SavingUp"},
			new String[] {"Craft a Diamond Chestplate"},
			new int[] {1},
			AchievementCategory.MC_LEAGUE),*/
			
	//UHC
	WIZARDS_WINS("Supreme Wizard", 600,
			new String[]{"Wizards.Wins"},
			new String[]{"Win 50 games of Wizards"},
			new int[]{50},
			AchievementCategory.WIZARDS),

	//Smash Mobs
	SMASH_MOBS_WINS("SO SUPER!", 600,
			new String[]{"Super Smash Mobs.Wins"},
			new String[]{"Win 100 games of Super Smash Mobs"},
			new int[]{100},
			AchievementCategory.SMASH_MOBS),

	SMASH_MOBS_MLG_PRO("MLG Pro", 1200,
			new String[]{"Super Smash Mobs.MLGPro"},
			new String[]{"Win a game without dying"},
			new int[]{1},
			AchievementCategory.SMASH_MOBS),

	SMASH_MOBS_FREE_KITS("Free Kits Forever", 800,
			new String[]{"Super Smash Mobs.FreeKitsForever"},
			new String[]{"Win 100 games using only Free Kits"},
			new int[]{100},
			AchievementCategory.SMASH_MOBS),

	SMASH_MOBS_1V3("1v3", 2000,
			new String[]{"Super Smash Mobs.1v3"},
			new String[]{"Get 10 kills in a game with 4 players"},
			new int[]{1},
			AchievementCategory.SMASH_MOBS),

	SMASH_MOBS_TRIPLE_KILL("Triple Kill", 1200,
			new String[]{"Super Smash Mobs.TripleKill"},
			new String[]{"Get 3 kills in a row, with no more than", "10 seconds between each kill"},
			new int[]{1},
			AchievementCategory.SMASH_MOBS),

	SMASH_MOBS_RECOVERY_MASTER("Recovery Master", 800,
			new String[]{"Super Smash Mobs.RecoveryMaster"},
			new String[]{"Take 200 damage in a single life"},
			new int[]{1},
			AchievementCategory.SMASH_MOBS),

	//Block Hunt
	BLOCK_HUNT_WINS("The Blockiest Block", 600,
			new String[]{"Block Hunt.Wins"},
			new String[]{"Win 50 games of Block Hunt"},
			new int[]{50},
			AchievementCategory.BLOCK_HUNT),

	BLOCK_HUNT_HUNTER_KILLER("Hunter Killer", 1200,
			new String[]{"Block Hunt.HunterKiller"},
			new String[]{"Kill 10 Hunters in a single game"},
			new int[]{1},
			AchievementCategory.BLOCK_HUNT),

	BLOCK_HUNT_MEOW("Meow Meow Meow Meow", 800,
			new String[]{"Block Hunt.Meow"},
			new String[]{"Meow 50 times in a single game"},
			new int[]{1},
			AchievementCategory.BLOCK_HUNT),

	BLOCK_HUNT_HUNTER_OF_THE_YEAR("Hunter of the Year", 1200,
			new String[]{"Block Hunt.HunterOfTheYear"},
			new String[]{"Kill 7 Hiders in a single game"},
			new int[]{1},
			AchievementCategory.BLOCK_HUNT),

	BLOCK_HUNT_BAD_HIDER("Bad Hider", 1000,
			new String[]{"Block Hunt.BadHider"},
			new String[]{"Win as Hider without disguising"},
			new int[]{1},
			AchievementCategory.BLOCK_HUNT),

	//Draw My Thing
	DRAW_MY_THING_WINS("Art Hipster", 600,
			new String[]{"Draw My Thing.Wins"},
			new String[]{"Win 50 games of Draw My Thing"},
			new int[]{50},
			AchievementCategory.DRAW_MY_THING),

	DRAW_MY_THING_MR_SQUIGGLE("Mr. Squiggle", 800,
			new String[]{"Draw My Thing.MrSquiggle"},
			new String[]{"Both your drawings are guessed", "within the first 15 seconds."},
			new int[]{1},
			AchievementCategory.DRAW_MY_THING),

	DRAW_MY_THING_KEEN_EYE("Keen Eye", 1200,
			new String[]{"Draw My Thing.KeenEye"},
			new String[]{"Guess every single drawing in a game"},
			new int[]{1},
			AchievementCategory.DRAW_MY_THING),

	DRAW_MY_THING_PURE_LUCK("Pure Luck", 800,
			new String[]{"Draw My Thing.PureLuck"},
			new String[]{"Guess a word in the first 8 seconds"},
			new int[]{1},
			AchievementCategory.DRAW_MY_THING),

	// Master Builders
	MASTER_BUILDER_WINS("Master Builder", 1000,
			new String[]{"Master Builders.Wins"},
			new String[]{"Win 30 games of Master Builders"},
			new int[]{30},
			AchievementCategory.MASTER_BUILDERS),
	
	//Castle Siege
	CASTLE_SIEGE_WINS("FOR THE KING!", 0,
			new String[]{"Castle Siege.ForTheKing"},
			new String[]{"Win as Defenders 50 times"},
			new int[][]{ new int[]{0,0,10000}},
			new int[]{50},
			"",
			new String[0],
			AchievementCategory.CASTLE_SIEGE),

	CASTLE_SIEGE_KINGSLAYER("Kingslayer", 0,
			new String[]{"Castle Siege.KingSlayer"},
			new String[]{"Get the killing blow on the King"},
			new int[][]{ new int[]{0,0,4000}},
			new int[]{1},
			"",
			new String[0],
			AchievementCategory.CASTLE_SIEGE),

	CASTLE_SIEGE_BLOOD_THIRSTY("Blood Thirsty", 0,
			new String[]{"Castle Siege.BloodThirsty"},
			new String[]{"Kill 50 Undead in a single game"},
			new int[][]{ new int[]{0,0,8000}},
			new int[]{1},
			"",
			new String[0],
			AchievementCategory.CASTLE_SIEGE),

	CASTLE_SIEGE_ASSASSIN("Assassin", 0,
			new String[]{"Castle Siege.Assassin"},
			new String[]{"Do 50% or more of the damage to the king"},
			new int[][]{ new int[]{0,0,8000}},
			new int[]{1},
			"",
			new String[0],
			AchievementCategory.CASTLE_SIEGE),

	CASTLE_SIEGE_CLOSE_CALL("Slash or Burn", 0,
			new String[]{"Castle Siege.CloseCall"},
			new String[]{"Win the Game as Undead within the last 70 seconds of the game"},
			new int[][]{ new int[]{0,0,8000}},
			new int[]{1},
			"",
			new String[0],
			AchievementCategory.CASTLE_SIEGE),

	CASTLE_SIEGE_WOLF_KILL("Canine Revenge", 0,
			new String[]{"Castle Siege.WolfKill"},
			new String[]{"As a Castle Wolf, Kill 12 Undead in One Game"},
			new int[][]{ new int[]{0,0,7000}},
			new int[]{1},
			"",
			new String[0],
			AchievementCategory.CASTLE_SIEGE),

	CASTLE_SIEGE_KING_GUARD("Royal Guard", 0,
			new String[]{"Castle Siege.KingGuard"},
			new String[]{"Kill 5 Undead within 8 blocks of the King in the last 70 seconds"},
			new int[][]{ new int[]{0,0,8000}},
			new int[]{1},
			"",
			new String[0],
			AchievementCategory.CASTLE_SIEGE),

	CASTLE_SIEGE_KING_FULL("Not Even a Scratch", 0,
			new String[]{"Castle Siege.KingFull"},
			new String[]{"Win the Game as Defense with the King at Full Health"},
			new int[][]{ new int[]{0,0,8000}},
			new int[]{1},
			"",
			new String[0],
			AchievementCategory.CASTLE_SIEGE),

	CASTLE_SIEGE_FIRST_BLOOD("Vigilante", 0,
			new String[]{"Castle Siege.FirstBlood"},
			new String[]{"Get 5 First Bloods"},
			new int[][]{ new int[]{0,0,6000}},
			new int[]{5},
			"",
			new String[0],
			AchievementCategory.CASTLE_SIEGE),

	CASTLE_SIEGE_TNT_KILLER("Defusal Squadron", 0,
			new String[]{"Castle Siege.TNTKiller"},
			new String[]{"Kill 5 TNT Carriers in One Game as Defenders"},
			new int[][]{ new int[]{0,0,7000}},
			new int[]{1},
			"",
			new String[0],
			AchievementCategory.CASTLE_SIEGE),

	CASTLE_SIEGE_HORSE_KILLER("Equestrian Elimination", 0,
			new String[]{"Castle Siege.HorseKiller"},
			new String[]{"Kill 25 Horses"},
			new int[][]{ new int[]{0,0,6000}},
			new int[]{25},
			"",
			new String[0],
			AchievementCategory.CASTLE_SIEGE),

	//Castle Assault
	CASTLE_ASSAULT_KILL_STREAK("Kill Streak", 0,
			new String[]{"Castle Assault.KillStreak", "Castle Assault TDM.KillStreak"},
			new String[]{"Earn Kill Streak Rewards"},
			new int[][]{new int[]{0, 50, 500}, new int[]{0, 100, 750}, new int[]{0, 150, 1000}, new int[]{0, 200, 1500}, new int[]{0, 400, 2000}, new int[]{0, 500, 2500}, new int[]{0, 1000, 3000}, new int[]{0, 1500, 3500}, new int[]{0, 2000, 4000}, new int[]{0, 5000, 100000}},
			new int[]{10, 20, 50, 100, 200, 250, 500, 750, 1000, 2000},
			"Initiate",
			new String[]{"Novice I", "Novice II", "Novice III", "Novice IV", "Novice V", "Master I", "Master II", "Master III", "Master IV", "GRANDMASTER"},
			AchievementCategory.CASTLE_ASSAULT),
	
	CASTLE_ASSAULT_FIRST_BLOOD("First Blood", 0,
			new String[]{"Castle Assault.FirstBlood", "Castle Assault TDM.FirstBlood"},
			new String[]{"Obtain the first kill in a Match"},
			new int[][]{new int[]{0, 100, 100}, new int[]{0, 150, 200}, new int[]{0, 200, 300}, new int[]{0, 250, 400}, new int[]{0, 500, 500}},
			new int[]{2, 5, 10, 25, 50},
			"Initiate",
			new String[]{"Novice I", "Novice II", "Novice III", "Novice IV", "Novice V"},
			AchievementCategory.CASTLE_ASSAULT),
	
	CASTLE_ASSAULT_FIGHTER_KIT("Fighter", 0,
			new String[]{"Castle Assault.FighterKitKills", "Castle Assault TDM.FighterKitKills"},
			new String[]{"Kill opponents while wearing the Fighter Kit"},
			new int[][]{new int[]{0, 100, 500}, new int[]{0, 150, 750}, new int[]{0, 250, 1000}, new int[]{0, 500, 1500}, new int[]{0, 1000, 2500}, new int[]{0, 1500, 3500}, new int[]{0, 2000, 4500}, new int[]{0, 3000, 6000}, new int[]{0, 5000, 10000}, new int[]{0, 10000, 100000}},
			new int[]{50, 100, 250, 500, 1000, 1500, 3000, 5000, 10000, 20000},
			"Initiate",
			new String[]{"Novice I", "Novice II", "Novice III", "Novice IV", "Novice V", "Master I", "Master II", "Master III", "Master IV", "GRANDMASTER"},
			AchievementCategory.CASTLE_ASSAULT),
	
	CASTLE_ASSAULT_TANK_KIT("Tank", 0,
			new String[]{"Castle Assault.TankKitKills", "Castle Assault TDM.TankKitKills"},
			new String[]{"Kill opponents while wearing the Tank Kit"},
			new int[][]{new int[]{0, 100, 500}, new int[]{0, 150, 750}, new int[]{0, 250, 1000}, new int[]{0, 500, 1500}, new int[]{0, 1000, 2500}, new int[]{0, 1500, 3500}, new int[]{0, 2000, 4500}, new int[]{0, 3000, 6000}, new int[]{0, 5000, 10000}, new int[]{0, 10000, 100000}},
			new int[]{50, 100, 250, 500, 1000, 1500, 3000, 5000, 10000, 20000},
			"Initiate",
			new String[]{"Novice I", "Novice II", "Novice III", "Novice IV", "Novice V", "Master I", "Master II", "Master III", "Master IV", "GRANDMASTER"},
			AchievementCategory.CASTLE_ASSAULT),
	
	CASTLE_ASSAULT_ARCHER_KIT("Archer", 0,
			new String[]{"Castle Assault.ArcherKitKills", "Castle Assault TDM.ArcherKitKills"},
			new String[]{"Kill opponents while wearing the Archer Kit"},
			new int[][]{new int[]{0, 100, 500}, new int[]{0, 150, 750}, new int[]{0, 250, 1000}, new int[]{0, 500, 1500}, new int[]{0, 1000, 2500}, new int[]{0, 1500, 3500}, new int[]{0, 2000, 4500}, new int[]{0, 3000, 6000}, new int[]{0, 5000, 10000}, new int[]{0, 10000, 100000}},
			new int[]{50, 100, 250, 500, 1000, 1500, 3000, 5000, 10000, 20000},
			"Initiate",
			new String[]{"Novice I", "Novice II", "Novice III", "Novice IV", "Novice V", "Master I", "Master II", "Master III", "Master IV", "GRANDMASTER"},
			AchievementCategory.CASTLE_ASSAULT),
	
	CASTLE_ASSAULT_DEMOLITIONIST_KIT("Demolitionist", 0,
			new String[]{"Castle Assault.DemolitionistKitKills", "Castle Assault TDM.DemolitionistKitKills"},
			new String[]{"Kill opponents while wearing the Demolitionist Kit"},
			new int[][]{new int[]{0, 100, 500}, new int[]{0, 150, 750}, new int[]{0, 250, 1000}, new int[]{0, 500, 1500}, new int[]{0, 1000, 2500}, new int[]{0, 1500, 3500}, new int[]{0, 2000, 4500}, new int[]{0, 3000, 6000}, new int[]{0, 5000, 10000}, new int[]{0, 10000, 100000}},
			new int[]{50, 100, 250, 500, 1000, 1500, 3000, 5000, 10000, 20000},
			"Initiate",
			new String[]{"Novice I", "Novice II", "Novice III", "Novice IV", "Novice V", "Master I", "Master II", "Master III", "Master IV", "GRANDMASTER"},
			AchievementCategory.CASTLE_ASSAULT),
	
	CASTLE_ASSAULT_ALCHEMIST_KIT("Alchemist", 0,
			new String[]{"Castle Assault.AlchemistKitKills", "Castle Assault TDM.AlchemistKitKills"},
			new String[]{"Kill opponents while wearing the Alchemist Kit"},
			new int[][]{new int[]{0, 100, 500}, new int[]{0, 150, 750}, new int[]{0, 250, 1000}, new int[]{0, 500, 1500}, new int[]{0, 1000, 2500}, new int[]{0, 1500, 3500}, new int[]{0, 2000, 4500}, new int[]{0, 3000, 6000}, new int[]{0, 5000, 10000}, new int[]{0, 10000, 100000}},
			new int[]{50, 100, 250, 500, 1000, 1500, 3000, 5000, 10000, 20000},
			"Initiate",
			new String[]{"Novice I", "Novice II", "Novice III", "Novice IV", "Novice V", "Master I", "Master II", "Master III", "Master IV", "GRANDMASTER"},
			AchievementCategory.CASTLE_ASSAULT),
	
	CASTLE_ASSAULT_WINNER("Assault", 0,
			new String[]{"Castle Assault.Wins", "Castle Assault TDM.Wins"},
			new String[]{"Win games of Castle Assault"},
			new int[][]{new int[]{0, 100, 500}, new int[]{0, 150, 750}, new int[]{0, 250, 1000}, new int[]{0, 500, 1500}, new int[]{0, 1000, 2500}, new int[]{0, 1500, 3500}, new int[]{0, 2000, 4500}, new int[]{0, 3000, 6000}, new int[]{0, 5000, 10000}, new int[]{0, 10000, 100000}},
			new int[]{2, 5, 25, 50, 100, 150, 250, 500, 1000, 2000},
			"Initiate",
			new String[]{"Novice I", "Novice II", "Novice III", "Novice IV", "Novice V", "Master I", "Master II", "Master III", "Master IV", "GRANDMASTER"},
			AchievementCategory.CASTLE_ASSAULT),

	//Champions
	CHAMPIONS_WINS("Champion", 600,
			new String[]{"Champions Domination.Wins", "Champions TDM.Wins", "Champions CTF.Wins"},
			new String[]{"Win 80 games of Dominate, TDM, or CTF"},
			new int[]{80},
			AchievementCategory.CHAMPIONS),

	CHAMPIONS_ASSASSINATION("Assassination", 1000,
			new String[]{"Champions Domination.Assassination", "Champions TDM.Assassination", "Champions CTF.Assassination"},
			new String[]{"Kill 40 players with Backstab without", "taking any damage from them"},
			new int[]{40},
			AchievementCategory.CHAMPIONS),

	CHAMPIONS_MASS_ELECTROCUTION("Mass Electrocution", 1200,
			new String[]{"Champions Domination.MassElectrocution", "Champions TDM.MassElectrocution", "Champions CTF.MassElectrocution"},
			new String[]{"Hit 4 enemies with a Lightning Orb"},
			new int[]{1},
			AchievementCategory.CHAMPIONS),

	CHAMPIONS_THE_LONGEST_SHOT("The Longest Shot", 1200,
			new String[]{"Champions Domination.TheLongestShot", "Champions TDM.TheLongestShot", "Champions CTF.TheLongestShot"},
			new String[]{"Kill someone using Longshot who", "is over 64 Blocks away from you"},
			new int[]{1},
			AchievementCategory.CHAMPIONS),

	CHAMPIONS_EARTHQUAKE("Earthquake", 1200,
			new String[]{"Champions Domination.Earthquake", "Champions TDM.Earthquake", "Champions CTF.Earthquake"},
			new String[]{"Launch 5 enemies using Seismic Slam"},
			new int[]{1},
			AchievementCategory.CHAMPIONS),
			
	CHAMPIONS_CAPTURES("Sticky Fingers", 2500,
			new String[]{"Champions CTF.Captures"},
			new String[]{"Capture the Enemy Flag 20 times"},
			new int[]{20},
			AchievementCategory.CHAMPIONS),
			
	CHAMPIONS_CLUTCH("Clutch", 600,
			new String[]{"Champions CTF.Clutch"},
			new String[]{"Kill the Enemy Flag Carrier in Sudden Death"},
			new int[]{1},
			AchievementCategory.CHAMPIONS),
			
	CHAMPIONS_SPECIAL_WIN("Champion of Champions", 3000,
			new String[]{"Champions CTF.SpecialWin"},
			new String[]{"Win the game with 5 more flag captures than the other team"},
			new int[]{1},
			AchievementCategory.CHAMPIONS),

	//Paintball
	SUPER_PAINTBALL_WINS("Paintball King", 600,
			new String[]{"Super Paintball.Wins"},
			new String[]{"Win 50 games of Paintball"},
			new int[]{50},
			AchievementCategory.SUPER_PAINTBALL),

	SUPER_PAINTBALL_KILLING_SPREE("Killing Spree", 1200,
			new String[]{"Super Paintball.KillingSpree"},
			new String[]{"Get 4 kills in a row, with no more than", "5 seconds between each kill"},
			new int[]{1},
			AchievementCategory.SUPER_PAINTBALL),

	SUPER_PAINTBALL_FLAWLESS_VICTORY("Flawless Victory", 1000,
			new String[]{"Super Paintball.Wins"},
			new String[]{"Win a game with your entire team alive"},
			new int[]{1},
			AchievementCategory.SUPER_PAINTBALL),

	SUPER_PAINTBALL_MEDIC("Medic!", 800,
			new String[]{"Super Paintball.Medic"},
			new String[]{"Revive 200 team members"},
			new int[]{200},
			AchievementCategory.SUPER_PAINTBALL),

	SUPER_PAINTBALL_SPEEDRUNNER("Speedrunner", 1000,
			new String[]{"Super Paintball.Speedrunner"},
			new String[]{"Win a game in 30 seconds"},
			new int[]{1},
			AchievementCategory.SUPER_PAINTBALL),

	SUPER_PAINTBALL_LAST_STAND("Last Stand", 1200,
			new String[]{"Super Paintball.LastStand"},
			new String[]{"Be the last alive on your team", "and kill 3 enemy players"},
			new int[]{1},
			AchievementCategory.SUPER_PAINTBALL),
			
	//Sheep Quest
	SHEEP_QUEST_WINS("Hungry Hungry Hippo", 600,
			new String[]{"Sheep Quest.Wins"},
			new String[]{"Win 50 games of Sheep Quest"},
			new int[]{50},
			AchievementCategory.SHEEP_QUEST),

	SHEEP_QUEST_THIEF("Thief", 800,
			new String[]{"Sheep Quest.Thief"},
			new String[]{"Steal 300 Sheep from enemy pens"},
			new int[]{300},
			AchievementCategory.SHEEP_QUEST),

	SHEEP_QUEST_ANIMAL_RESCUE("Animal Rescue", 800,
			new String[]{"Sheep Quest.AnimalRescue"},
			new String[]{"Make 300 enemies drop their Sheep"},
			new int[]{300},
			AchievementCategory.SHEEP_QUEST),

	SHEEP_QUEST_SELFISH("Selfish", 1200,
			new String[]{"Sheep Quest.Selfish"},
			new String[]{"Win with more than 12 Sheep"},
			new int[]{1},
			AchievementCategory.SHEEP_QUEST),

	//Snake
	SNAKE_WINS("Nokia 3310", 600,
			new String[]{"Snake.Wins"},
			new String[]{"Win 50 games of Snake"},
			new int[]{50},
			AchievementCategory.SNAKE),

	SNAKE_CANNIBAL("Cannibal", 1600,
			new String[]{"Snake.Cannibal"},
			new String[]{"Kill 6 players in a single game"},
			new int[]{1},
			AchievementCategory.SNAKE),

	SNAKE_CHOO_CHOO("Choo Choo", 1000,
			new String[]{"Snake.ChooChoo"},
			new String[]{"Grow to be 60 Sheep or longer"},
			new int[]{1},
			AchievementCategory.SNAKE),

	SNAKE_SLIMY_SHEEP("Slimy Sheep", 800,
			new String[]{"Snake.SlimySheep"},
			new String[]{"Eat 20 slimes in a single game"},
			new int[]{1},
			AchievementCategory.SNAKE),

	//Dragons
	DRAGONS_WINS("Dragon Tamer", 600,
			new String[]{"Dragons.Wins"},
			new String[]{"Win 50 games of Dragons"},
			new int[]{50},
			AchievementCategory.DRAGONS),

	DRAGONS_SPARKLEZ("Sparklez", 400,
			new String[]{"Dragons.Sparklez"},
			new String[]{"Throw 100 Sparklers"},
			new int[]{100},
			AchievementCategory.DRAGONS),

	//Turf Wars
	TURF_WARS_WINS("Turf Master 3000", 600,
			new String[]{"Turf Wars.Wins"},
			new String[]{"Win 50 games of Turf Wars"},
			new int[]{50},
			AchievementCategory.TURF_WARS),

	TURF_WARS_SHREDDINATOR("The Shreddinator", 800,
			new String[]{"Turf Wars.TheShreddinator"},
			new String[]{"Destroy 2000 blocks as Shredder"},
			new int[]{2000},
			AchievementCategory.TURF_WARS),

	TURF_WARS_BEHIND_ENEMY_LINES("Behind Enemy Lines", 1000,
			new String[]{"Turf Wars.BehindEnemyLines"},
			new String[]{"Stay on enemy turf for 15 seconds"},
			new int[]{1},
			AchievementCategory.TURF_WARS),

	TURF_WARS_COMEBACK("The Comeback", 2000,
			new String[]{"Turf Wars.TheComeback"},
			new String[]{"Win a game after having 5 or less turf"},
			new int[]{1},
			AchievementCategory.TURF_WARS),

	//Death Tag
	DEATH_TAG_WINS("Death Proof", 600,
			new String[]{"Death Tag.Wins"},
			new String[]{"Win 50 games of Death Tag"},
			new int[]{50},
			AchievementCategory.DEATH_TAG),

	DEATH_TAG_COME_AT_ME_BRO("Come At Me Bro!", 1200,
			new String[]{"Death Tag.ComeAtMeBro"},
			new String[]{"Kill 2 Undead Chasers in a single game"},
			new int[]{1},
			AchievementCategory.DEATH_TAG),

	//Runner
	RUNNER_WINS("Hot Feet", 600,
			new String[]{"Runner.Wins"},
			new String[]{"Win 50 games of Runner"},
			new int[]{50},
			AchievementCategory.RUNNER),

	RUNNER_MARATHON_RUNNER("Marathon Runner", 1000,
			new String[]{"Runner.MarathonRunner"},
			new String[]{"Run over 20,000 blocks"},
			new int[]{20000},
			AchievementCategory.RUNNER),

	//Dragon Escape
	DRAGON_ESCAPE_WINS("Douglas Defeater", 600,
			new String[]{"Dragon Escape.Wins"},
			new String[]{"Win 50 games of Dragon Escape"},
			new int[]{50},
			AchievementCategory.DRAGON_ESCAPE),

	DRAGON_ESCAPE_PARALYMPICS("Paralympics", 1200,
			new String[]{"Dragon Escape.Wins"},
			new String[]{"Win a game without using Leap"},
			new int[]{1},
			AchievementCategory.DRAGON_ESCAPE),

	DRAGON_ESCAPE_SKYLANDS("Skylands Master", 1000,
			new String[]{"Dragon Escape.Win.Skylands"},
			new String[]{"Win by finishing Skylands 5 times"},
			new int[]{5},
			AchievementCategory.DRAGON_ESCAPE),

	DRAGON_ESCAPE_THROUGH_HELL("To Hell and Back", 1000,
			new String[]{"Dragon Escape.Win.Through Hell"},
			new String[]{"Win by finishing Through Hell 5 times"},
			new int[]{5},
			AchievementCategory.DRAGON_ESCAPE),

	DRAGON_ESCAPE_PIRATE_BAY("Plundered", 1000,
			new String[]{"Dragon Escape.Win.Pirate Bay"},
			new String[]{"Win by finishing Pirate Bay 5 times"},
			new int[]{5},
			AchievementCategory.DRAGON_ESCAPE),

	//OITQ
	OITQ_WINS("One of a Kind", 600,
			new String[]{"One in the Quiver.Wins"},
			new String[]{"Win 50 games of One in the Quiver"},
			new int[]{50},
			AchievementCategory.ONE_IN_THE_QUIVER),

	OITQ_PERFECTIONIST("The Perfect Game", 3000,
			new String[]{"One in the Quiver.Perfectionist"},
			new String[]{"Win without dying"},
			new int[]{1},
			AchievementCategory.ONE_IN_THE_QUIVER),

	OITQ_SHARPSHOOTER("SharpShooter", 1200,
			new String[]{"One in the Quiver.Sharpshooter"},
			new String[]{"Hit with 8 Arrows in a row"},
			new int[]{1},
			AchievementCategory.ONE_IN_THE_QUIVER),

	OITQ_WHATS_A_BOW("What's A Bow?", 1200,
			new String[]{"One in the Quiver.WhatsABow"},
			new String[]{"Win a game without using a bow"},
			new int[]{1},
			AchievementCategory.ONE_IN_THE_QUIVER),

	//Super Spleef
	SPLEEF_WINS("Spleef King (or Queen)", 600,
			new String[]{"Super Spleef.Wins"},
			new String[]{"Win 50 games of Super Spleef"},
			new int[]{50},
			AchievementCategory.SPLEEF),

	SPLEEF_DEMOLITIONIST("Demolitionist", 1000,
			new String[]{"Super Spleef.SpleefBlocks"},
			new String[]{"Destroy 20,000 blocks."},
			new int[]{20000},
			AchievementCategory.SPLEEF),

	//Bacon Brawl
	BACON_BRAWL_WINS("King of Bacon", 600,
			new String[]{"Bacon Brawl.Wins"},
			new String[]{"Win 50 games of Bacon Brawl"},
			new int[]{50},
			AchievementCategory.BACON_BRAWL),

	BACON_BRAWL_KILLS_IN_GAME("Pigs In Blankets", 800,
			new String[]{"Bacon Brawl.KillsInGame"},
			new String[]{"Kill 6 players in one game."},
			new int[]{1},
			AchievementCategory.BACON_BRAWL),

	//Sneaky Assassins
	SNEAKY_ASSASSINS_WINS("So So Sneaky", 600,
			new String[]{"Sneaky Assassins.Wins"},
			new String[]{"Win 50 games of Sneaky Assassins"},
			new int[]{50},
			AchievementCategory.SNEAKY_ASSASSINS),

	SNEAK_ASSASSINS_MASTER_ASSASSIN("Master Assassin", 600,
			new String[]{"Sneaky Assassins.MasterAssassin"},
			new String[]{"Get Master Assassin 10 times"},
			new int[]{10},
			AchievementCategory.SNEAKY_ASSASSINS),

	SNEAK_ASSASSINS_THE_MASTERS_MASTER("The Master's Master", 700,
			new String[]{"Sneaky Assassins.TheMastersMaster"},
			new String[]{"Kill a Master Assassin without", "having a single power-up."},
			new int[]{1},
			AchievementCategory.SNEAKY_ASSASSINS),

	SNEAK_ASSASSINS_INCOMPETENCE("Incompetence", 600,
			new String[]{"Sneaky Assassins.Incompetence"},
			new String[]{"Kill 200 NPCs."},
			new int[]{200},
			AchievementCategory.SNEAKY_ASSASSINS),

	SNEAK_ASSASSINS_I_SEE_YOU("I See You", 800,
			new String[]{"Sneaky Assassins.ISeeYou"},
			new String[]{"Reveal 50 players."},
			new int[]{50},
			AchievementCategory.SNEAKY_ASSASSINS),

	//Micro Battle
	MICRO_BATTLE_WINS("Micro Champion", 600,
			new String[]{"Micro Battle.Wins"},
			new String[]{"Win 100 games of Micro Battle"},
			new int[]{100},
			AchievementCategory.MICRO_BATTLE),

	MICRO_BATTLE_ANNIHILATION("Annihilation", 1200,
			new String[]{"Micro Battle.Annihilation"},
			new String[]{"Kill 8 players in one game"},
			new int[]{1},
			AchievementCategory.MICRO_BATTLE),

	//MineStrike
	MINE_STRIKE_WINS("Striker", 800,
			new String[]{"MineStrike.Wins"},
			new String[]{"Win 50 games of MineStrike"},
			new int[]{50},
			AchievementCategory.MINE_STRIKE),

	MINE_STRIKE_BOOM_HEADSHOT("BOOM! HEADSHOT!", 800,
			new String[]{"MineStrike.BoomHeadshot"},
			new String[]{"Kill 500 people with headshots"},
			new int[]{500},
			AchievementCategory.MINE_STRIKE),

	MINE_STRIKE_ACE("Ace", 2000,
			new String[]{"MineStrike.Ace"},
			new String[]{"Get the kill on all enemies in a single round"},
			new int[]{1},
			AchievementCategory.MINE_STRIKE),

	MINE_STRIKE_KABOOM("Kaboom!", 1000,
			new String[]{"MineStrike.Kaboom"},
			new String[]{"Kill two people with a single", "High Explosive Grenade"},
			new int[]{1},
			AchievementCategory.MINE_STRIKE),

	MINE_STRIKE_ASSASSINATION("Assassination", 800,
			new String[]{"MineStrike.Assassination"},
			new String[]{"Get 20 backstab kills with the knife"},
			new int[]{20},
			AchievementCategory.MINE_STRIKE),

	MINE_STRIKE_CLUTCH_OR_KICK("Clutch or Kick", 1000,
			new String[]{"MineStrike.ClutchOrKick"},
			new String[]{"Be the last one alive, and kill", "3 or more enemies to achieve victory"},
			new int[]{1},
			AchievementCategory.MINE_STRIKE),

	MINE_STRIKE_KILLING_SPREE("Killing Spree", 1000,
			new String[]{"MineStrike.KillingSpree"},
			new String[]{"Kill 4 enemies in a row with no more", "than 5 seconds between each kill"},
			new int[]{1},
			AchievementCategory.MINE_STRIKE),

	MINE_STRIKE_BLINDFOLDED("Blindfolded", 800,
			new String[]{"MineStrike.Blindfolded"},
			new String[]{"Kill 2 enemies while blinded from", "a single flashbang"},
			new int[]{1},
			AchievementCategory.MINE_STRIKE),
			
	//Bawk Bawk Battles
	BAWK_BAWK_BATTLES_VETERAN("Veteran", 1000,
			new String[]{"Bawk Bawk Battles.Veteran"},
			new String[]{"Win 50 games of Bawk Bawk Battles"},
			new int[] {50},
			AchievementCategory.BAWK_BAWK_BATTLES),

	BAWK_BAWK_BATTLES_PINATA_MASTER("Pinata Master", 1000,
			new String[]{"Bawk Bawk Battles.PinataMaster"},
			new String[]{C.cGray + "Chicken Shooting Challenge", "Shoot 500 chickens"},
			new int[] {500},
			AchievementCategory.BAWK_BAWK_BATTLES),

	BAWK_BAWK_BATTLES_SURF_UP("Surf Up", 1000,
			new String[]{"Bawk Bawk Battles.SurfUp"},
			new String[]{C.cGray + "Wave Crush Challenge", "Avoid 500 waves"},
			new int[] {500},
			AchievementCategory.BAWK_BAWK_BATTLES),

	BAWK_BAWK_BATTLES_MILK_MAN("Milk Man", 600,
			new String[]{"Bawk Bawk Battles.MilkMan"},
			new String[]{C.cGray + "Milk A Cow Challenge", "Deliver 300 buckets of milk to the farmer"},
			new int[] {300},
			AchievementCategory.BAWK_BAWK_BATTLES),

	BAWK_BAWK_BATTLES_DRAGON_KING("Dragon King", 600,
			new String[]{"Bawk Bawk Battles.DragonKing"},
			new String[]{C.cGray + "Egg Smash Challenge", "Smash 300 dragon eggs"},
			new int[] {300},
			AchievementCategory.BAWK_BAWK_BATTLES),

	BAWK_BAWK_BATTLES_PIXEL_NINJA("Pixel Ninja", 200,
			new String[]{"Bawk Bawk Battles.PixelNinja"},
			new String[]{C.cGray + "Falling Blocks Challenge", "Dodge 100 waves of falling blocks"},
			new int[] {100},
			AchievementCategory.BAWK_BAWK_BATTLES),

	BAWK_BAWK_BATTLES_ELITE_ARCHER("Elite Archer", 200,
			new String[]{"Bawk Bawk Battles.EliteArcher"},
			new String[]{C.cGray + "Mini OITQ Challenge", "Kill 100 players"},
			new int[] {100},
			AchievementCategory.BAWK_BAWK_BATTLES),

	BAWK_BAWK_BATTLES_TAG_MASTER("Tag Master", 500,
			new String[]{"Bawk Bawk Battles.TagMaster"},
			new String[]{C.cGray + "Reverse Tag Challenge", "Win 5 entire rounds", "without being untagged"},
			new int[] {5},
			AchievementCategory.BAWK_BAWK_BATTLES),

	BAWK_BAWK_BATTLES_SPEEDY_BUILDERS("Speedy Builders", 500,
		new String[]{"Bawk Bawk Battles.SpeedyBuilders"},
		new String[]{C.cGray + "Build Race Challenge", "Place all blocks in your", "inventory within 15 seconds"},
		new int[] {3},
		AchievementCategory.BAWK_BAWK_BATTLES),

	BAWK_BAWK_BATTLES_BOUNCING_SHADOW("Bouncing Shadow", 500,
			new String[]{"Bawk Bawk Battles.BouncingShadow"},
			new String[]{C.cGray + "Bouncing Block Challenge", "Win 3 entire rounds", "without stepping on red wool"},
			new int[] {3},
			AchievementCategory.BAWK_BAWK_BATTLES),

	//Bomb Lobbers
	BOMB_LOBBERS_WINS("Master Bomber", 1200,
			new String[]{"Bomb Lobbers.Wins"},
			new String[]{"Win 100 games of Bomb Lobbers"},
			new int[] {100},
			AchievementCategory.BOMB_LOBBERS),
			
	BOMB_LOBBERS_PROFESSIONAL_LOBBER("Professional Lobber", 1000,
			new String[]{"Bomb Lobbers.Thrown"},
			new String[]{"Throw 2000 TNT"},
			new int[]{2000},
			AchievementCategory.BOMB_LOBBERS),

	BOMB_LOBBERS_ULTIMATE_KILLER("Ultimate Killer", 800,
			new String[]{"Bomb Lobbers.Killer"},
			new String[]{"Kill 6 players in a single game"},
			new int[]{1},
			AchievementCategory.BOMB_LOBBERS),
			
	BOMB_LOBBERS_EXPLOSION_PROOF("Jelly Skin", 1200, 
			new String[]{"Bomb Lobbers.JellySkin"},
			new String[]{"Win a game without taking any damage."},
			new int[]{1},
			AchievementCategory.BOMB_LOBBERS),
			
	BOMB_LOBBERS_BLAST_PROOF("Blast Proof", 800,
			new String[]{"Bomb Lobbers.BlastProof"},
			new String[]{"Win 20 games using Armorer"},
			new int[]{20},
			AchievementCategory.BOMB_LOBBERS),
			
	BOMB_LOBBERS_SNIPER("Sniper", 1000,
			new String[]{"Bomb Lobbers.Direct Hit"},
			new String[]{"Get 50 direct hits"},
			new int[]{50},
			AchievementCategory.BOMB_LOBBERS),
			
	EVOLUTION_WINS("Expert Evolver", 1200,
			new String[]{"Evolution.Wins"},
			new String[]{"Win 20 games of Evolution"},
			new int[]{20},
			AchievementCategory.EVOLUTION),
			
	EVOLUTION_NO_DEATHS("Perfect Game", 2000, 
			new String[]{"Evolution.NoDeaths"},
			new String[]{"Win a game without dying"},
			new int[]{1},
			AchievementCategory.EVOLUTION),
			
	EVOLUTION_STEALTH("Stealth Mastah", 1000,
			new String[]{"Evolution.Stealth"},
			new String[]{"Win without taking any", "damage while evolving"},
			new int[]{1},
			AchievementCategory.EVOLUTION),
			
	EVOLUTION_RAMPAGE("Rampage", 800, 
			new String[]{"Evolution.Rampage"},
			new String[]{"Get 3 kills within 5 seconds", "of each other"},
			new int[]{1},
			AchievementCategory.EVOLUTION),
			
	EVOLUTION_MELEE("Melee Monster", 1000,
			new String[]{"Evolution.MeleeOnly"},
			new String[]{"Win without using any abilities"},
			new int[]{1},
			AchievementCategory.EVOLUTION),
			
	EVOLUTION_SKILLS("Ability Assassin", 1000,
			new String[]{"Evolution.AbilityOnly"},
			new String[]{"Win without any melee attacks"},
			new int[]{1},
			AchievementCategory.EVOLUTION),
			
	EVOLUTION_EVOLVEKILL("No Evolve 5 U", 800,
			new String[]{"Evolution.EvolveKill"},
			new String[]{"Kill 25 people while they", "Are trying to evolve"},
			new int[]{25},
			AchievementCategory.EVOLUTION),
			
	MONSTER_MAZE_WINS("Maze Master", 1200,
			new String[]{"Monster Maze.Wins"},
			new String[]{"Win 40 games of Monster Maze"},
			new int[]{40},
			AchievementCategory.MONSTER_MAZE),
			
	MONSTER_MAZE_HARD_MODE("Hard Mode", 1000,
			new String[]{"Monster Maze.Hard Mode"},
			new String[]{"Win a game without using", "any kit abilities"},
			new int[]{1},
			AchievementCategory.MONSTER_MAZE),
			
	MONSTER_MAZE_NINJA("Ninja", 1200,
			new String[]{"Monster Maze.Ninja"},
			new String[]{"Win a game without", "touching a monster"},
			new int[]{1},
			AchievementCategory.MONSTER_MAZE),
			
	MONSTER_MAZE_SPEEDSTER("Speedy McGee", 1000,
			new String[]{"Monster Maze.Speed"},
			new String[]{"Be the first to the", "Safe Pad 50 times"},
			new int[]{50},
			AchievementCategory.MONSTER_MAZE),
			
	MONSTER_MAZE_SURVIVAL("Die Already!", 1200,
			new String[]{"Monster Maze.ToughCompetition"},
			new String[]{"Survive past the 10th Safe Pad"},
			new int[]{1},
			AchievementCategory.MONSTER_MAZE),
	
	MONSTER_MAZE_PILOT("Pilot", 800, //TODO
			new String[]{"Monster Maze.Pilot"},
			new String[]{"Get hit by a monster and", "land on the Safe Pad"},
			new int[]{1},
			AchievementCategory.MONSTER_MAZE),

	GLADIATORS_HARDENED_GLADIATOR("Hardened Gladiator", 1000,
			new String[]{"Gladiators.Wins"},
			new String[]{"Win 50 games of Gladiators"},
			new int[]{50},
			AchievementCategory.GLADIATORS),

	GLADIATORS_BATTLE_BRED("Battle Bred", 1500,
			new String[]{"Gladiators.Wins"},
			new String[]{"Win 100 games of Gladiators"},
			new int[]{100},
			AchievementCategory.GLADIATORS),

	GLADIATORS_BRAWLER("Brawler", 1000,
			new String[]{"Gladiators.Brawler"},
			new String[]{"Kill 3 Gladiators", "with your bare hands"},
			new int[]{3},
			AchievementCategory.GLADIATORS),

//	GLADIATORS_UNTOUCHABLE("Untouchable", 1500,
//			new String[]{"Gladiators.Untouchable"},
//			new String[]{"Kill 10 Gladiators", "without taking any damage"},
//			new int[]{10},
//			AchievementCategory.GLADIATORS),

//	GLADIATORS_FLAWLESS("Flawless", 1000,
//			new String[]{"Gladiators.Flawless"},
//			new String[]{"Win a game of gladiators", "without taking any damage"},
//			new int[]{1},
//			AchievementCategory.GLADIATORS),

	GLADIATORS_PRECISION("Precision", 800,
			new String[]{"Gladiators.Precision"},
			new String[]{"Don't miss a single", "arrow in a game of", "Gladiators (Minimum 3)"},
			new int[]{1},
			AchievementCategory.GLADIATORS),

	GLADIATORS_SWIFT_KILL("Swift Kill", 1000,
			new String[]{"Gladiators.SwiftKill"},
			new String[]{"Earn 15 first bloods", "in Gladiators"},
			new int[]{15},
			AchievementCategory.GLADIATORS),
			
	/*TYPE_WARS_SPEED_DEMON("Speed Demon", 1000, 
			new String[]{"Type Wars.Demon"},
			new String[]{"Kill 5 Mobs in 8 seconds", "by typing"},
			new int[]{1},
			AchievementCategory.TYPE_WARS),
					
	TYPE_WARS_PERFECTIONIST("Perfectionist", 1200,
			new String[]{"Type Wars.Perfectionist"},
			new String[]{"Go an entire game", "without mistyping"},
			new int[]{1},
			AchievementCategory.TYPE_WARS),
					
	TYPE_WARS_WAIT_FOR_IT("Wait for it", 1200, 
			new String[]{"Type Wars.Nuke"},
			new String[]{"Kill 30 or more Mobs", "with a Nuke Spell"},
			new int[]{1},
			AchievementCategory.TYPE_WARS),
					
	TYPE_WARS_HOARDER("Hoarder", 1000,
			new String[]{"Type Wars.Hoarder"},
			new String[]{"Summon 50 Mobs in one game"},
			new int[]{1},
			AchievementCategory.TYPE_WARS),
					
	TYPE_WARS_DUMBLEDONT("Dumbledont", 800,
			new String[]{"Type Wars.Dumbledont"},
			new String[]{"Win without using any spells"},
			new int[]{1},
			AchievementCategory.TYPE_WARS),
					
	TYPE_WARS_WINNS("The True Typewriter", 2000,
			new String[]{"Type Wars.Wins"},
			new String[]{"Win 30 Games"},
			new int[]{30},
			AchievementCategory.TYPE_WARS),*/

	SPEED_BUILDERS_SPEED_MASTER("Speed Master", 800,
			new String[]{"Speed Builders.Wins"},
			new String[]{"Win 10 Games of Speed Builder"},
			new int[]{10},
			AchievementCategory.SPEED_BUILDERS),

	SPEED_BUILDERS_DEPENDABLE("Dependable", 1200,
			new String[]{"Speed Builders.PerfectBuild"},
			new String[]{"Complete 50 Perfect Builds"},
			new int[]{50},
			AchievementCategory.SPEED_BUILDERS),

	SPEED_BUILDERS_FIRST_BUILD("First Build!", 1800,
			new String[]{"Speed Builders.PerfectFirst"},
			new String[]{"Be the first person to complete a build in the game 10 times"},
			new int[]{10},
			AchievementCategory.SPEED_BUILDERS),

	SPEED_BUILDERS_PERFECTIONIST("Perfectionist", 2200,
			new String[]{"Speed Builders.PerfectWins"},
			new String[]{"Win a game of Speed Builder with a perfect build every round"},
			new int[]{1},
			AchievementCategory.SPEED_BUILDERS),

	SPEED_BUILDERS_SPEEDIEST("Speediest Builderizer", 2000,
			new String[]{"Speed Builders.SpeediestBuilderizer"},
			new String[]{"Perfect a build in less than 10 seconds"},
			new int[]{1},
			AchievementCategory.SPEED_BUILDERS),

	// OITQP
	/*QUIVER_PAYLOAD_BLOSSOM("Flowering Blossom", 2000,
			new String[]{"One in the Quiver Payload.Blossom"},
			new String[]{"Get 4 kills with a single Pyromancer ultimate."},
			new int[]{1},
			AchievementCategory.ONE_IN_THE_QUIVER_PAYLOAD),

	QUIVER_PAYLOAD_STEADY_HANDS("Steady Hands", 2000,
			new String[]{"One in the Quiver Payload.SteadyHands"},
			new String[]{"Get 10 triple kills."},
			new int[]{10},
			AchievementCategory.ONE_IN_THE_QUIVER_PAYLOAD),

	QUIVER_PAYLOAD_ASSASSIN("Way of the Assassin", 2000,
			new String[]{"One in the Quiver Payload.Assassin"},
			new String[]{"Get 5 kills with a single use of Ancient Blade."},
			new int[]{1},
			AchievementCategory.ONE_IN_THE_QUIVER_PAYLOAD),

	QUIVER_PAYLOAD_UNSTOPPABLE("Unstoppable", 2000,
			new String[]{"One in the Quiver Payload.Unstoppable"},
			new String[]{"Get shot by 100 arrows while using the Berserker ultimate."},
			new int[]{100},
			AchievementCategory.ONE_IN_THE_QUIVER_PAYLOAD),

	QUIVER_PAYLOAD_BOW("What bow?", 2000,
			new String[]{"One in the Quiver Payload.Bow"},
			new String[]{"Get 10 kills in a single game without firing an arrow."},
			new int[]{1},
			AchievementCategory.ONE_IN_THE_QUIVER_PAYLOAD),*/

	// Skyfall
	SKYFALL_GAMES_WINS("Skyfaller", 600,
			new String[]{"Skyfall.Wins"},
			new String[]{"Win 30 games of Skyfall"},
			new int[]{30},
			AchievementCategory.SKYFALL),

	SKYFALL_GAMES_LIGHT_WEIGHT("Light Weight", 1000,
			new String[]{"Skyfall.NoArmor"},
			new String[]{"Win a game without wearing any armor"},
			new int[]{1},
			AchievementCategory.SKYFALL),

	SKYFALL_GAMES_BLOODLUST("Bloodlust", 1200,
			new String[]{"Skyfall.Bloodlust"},
			new String[]{"Kill 3 other players in the first minute"},
			new int[]{1},
			AchievementCategory.SKYFALL),

	SKYFALL_KILLS_IN_AIR("Aeronaught", 1200,
			new String[]{"Skyfall.Aeronaught"},
			new String[]{"Get 10 kills while flying"},
			new int[]{10},
			AchievementCategory.SKYFALL),

	SKYFALL_RINGS("I love Booster Rings <3", 1200,
			new String[]{"Skyfall.Rings"},
			new String[]{"Fly through 1000 Booster Rings"},
			new int[]{1000},
			AchievementCategory.SKYFALL),

	SKYFALL_GAMES_LOOT("Loot Hoarder", 800,
			new String[]{"Skyfall.SupplyDropsOpened"},
			new String[]{"Be the first to open 20 Supply Drops"},
			new int[]{20},
			AchievementCategory.SKYFALL),

	GEM_HUNTERS_KILLS("Gem Killer", 5000,
			new String[]{"Gem Hunters.Kills"},
			new String[]{"+1 for each kill"},
			new int[]{10,25,50,100,1000},
			AchievementCategory.GEM_HUNTERS),

	GEM_HUNTERS_GEMS_EARNED("Gem Millionaire", 5000,
			new String[]{"Gem Hunters.GemsEarned"},
			new String[]{"+1 for each Gem cashed out"},
			new int[]{1000,2500,5000,10000,100000},
			AchievementCategory.GEM_HUNTERS),

	GEM_HUNTERS_QUESTS("Quest Complete", 5000,
			new String[]{"Gem Hunters.QuestsCompleted"},
			new String[]{"+1 for each quest completed"},
			new int[]{10,25,50,100,1000},
			AchievementCategory.GEM_HUNTERS),

	GEM_HUNTERS_CHESTS_OPENED("Loot Get!", 5000,
			new String[]{"Gem Hunters.ChestsOpened"},
			new String[]{"+1 for each chest opened"},
			new int[]{50,100,200,400,1000},
			AchievementCategory.GEM_HUNTERS),

	MOBA_GOLD_EARNED("Gold Farmer", 0,
			new String[]{"Heroes of GWEN.GoldEarned"},
			new String[]{"Earn Gold"},
			new int[][] {new int[] {0,0,1000}, new int[] {0,0,5000}, new int[]{0,0,10000}, new int[]{0,0,25000}, new int[]{0,0,50000}},
			new int[]{100000,500000,1000000,2500000,5000000},
			"I",
			new String[] {"II","III","IV","V","X"},
			AchievementCategory.MOBA),

	MOBA_KILLS("Champion Slayer", 0,
			new String[]{"Heroes of GWEN.Kills"},
			new String[]{"Kill players"},
			new int[][] {new int[] {0,0,1000}, new int[] {0,0,5000}, new int[]{0,0,10000}, new int[]{0,0,25000}, new int[]{0,0,50000}},
			new int[]{100,250,500,1000,5000},
			"I",
			new String[] {"II","III","IV","V","X"},
			AchievementCategory.MOBA),

	MOBA_WINS_ASSASSIN("Assassin Victor", 0,
			new String[]{"Heroes of GWEN.Assassin.Wins"},
			new String[]{"Win Games as an Assassin"},
			new int[][] {new int[] {0,0,1000}, new int[] {0,0,5000}, new int[]{0,0,10000}, new int[]{0,0,25000}, new int[]{0,0,50000}},
			new int[]{10,50,100,250,500},
			"I",
			new String[] {"II","III","IV","V","X"},
			AchievementCategory.MOBA),

	MOBA_WINS_HUNTER("Hunter Victor", 0,
			new String[]{"Heroes of GWEN.Hunter.Wins"},
			new String[]{"Win Games as a Hunter"},
			new int[][] {new int[] {0,0,1000}, new int[] {0,0,5000}, new int[]{0,0,10000}, new int[]{0,0,25000}, new int[]{0,0,50000}},
			new int[]{10,50,100,250,500},
			"I",
			new String[] {"II","III","IV","V","X"},
			AchievementCategory.MOBA),

	MOBA_WINS_MAGE("Mage Victor", 0,
			new String[]{"Heroes of GWEN.Mage.Wins"},
			new String[]{"Win Games as a Mage"},
			new int[][] {new int[] {0,0,1000}, new int[] {0,0,5000}, new int[]{0,0,10000}, new int[]{0,0,25000}, new int[]{0,0,50000}},
			new int[]{10,50,100,250,500},
			"I",
			new String[] {"II","III","IV","V","X"},
			AchievementCategory.MOBA),

	MOBA_WINS_WARRIOR("Warrior Victor", 0,
			new String[]{"Heroes of GWEN.Warrior.Wins"},
			new String[]{"Win Games as a Warrior"},
			new int[][] {new int[] {0,0,1000}, new int[] {0,0,5000}, new int[]{0,0,10000}, new int[]{0,0,25000}, new int[]{0,0,50000}},
			new int[]{10,50,100,250,500},
			"I",
			new String[] {"II","III","IV","V","X"},
			AchievementCategory.MOBA),

	CAKE_WARS_WIN("Cake Dinners", 15000,
			new String[]{"Cake Wars Standard.Wins", "Cake Wars Duos.Wins"},
			new String[]{"Win 100 games of Cake Wars"},
			new int[]{100},
			AchievementCategory.CAKE_WARS),

	CAKE_WARS_KILLS("Cake Slayer", 15000,
			new String[]{"Cake Wars Standard.Kills", "Cake Wars Duos.Kills"},
			new String[]{"Kill 1000 players in Cake Wars"},
			new int[]{1000},
			AchievementCategory.CAKE_WARS),

	CAKE_WARS_BITES("Big Appetite", 15000,
			new String[]{"Cake Wars Standard.Bites", "Cake Wars Duos.Bites"},
			new String[]{"Take 500 bites of cake in Cake Wars"},
			new int[]{500},
			AchievementCategory.CAKE_WARS),

	CAKE_WARS_EAT_WHOLE_CAKE("Greedy", 2000,
			new String[]{"Cake Wars Standard.EatWholeCake", "Cake Wars Duos.EatWholeCake"},
			new String[]{"Eat a whole cake"},
			new int[]{1},
			AchievementCategory.CAKE_WARS),

	CAKE_WARS_SURVIVE_10("This game has cakes?", 4000,
			new String[]{"Cake Wars Standard.Survive10", "Cake Wars Duos.Survive10"},
			new String[]{"Survive 10 minutes without", "a cake"},
			new int[]{1},
			AchievementCategory.CAKE_WARS),

	CAKE_WARS_FIRST_BLOOD("Cold Baked Killer", 2000,
			new String[]{"Cake Wars Standard.FirstBlood", "Cake Wars Duos.FirstBlood"},
			new String[]{"Get 10 First Bloods"},
			new int[]{10},
			AchievementCategory.CAKE_WARS),

	CAKE_WARS_EAT_1("Starving", 2000,
			new String[]{"Cake Wars Standard.Eat1", "Cake Wars Duos.Eat1"},
			new String[]{"Eat a cake within the first", "minute of the game"},
			new int[]{1},
			AchievementCategory.CAKE_WARS),

	CAKE_WARS_NO_DEATHS("You call that a challenge", 4000,
			new String[]{"Cake Wars Standard.NoDeaths", "Cake Wars Duos.NoDeaths"},
			new String[]{"Win a game without dying"},
			new int[]{1},
			AchievementCategory.CAKE_WARS),

	CAKE_WARS_BUY_ALL("Team Player", 2000,
			new String[]{"Cake Wars Standard.BuyAll", "Cake Wars Duos.BuyAll"},
			new String[]{"Purchase all Team Upgrades", "within one game"},
			new int[]{1},
			AchievementCategory.CAKE_WARS),

	CAKE_WARS_WIN_WITHOUT_KILL("Love not War", 4000,
			new String[]{"Cake Wars Standard.NoKills", "Cake Wars Duos.NoKills"},
			new String[]{"Win a game without killing", "a player"},
			new int[]{1},
			AchievementCategory.CAKE_WARS),

	CAKE_WARS_WIN_WITH_1("Last Crumb", 2000,
			new String[]{"Cake Wars Standard.WinWithOneBite", "Cake Wars Duos.WinWithOneBite"},
			new String[]{"Win a game with only one", "bite of your cake left"},
			new int[]{1},
			AchievementCategory.CAKE_WARS),

	CAKE_WARS_WIN_IN_10("Gotta go fast", 2000,
			new String[]{"Cake Wars Standard.WinIn10", "Cake Wars Duos.WinIn10"},
			new String[]{"Win a game within 10", "minutes"},
			new int[]{1},
			AchievementCategory.CAKE_WARS),

	CAKE_WARS_FINAL_BITE("Last Laugh", 2000,
			new String[]{"Cake Wars Standard.FinalBite", "Cake Wars Duos.FinalBite"},
			new String[]{"Get the final bite on every", "cake within a game"},
			new int[]{1},
			AchievementCategory.CAKE_WARS),

	CAKE_WARS_OWN_ALL_BEACONS("I <3 Capture Points", 4000,
			new String[]{"Cake Wars Standard.OwnAllBeacons", "Cake Wars Duos.OwnAllBeacons"},
			new String[]{"Own all beacons for your team", "in one game"},
			new int[]{1},
			AchievementCategory.CAKE_WARS),

	CAKE_WARS_GET_GOOD("Get Good", 15000,
			new String[]{"Cake Wars Standard.GetGood", "Cake Wars Duos.GetGood"},
			new String[]{"Kill a player while falling", "from a great height.", "Then save yourself from dying"},
			new int[]{1},
			AchievementCategory.CAKE_WARS),

	CAKE_WARS_FLOOR_IS_LAVA("The Floor Is Lava", 15000,
			new String[]{"Cake Wars Standard.FloorIsLava", "Cake Wars Duos.FloorIsLava"},
			new String[]{"Win a game of Cake Wars while", "after the first 30 seconds of the game,", "only stand on player placed blocks", "", "Includes Deploy Platforms and", "near Shops"},
			new int[]{1},
			AchievementCategory.CAKE_WARS),

	// Nano Games
	NANO_WINNER("Nano Winner", 0,
			new String[]{"Nano Games.Wins"},
			new String[]{"Win Nano Games"},
			new int[][] {new int[] {0,0,1000}, new int[] {0,0,5000}, new int[]{0,0,10000}, new int[]{0,0,25000}, new int[]{0,0,50000}},
			new int[]{2,10,50,100,500},
			"I",
			new String[] {"II","III","IV","V","X"},
			AchievementCategory.NANO_GAMES),

	NANO_PLAY_IN_A_ROW("Player 1", 6000,
			new String[]{"Nano Games.PlayInARow"},
			new String[]{"Play 100 Games in a row"},
			new int[]{1},
			AchievementCategory.NANO_GAMES),

	NANO_SECOND_PLACE("Better than last!", 500,
			new String[]{"Nano Games.SecondPlace"},
			new String[]{"Get second place"},
			new int[]{1},
			AchievementCategory.NANO_GAMES),

	NANO_THIRD_PLACE("Still better than last!", 500,
			new String[]{"Nano Games.ThirdPlace"},
			new String[]{"Get third place"},
			new int[]{1},
			AchievementCategory.NANO_GAMES),

	NANO_PLAY("Nani Games?", 500,
			new String[]{"Nano Games.Wins", "Nano Games.Losses"},
			new String[]{"Play a Nano Game"},
			new int[]{1},
			AchievementCategory.NANO_GAMES),

	NANO_SHARE_FIRST("Best Friends", 500,
			new String[]{"Nano Games.ShareFirst"},
			new String[]{"Share first place with at least", "one other player"},
			new int[]{1},
			AchievementCategory.NANO_GAMES),

	NANO_COPY_CAT_LEVEL("Copy Kitty", 4000,
			new String[]{"Nano Games.CopyCatLevel10"},
			new String[]{"Reach Level 10 in Copy Cat"},
			new int[]{1},
			AchievementCategory.NANO_GAMES),

	NANO_QUICK_GAMES("QUICK!!!", 0,
			new String[]{"Nano Games.QuickWins"},
			new String[]{"Complete Quick Challenges"},
			new int[][] {new int[] {0,0,1000}, new int[] {0,0,5000}, new int[]{0,0,10000}, new int[]{0,0,25000}, new int[]{0,0,50000}},
			new int[]{10,25,50,100,500},
			"I",
			new String[] {"II","III","IV","V","X"},
			AchievementCategory.NANO_GAMES),

	NANO_SLIME_TAIL("Not dead yet", 3000,
			new String[]{"Nano Games.SlimeCyclesTail"},
			new String[]{"Get a trail length of 100 blocks", "in Slime Cycles"},
			new int[]{1},
			AchievementCategory.NANO_GAMES),

	;

	private final String _name;
	private final String[] _desc;
	private final String[] _stats;
	private final int[][] _levelUpRewards;
	private final int[] _levels;
	private final String _defaultLevelName;
	private final String[] _levelNames;
	private final int _gems;
	private final AchievementCategory _category;

	Achievement(String name, int gems, String[] stats, String[] desc, int[][] levelUpRewards, int[] levels, String defaultLevelName, String[] levelNames, AchievementCategory category)
	{
		_name = name;
		_gems = gems;
		_desc = desc;
		_stats = stats;
		_levelUpRewards = levelUpRewards;
		_levels = levels;
		_defaultLevelName = defaultLevelName;
		_levelNames = levelNames;
		_category = category;
	}
	
	Achievement(String name, int gems, String[] stats, String[] desc, int[] levels, AchievementCategory category)
	{
		_name = name;
		_gems = gems;
		_desc = desc;
		_stats = stats;
		_levelUpRewards = new int[][] {};
		_levels = levels;
		_levelNames = new String[] {};
		_defaultLevelName = "";
		_category = category;
	}

	private static int[] getExperienceLevels()
	{
		int[] levels = new int[LevelingManager.getMaxLevel()];
		
		int expReq = 0;
		
		for (int i=0 ; i<10 ; i++)
		{
			expReq += 500;
			levels[i] = expReq;
		}
		
		for (int i=10 ; i<20 ; i++)
		{
			expReq += 1000;
			levels[i] = expReq;
		}
			
		for (int i=20 ; i<40 ; i++)
		{
			expReq += 2000;
			levels[i] = expReq;
		}
		
		for (int i=40 ; i<60 ; i++)
		{
			expReq += 3000;
			levels[i] = expReq;
		}
		
		for (int i=60 ; i<80 ; i++)
		{
			expReq += 4000;
			levels[i] = expReq;
		}
		
		for (int i=80 ; i<levels.length ; i++)
		{
			expReq += 5000;
			levels[i] = expReq;
		}

		return levels;
	}

	public static String getExperienceString(int level)
	{
		if (level < 0)
			return C.cPurple + level;

		if (level < 20)
			return C.cGray + level;

		if (level < 40)
			return C.cBlue + level;
		
		if (level < 60)
			return C.cDGreen + level;
		
		if (level < 80)
			return C.cGold + level;
		
		return C.cRed + level;
	}

	public String getName()
	{
		return _name;
	}

	public String[] getDesc()
	{
		return _desc;
	}

	public String[] getStats()
	{
		return _stats;
	}
	
	public int[][] getLevelUpRewards()
	{
		return _levelUpRewards;
	}

	public int[] getLevels()
	{
		return _levels;
	}
	
	public String getDefaultLevelName()
	{
		return _defaultLevelName;
	}
	
	public String[] getLevelNames()
	{
		return _levelNames;
	}

	public int getMaxLevel()
	{
		return _levels.length;
	}

	public boolean isOngoing()
	{
		return _levels[0] > 1;
	}

	public boolean isSingleLevel()
	{
		return _levels.length == 1;
	}
	
	public boolean hasLevelNames()
	{
		return _levelNames.length > 0;
	}

	public AchievementCategory getCategory()
	{
		return _category;
	}

	public AchievementData getLevelData(long exp)
	{
		for (int i = 0; i < _levels.length; i++)
		{
			int req = _levels[i];

			//Has Experience, Level Up!
			if (exp >= req)
			{
				exp -= req;
				continue;
			}

			return new AchievementData(i, exp, req);
		}

		return new AchievementData(getMaxLevel(), -1, -1);
	}

	public int getGemReward()
	{
		return _gems;
	}

	private static final Map<AchievementCategory, List<Achievement>> BY_CATEGORY;

	static
	{
		Map<AchievementCategory, List<Achievement>> byCategory = new HashMap<>();
		for (Achievement achievement : values())
		{
			byCategory.computeIfAbsent(achievement.getCategory(), key -> new ArrayList<>()).add(achievement);
		}
		Map<AchievementCategory, List<Achievement>> immutableByCategory = new HashMap<>();
		byCategory.forEach((key, value) -> immutableByCategory.put(key, Collections.unmodifiableList(value)));

		BY_CATEGORY = Collections.unmodifiableMap(immutableByCategory);
	}

	public static List<Achievement> getByCategory(AchievementCategory category)
	{
		return BY_CATEGORY.get(category);
	}
}
