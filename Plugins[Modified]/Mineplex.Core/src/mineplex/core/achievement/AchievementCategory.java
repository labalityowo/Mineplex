package mineplex.core.achievement;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import mineplex.core.account.CoreClientManager;
import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilTime;
import mineplex.core.game.GameDisplay;
import mineplex.core.stats.PlayerStats;
import mineplex.core.stats.StatsManager;

public enum AchievementCategory
{
	GLOBAL("Global", null,
			new StatDisplay[]
					{
							StatDisplay.GEMS_EARNED,
							new StatDisplay("Exp Earned", "ExpEarned"),
							null,
							new StatDisplay("Games Played", "GamesPlayed"),
							StatDisplay.TIME_IN_GAME,
							null,
							new StatDisplay("Daily Rewards", "DailyReward"),
							new StatDisplay("Times Voted", "DailyVote"),
							null,
							new StatDisplay("Chests Opened", "Treasure.Old", "Treasure.Ancient", "Treasure.Mythical")
					}, Material.EMERALD, 0, GameCategory.STAND_ALONE, "None", false, -1),

	HOLIDAY("Holiday Achievements", null,
			new StatDisplay[]{},
			Material.CAKE, 0, GameCategory.STAND_ALONE, "None", false, -1),

	BRIDGES("The Bridges", null,
			new StatDisplay[]{StatDisplay.WINS, StatDisplay.GAMES_PLAYED, StatDisplay.KILLS, StatDisplay.DEATHS, StatDisplay.GEMS_EARNED},
			Material.IRON_PICKAXE, 0, GameCategory.GAME, "Destructor Kit", false, GameDisplay.Bridge.getGameId()),

	SURVIVAL_GAMES("Survival Games", null,
			new StatDisplay[]{StatDisplay.WINS, StatDisplay.GAMES_PLAYED, StatDisplay.KILLS, StatDisplay.DEATHS, StatDisplay.GEMS_EARNED,
					null, null, new StatDisplay(C.Bold + "Teams Stats", true), null,
					StatDisplay.fromGame("Wins", GameDisplay.SurvivalGamesTeams, "Wins"), StatDisplay.fromGame("Games Played", GameDisplay.SurvivalGamesTeams, "Wins", "Losses"),
					StatDisplay.fromGame("Kills", GameDisplay.SurvivalGamesTeams, "Kills"), StatDisplay.fromGame("Deaths", GameDisplay.SurvivalGamesTeams, "Deaths"),
					StatDisplay.fromGame("Gems Earned", GameDisplay.SurvivalGamesTeams, "GemsEarned")},
			Material.DIAMOND_SWORD, 0, GameCategory.GAME, "Horseman Kit", false, GameDisplay.SurvivalGames.getGameId(), GameDisplay.SurvivalGamesTeams.getGameId()),

	SKYWARS("Skywars", null,
			new StatDisplay[]{StatDisplay.WINS, StatDisplay.GAMES_PLAYED, StatDisplay.KILLS, StatDisplay.DEATHS, StatDisplay.GEMS_EARNED,
					null, null, new StatDisplay(C.Bold + "Team Stats", true), null,
					StatDisplay.fromGame("Wins", GameDisplay.SkywarsTeams, "Wins"), StatDisplay.fromGame("Games Played", GameDisplay.SkywarsTeams, "Wins", "Losses"),
					StatDisplay.fromGame("Kills", GameDisplay.SkywarsTeams, "Kills"), StatDisplay.fromGame("Deaths", GameDisplay.SkywarsTeams, "Deaths"),
					StatDisplay.fromGame("Gems Earned", GameDisplay.SkywarsTeams, "GemsEarned")},
			Material.FEATHER, 0, GameCategory.GAME, "Earth Kit", false, GameDisplay.Skywars.getGameId(), GameDisplay.SkywarsTeams.getGameId()),

	UHC("Ultra Hardcore", null,
			new StatDisplay[]{
					new StatDisplay(C.Bold + "Solo Stats", true),
					null,
					StatDisplay.fromGame("Wins", GameDisplay.UHCSolo, "Wins"), StatDisplay.fromGame("Games Played", GameDisplay.UHCSolo, "Wins", "Losses"),
					StatDisplay.fromGame("Kills", GameDisplay.UHCSolo, "Kills"), StatDisplay.fromGame("Deaths", GameDisplay.UHCSolo, "Deaths"),
					StatDisplay.fromGame("Gems Earned", GameDisplay.UHCSolo, "GemsEarned"),
					null,
					null,
					new StatDisplay(C.Bold + "Teams Stats", true),
					null,
					StatDisplay.fromGame("Wins", GameDisplay.UHC, "Wins"), StatDisplay.fromGame("Games Played", GameDisplay.UHC, "Wins", "Losses"),
					StatDisplay.fromGame("Kills", GameDisplay.UHC, "Kills"), StatDisplay.fromGame("Deaths", GameDisplay.UHC, "Deaths"),
					StatDisplay.fromGame("Gems Earned", GameDisplay.UHC, "GemsEarned")},
			Material.GOLDEN_APPLE, 0, GameCategory.UHC, "None", false, GameDisplay.UHCSolo.getGameId(), GameDisplay.UHC.getGameId()),

	UHC_SPEED("Ultra Hardcore Speed", null,
			new StatDisplay[]{
					new StatDisplay(C.Bold + "Solo Stats", true),
					null,
					StatDisplay.fromGame("Wins", GameDisplay.UHCSoloSpeed, "Wins"), StatDisplay.fromGame("Games Played", GameDisplay.UHCSoloSpeed, "Wins", "Losses"),
					StatDisplay.fromGame("Kills", GameDisplay.UHCSoloSpeed, "Kills"), StatDisplay.fromGame("Deaths", GameDisplay.UHCSoloSpeed, "Deaths"),
					StatDisplay.fromGame("Gems Earned", GameDisplay.UHCSoloSpeed, "GemsEarned"),
					null,
					null,
					new StatDisplay(C.Bold + "Teams Stats", true),
					null,
					StatDisplay.fromGame("Wins", GameDisplay.UHCTeamsSpeed, "Wins"), StatDisplay.fromGame("Games Played", GameDisplay.UHCTeamsSpeed, "Wins", "Losses"),
					StatDisplay.fromGame("Kills", GameDisplay.UHCTeamsSpeed, "Kills"), StatDisplay.fromGame("Deaths", GameDisplay.UHCTeamsSpeed, "Deaths"),
					StatDisplay.fromGame("Gems Earned", GameDisplay.UHCTeamsSpeed, "GemsEarned")},
			Material.GOLDEN_APPLE, 0, GameCategory.UHC, "None", false, GameDisplay.UHCSoloSpeed.getGameId(), GameDisplay.UHCTeamsSpeed.getGameId()),

	/*MC_LEAGUE("MC League", null,
			new StatDisplay[] { StatDisplay.WINS, StatDisplay.GAMES_PLAYED, StatDisplay.KILLS, StatDisplay.DEATHS, StatDisplay.GEMS_EARNED },
			Material.IRON_CHESTPLATE, 0, GameCategory.SURVIVAL, "None", true, GameDisplay.Minecraft_League.getGameId()),*/

	WIZARDS("Wizards", null,
			new StatDisplay[]{StatDisplay.WINS, StatDisplay.GAMES_PLAYED, StatDisplay.KILLS, StatDisplay.DEATHS, StatDisplay.GEMS_EARNED},
			Material.BLAZE_ROD, 0, GameCategory.GAME, "Witch Doctor Kit", false, GameDisplay.Wizards.getGameId()),

	CASTLE_ASSAULT("Castle Assault", new String[]{"Castle Assault", "Castle Assault TDM"},
			new StatDisplay[]{StatDisplay.WINS, StatDisplay.GAMES_PLAYED, StatDisplay.KILLS, StatDisplay.DEATHS, StatDisplay.CROWNS_EARNED},
			Material.DIAMOND_CHESTPLATE, 0, GameCategory.GAME, null, false, GameDisplay.CastleAssault.getGameId(), GameDisplay.CastleAssaultTDM.getGameId()),

	CASTLE_SIEGE("Castle Siege", null,
			new StatDisplay[]
					{
							StatDisplay.WINS,
							StatDisplay.GAMES_PLAYED,
							null,
							new StatDisplay(C.Bold + "Total", true),
							StatDisplay.KILLS,
							new StatDisplay("Assists", "Defender Assists", "Undead Assists"),
							StatDisplay.DEATHS,
							null,
							new StatDisplay(C.Bold + "Defenders", true),
							null,
							StatDisplay.fromGame("Kills", GameDisplay.CastleSiege, "Defenders Kills"),
							StatDisplay.fromGame("Assists", GameDisplay.CastleSiege, "Defenders Assists"),
							StatDisplay.fromGame("Deaths", GameDisplay.CastleSiege, "Defenders Deaths"),
							null,
							new StatDisplay(C.Bold + "Undead", true),
							null,
							StatDisplay.fromGame("Kills", GameDisplay.CastleSiege, "Undead Kills"),
							StatDisplay.fromGame("Assists", GameDisplay.CastleSiege, "Undead Assists"),
							StatDisplay.fromGame("Deaths", GameDisplay.CastleSiege, "Undead Deaths"),
							null,
							StatDisplay.GEMS_EARNED,
					},
			Material.DIAMOND_CHESTPLATE, 0, GameCategory.GAME, "Undead Summoner & Castle Paladin Kit", false, GameDisplay.CastleSiege.getGameId()),

	BAWK_BAWK_BATTLES("Bawk Bawk Battles", null,
			new StatDisplay[]{StatDisplay.WINS, StatDisplay.GAMES_PLAYED, StatDisplay.GEMS_EARNED},
			Material.EGG, 0, GameCategory.GAME, null, false, GameDisplay.BawkBawkBattles.getGameId()),

	BLOCK_HUNT("Block Hunt", null,
			new StatDisplay[]
					{
							StatDisplay.fromGame("Hider Wins", GameDisplay.HideSeek, "Wins"),
							StatDisplay.fromGame("Hunter Wins", GameDisplay.HideSeek, "HunterWins"),
							StatDisplay.fromGame("Games Played", GameDisplay.HideSeek, "Wins", "HunterWins", "Losses"),
							StatDisplay.KILLS,
							StatDisplay.DEATHS,
							StatDisplay.GEMS_EARNED
					},
			Material.GRASS, 0, GameCategory.GAME, "Infestor Kit", false, GameDisplay.HideSeek.getGameId()),

	SMASH_MOBS("Super Smash Mobs", null,
			new StatDisplay[]{StatDisplay.WINS, StatDisplay.GAMES_PLAYED, StatDisplay.KILLS, StatDisplay.DEATHS, StatDisplay.GEMS_EARNED,
					null, null, new StatDisplay(C.Bold + "Team Stats", true), null,
					StatDisplay.fromGame("Wins", GameDisplay.SmashTeams, "Wins"), StatDisplay.fromGame("Games Played", GameDisplay.SmashTeams, "Wins", "Losses"),
					StatDisplay.fromGame("Kills", GameDisplay.SmashTeams, "Kills"), StatDisplay.fromGame("Deaths", GameDisplay.SmashTeams, "Deaths"),
					StatDisplay.fromGame("Gems Earned", GameDisplay.SmashTeams, "GemsEarned")},
			Material.SKULL_ITEM, 4, GameCategory.GAME, "Sheep Kit", false, GameDisplay.Smash.getGameId(), GameDisplay.SmashTeams.getGameId()),

	MINE_STRIKE("MineStrike", null,
			new StatDisplay[]{StatDisplay.WINS, StatDisplay.GAMES_PLAYED, StatDisplay.KILLS, StatDisplay.DEATHS, StatDisplay.GEMS_EARNED},
			Material.TNT, 0, GameCategory.GAME, "None", false, GameDisplay.MineStrike.getGameId()),

	DRAW_MY_THING("Draw My Thing", null,
			new StatDisplay[]{StatDisplay.WINS, StatDisplay.GAMES_PLAYED, StatDisplay.GEMS_EARNED},
			Material.BOOK_AND_QUILL, 0, GameCategory.GAME, "None", false, GameDisplay.Draw.getGameId()),

	CHAMPIONS("Champions", new String[]{"Champions Domination", "Champions TDM", "Champions CTF"},
			new StatDisplay[]{StatDisplay.WINS, StatDisplay.GAMES_PLAYED, StatDisplay.KILLS, StatDisplay.DEATHS, StatDisplay.GEMS_EARNED, new StatDisplay("Flags Captured", "Captures")},
			Material.BEACON, 0, GameCategory.GAME, "Extra Class Skills", false, GameDisplay.ChampionsCTF.getGameId(), GameDisplay.ChampionsDominate.getGameId(), GameDisplay.ChampionsTDM.getGameId()),

	MASTER_BUILDERS("Master Builders", null,
			new StatDisplay[]{StatDisplay.WINS, StatDisplay.GAMES_PLAYED, StatDisplay.GEMS_EARNED},
			Material.WOOD, 0, GameCategory.GAME, "None", false, GameDisplay.Build.getGameId()),

	//Arcade
	DRAGONS("Dragons", null,
			new StatDisplay[]{StatDisplay.WINS, StatDisplay.GAMES_PLAYED, StatDisplay.GEMS_EARNED},
			Material.ENDER_STONE, 0, GameCategory.ARCADE, null, false, GameDisplay.Dragons.getGameId()),

	DRAGON_ESCAPE("Dragon Escape", null,
			new StatDisplay[]{StatDisplay.WINS, StatDisplay.GAMES_PLAYED, StatDisplay.GEMS_EARNED},
			Material.DRAGON_EGG, 0, GameCategory.ARCADE, "Digger Kit", false, GameDisplay.DragonEscape.getGameId()),

	SHEEP_QUEST("Sheep Quest", null,
			new StatDisplay[]{StatDisplay.WINS, StatDisplay.GAMES_PLAYED, StatDisplay.KILLS, StatDisplay.DEATHS, StatDisplay.GEMS_EARNED},
			Material.WOOL, 0, GameCategory.ARCADE, null, false, GameDisplay.Sheep.getGameId()),

	SNEAKY_ASSASSINS("Sneaky Assassins", null,
			new StatDisplay[]{StatDisplay.WINS, StatDisplay.GAMES_PLAYED, StatDisplay.KILLS, StatDisplay.DEATHS, StatDisplay.GEMS_EARNED},
			Material.INK_SACK, 0, GameCategory.ARCADE, "Briber Kit", false, GameDisplay.SneakyAssassins.getGameId()),

	ONE_IN_THE_QUIVER("One in the Quiver", null,
			new StatDisplay[]{StatDisplay.WINS, StatDisplay.GAMES_PLAYED, StatDisplay.KILLS, StatDisplay.DEATHS, StatDisplay.GEMS_EARNED},
			Material.BOW, 0, GameCategory.ARCADE, "Ninja Kit", false, GameDisplay.Quiver.getGameId()),

	SUPER_PAINTBALL("Super Paintball", null,
			new StatDisplay[]{StatDisplay.WINS, StatDisplay.GAMES_PLAYED, StatDisplay.KILLS, StatDisplay.DEATHS, StatDisplay.GEMS_EARNED},
			Material.ENDER_PEARL, 0, GameCategory.ARCADE, "Sniper Kit", false, GameDisplay.Paintball.getGameId()),

	TURF_WARS("Turf Wars", null,
			new StatDisplay[]{StatDisplay.WINS, StatDisplay.GAMES_PLAYED, StatDisplay.KILLS, StatDisplay.DEATHS, StatDisplay.GEMS_EARNED},
			Material.HARD_CLAY, 14, GameCategory.ARCADE, null, false, GameDisplay.TurfWars.getGameId()),

	RUNNER("Runner", null,
			new StatDisplay[]{StatDisplay.WINS, StatDisplay.GAMES_PLAYED, StatDisplay.DEATHS, StatDisplay.GEMS_EARNED},
			Material.LEATHER_BOOTS, 0, GameCategory.ARCADE, null, false, GameDisplay.Runner.getGameId()),

	SPLEEF("Super Spleef", null,
			new StatDisplay[]{StatDisplay.WINS, StatDisplay.GAMES_PLAYED, StatDisplay.DEATHS, StatDisplay.GEMS_EARNED},
			Material.IRON_SPADE, 0, GameCategory.ARCADE, null, false, GameDisplay.Spleef.getGameId()),

	DEATH_TAG("Death Tag", null,
			new StatDisplay[]{StatDisplay.WINS, StatDisplay.GAMES_PLAYED, StatDisplay.KILLS, StatDisplay.DEATHS, StatDisplay.GEMS_EARNED},
			Material.SKULL_ITEM, 0, GameCategory.ARCADE, null, false, GameDisplay.DeathTag.getGameId()),

	SNAKE("Snake", null,
			new StatDisplay[]{StatDisplay.WINS, StatDisplay.GAMES_PLAYED, StatDisplay.KILLS, StatDisplay.DEATHS, StatDisplay.GEMS_EARNED},
			Material.WOOL, 4, GameCategory.ARCADE, "Reversal Snake Kit", false, GameDisplay.Snake.getGameId()),

	BACON_BRAWL(
			"Bacon Brawl",
			null,
			new StatDisplay[]
					{
							StatDisplay.WINS,
							StatDisplay.GAMES_PLAYED,
							StatDisplay.KILLS,
							StatDisplay.DEATHS,
							StatDisplay.GEMS_EARNED
					}, Material.PORK, 0, GameCategory.ARCADE, "Chris P Bacon", false, GameDisplay.BaconBrawl.getGameId()),

	MICRO_BATTLE("Micro Battle", null,
			new StatDisplay[]{StatDisplay.WINS, StatDisplay.GAMES_PLAYED, StatDisplay.KILLS, StatDisplay.DEATHS, StatDisplay.GEMS_EARNED},
			Material.LAVA_BUCKET, 0, GameCategory.ARCADE, null, false, GameDisplay.Micro.getGameId()),

	BOMB_LOBBERS("Bomb Lobbers", null,
			new StatDisplay[]{StatDisplay.WINS, StatDisplay.GAMES_PLAYED, StatDisplay.KILLS, StatDisplay.DEATHS, StatDisplay.GEMS_EARNED},
			Material.FIREBALL, 0, GameCategory.ARCADE, "Waller Kit", false, GameDisplay.Lobbers.getGameId()),

	EVOLUTION("Evolution", null,
			new StatDisplay[]{StatDisplay.WINS, StatDisplay.GAMES_PLAYED, StatDisplay.KILLS, StatDisplay.DEATHS, StatDisplay.GEMS_EARNED},
			Material.MONSTER_EGG, 55 /* slime */, GameCategory.ARCADE, "Harvester Kit", false, GameDisplay.Evolution.getGameId()),

	MONSTER_MAZE("Monster Maze", null,
			new StatDisplay[]{StatDisplay.WINS, StatDisplay.GAMES_PLAYED, StatDisplay.DEATHS, StatDisplay.GEMS_EARNED},
			Material.ROTTEN_FLESH, 0, GameCategory.ARCADE, "SoonTM", false, GameDisplay.MonsterMaze.getGameId()),

	GLADIATORS("Gladiators", null,
			new StatDisplay[]{StatDisplay.WINS, StatDisplay.GAMES_PLAYED, StatDisplay.KILLS, StatDisplay.DEATHS, StatDisplay.GEMS_EARNED},
			Material.IRON_SWORD, 0, GameCategory.ARCADE, null, false, GameDisplay.Gladiators.getGameId()),

	/*TYPE_WARS("Type Wars", null,
			new StatDisplay[] {StatDisplay.WINS, StatDisplay.GAMES_PLAYED, new StatDisplay("Minions killed", "MinionKills"), new StatDisplay("Words Per Minute", false, true, "MinionKills", "TimeInGame"), StatDisplay.GEMS_EARNED},
			Material.NAME_TAG, 0, GameCategory.CLASSICS, null),*/

	SPEED_BUILDERS("Speed Builders", null,
			new StatDisplay[]{StatDisplay.WINS, StatDisplay.GAMES_PLAYED, StatDisplay.GEMS_EARNED, null, new StatDisplay("Perfect Builds", "PerfectBuild")},
			Material.QUARTZ_BLOCK, 0, GameCategory.GAME, null, false, GameDisplay.SpeedBuilders.getGameId()),

	/*ONE_IN_THE_QUIVER_PAYLOAD("One in the Quiver Payload", null,
			new StatDisplay[] {StatDisplay.WINS, StatDisplay.GAMES_PLAYED, StatDisplay.GEMS_EARNED},
			Material.EXPLOSIVE_MINECART, 0, GameCategory.CLASSICS, "Sky Warrior Kit", false, GameDisplay.QuiverPayload.getGameId()),*/

	SKYFALL("Skyfall", null,
			new StatDisplay[]{StatDisplay.WINS, StatDisplay.GAMES_PLAYED, StatDisplay.KILLS, StatDisplay.DEATHS, StatDisplay.GEMS_EARNED, null, new StatDisplay("Booster Rings", "Rings"),
					null, null, new StatDisplay(C.Bold + "Team Stats", true), null,
					StatDisplay.fromGame("Wins", GameDisplay.SkyfallTeams, "Wins"), StatDisplay.fromGame("Games Played", GameDisplay.SkyfallTeams, "Wins", "Losses"),
					StatDisplay.fromGame("Kills", GameDisplay.SkyfallTeams, "Kills"), StatDisplay.fromGame("Deaths", GameDisplay.SkyfallTeams, "Deaths"),
					StatDisplay.fromGame("Gems Earned", GameDisplay.SkyfallTeams, "GemsEarned"), null, StatDisplay.fromGame("Booster Rings", GameDisplay.SkyfallTeams, "Rings")},
			Material.DIAMOND_BOOTS, 0, GameCategory.GAME, null, false, GameDisplay.Skyfall.getGameId(), GameDisplay.SkyfallTeams.getGameId()),

	GEM_HUNTERS("Gem Hunters", null,
			new StatDisplay[]{StatDisplay.KILLS, StatDisplay.GEMS_EARNED, StatDisplay.fromGame("Quests Completed", GameDisplay.GemHunters, "QuestsCompleted"), StatDisplay.fromGame("Chests Opened", GameDisplay.GemHunters, "ChestsOpened")},
			Material.EMERALD, 0, GameCategory.GAME, null, false, GameDisplay.GemHunters.getGameId()),

	MOBA("Heroes of GWEN", null,
			new StatDisplay[]{StatDisplay.WINS, StatDisplay.GAMES_PLAYED, StatDisplay.KILLS, StatDisplay.DEATHS, StatDisplay.GEMS_EARNED, null, StatDisplay.fromGame("Gold Earned", GameDisplay.MOBA, "GoldEarned")},
			Material.PRISMARINE_SHARD, 0, GameCategory.GAME, null, false, GameDisplay.MOBA.getGameId()),

	CAKE_WARS("Cake Wars", null,
			new StatDisplay[]
					{
							StatDisplay.fromGame("Wins", GameDisplay.CakeWars4, "Wins"),
							StatDisplay.fromGame("Games Played", GameDisplay.CakeWars4, "Wins", "Losses"),
							StatDisplay.fromGame("Best Win Streak", GameDisplay.CakeWars4, "BestWinStreak"),
							StatDisplay.fromGame("Kills", GameDisplay.CakeWars4, "Kills"),
							StatDisplay.fromGame("Elimination Kills", GameDisplay.CakeWars4, "FinalKills"),
							StatDisplay.fromGame("Deaths", GameDisplay.CakeWars4, "Deaths"),
							StatDisplay.fromGame("Cake Bites", GameDisplay.CakeWars4, "Bites"),
							StatDisplay.fromGame("Whole Cakes", GameDisplay.CakeWars4, "EatWholeCake"),
							null,
							new StatDisplay(C.Bold + "Duos", true),
							StatDisplay.fromGame("Wins", GameDisplay.CakeWarsDuos, "Wins"),
							StatDisplay.fromGame("Games Played", GameDisplay.CakeWarsDuos, "Wins", "Losses"),
							StatDisplay.fromGame("Best Win Streak", GameDisplay.CakeWarsDuos, "BestWinStreak"),
							StatDisplay.fromGame("Kills", GameDisplay.CakeWarsDuos, "Kills"),
							StatDisplay.fromGame("Elimination Kills", GameDisplay.CakeWarsDuos, "FinalKills"),
							StatDisplay.fromGame("Deaths", GameDisplay.CakeWarsDuos, "Deaths"),
							StatDisplay.fromGame("Cake Bites", GameDisplay.CakeWarsDuos, "Bites"),
							StatDisplay.fromGame("Whole Cakes", GameDisplay.CakeWarsDuos, "EatWholeCake"),
					},
			Material.CAKE, 0, GameCategory.GAME, "Frosting Kit", false, GameDisplay.CakeWars4.getGameId(), GameDisplay.CakeWarsDuos.getGameId()),

	NANO_GAMES("Nano Games", null, new StatDisplay[]
			{
					StatDisplay.WINS,
					StatDisplay.GAMES_PLAYED,
					StatDisplay.KILLS,
					StatDisplay.DEATHS,
			}, Material.EGG, 0, GameCategory.GAME, null, false, GameDisplay.NanoGames.getGameId()),

	;

	private String _name;
	private String[] _statsToPull;
	private StatDisplay[] _statDisplays;
	private Material _icon;
	private GameCategory _gameCategory;
	private byte _iconData;
	private String _kitReward;
	public boolean DisplayDivision;
	public int[] GameId;

	AchievementCategory(String name, String[] statsToPull, StatDisplay[] statDisplays, Material icon, int iconData, GameCategory gameCategory, String kitReward, boolean displayDivision, int... gameId)
	{
		_name = name;

		if (statsToPull != null)
			_statsToPull = statsToPull;
		else
			_statsToPull = new String[]{name};
		_statDisplays = statDisplays;
		_icon = icon;
		_iconData = (byte) iconData;
		_gameCategory = gameCategory;
		_kitReward = kitReward;

		GameId = gameId;
		DisplayDivision = displayDivision;
	}

	public String getFriendlyName()
	{
		return _name;
	}

	public String getReward()
	{
		return _kitReward;
	}

	public Material getIcon()
	{
		return _icon;
	}

	public String[] getStatsToPull()
	{
		return _statsToPull;
	}

	public StatDisplay[] getStatsToDisplay()
	{
		return _statDisplays;
	}

	public byte getIconData()
	{
		return _iconData;
	}

	public GameCategory getGameCategory()
	{
		return _gameCategory;
	}

	public void addStats(CoreClientManager clientManager, StatsManager statsManager, List<String> lore, Player player, String targetName, PlayerStats targetStats)
	{
		addStats(clientManager, statsManager, lore, Integer.MAX_VALUE, player, targetName, targetStats);
	}

	public void addStats(CoreClientManager clientManager, StatsManager statsManager, List<String> lore, int max, Player player, String targetName, PlayerStats targetStats)
	{
		for (int i = 0; i < _statDisplays.length && i < max; i++)
		{
			// If the stat is null then just display a blank line instead
			if (_statDisplays[i] == null)
			{
				lore.add(" ");
				continue;
			}
			else if (_statDisplays[i].isJustDisplayName())
			{
				lore.add(ChatColor.RESET + _statDisplays[i].getDisplayName());
				continue;
			}

			String displayName = _statDisplays[i].getDisplayName();

			// Skip showing Losses, Kills, Deaths for other players
			if (!clientManager.Get(player).hasPermission(AchievementManager.Perm.SEE_FULL_STATS) && !player.getName().equals(targetName) && (displayName.contains("Losses") || displayName.contains("Kills") || displayName.contains("Deaths") || displayName.equals("Time In Game") || displayName.equals("Games Played")))
				continue;

			double statNumber = 0;


			// This is so we could load stats from other games
			// (Refer to team games, displaying team stats in the normal game view)
			if (_statDisplays[i].isFullStat())
			{
				for (String statName : _statDisplays[i].getStats())
					statNumber += targetStats.getStat(statName);
			}
			else
			{
				for (String statToPull : _statsToPull)
				{
					for (String statName : _statDisplays[i].getStats())
					{
						if (_statDisplays[i].isDivideStats())
						{
							if (statNumber == 0)
							{
								statNumber = targetStats.getStat(statToPull + "." + statName);
								continue;
							}
							double stat = targetStats.getStat(statToPull + "." + statName);
							if (stat == 0)
								statNumber = statNumber / 1;
							else
								statNumber = (double) statNumber / stat;
						}
						else
							statNumber += targetStats.getStat(statToPull + "." + statName);
					}
				}
			}

			String statString = C.cWhite + Math.round(statNumber);

			// doubles
			// Special display for Words per Minute
			if (displayName.equalsIgnoreCase("Words Per Minute"))
			{
				statString = C.cWhite + (double) statNumber;
				if (statString.length() > 7)
					statString = statString.substring(0, 7);

				lore.add(C.cYellow + displayName + ": " + statString);
				continue;
			}


			// ints
			// Need to display special for time
			if (displayName.equalsIgnoreCase("Time In Game"))
				statString = C.cWhite + UtilTime.convertString(Math.round(statNumber) * 1000L, 0, UtilTime.TimeUnit.FIT);


			lore.add(C.cYellow + displayName + ": " + statString);
		}
	}

	public enum GameCategory
	{
		STAND_ALONE, GAME, ARCADE, UHC, NANO_GAME
	}
}