package mineplex.mapparser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public enum GameType
{	
	// Stand Alone
	Other("Other"),
	Unknown("Unknown"),
	Lobby("Lobby"),
	Event("Mineplex Event"),
	GemHunters("Gem Hunters"),
	
	// Games
	BaconBrawl("Bacon Brawl"),
	Barbarians("A Barbarians Life"),
	Bridge("The Bridges"),
	Build("Master Builders"),
	BuildMavericks("Mavericks Master Builders"),
	CakeWars2("Cake Wars Duos"),
	CakeWars4("Cake Wars Standard"),
	CastleSiege("Castle Siege"),
	CastleAssault("Castle Assault"),
	CastleAssaultTDM("Castle Assault TDM"),
	ChampionsTDM("Champions TDM", "Champions"),
	ChampionsDominate("Champions Domination", "Champions"),
	ChampionsCTF("Champions CTF", "Champions"),
	ChampionsMOBA("Champions MOBA", "Champions"),
	Christmas("Christmas Chaos"),
	DeathTag("Death Tag"),
	DragonEscape("Dragon Escape"),
	DragonEscapeTeams("Dragon Escape Teams"),
	DragonRiders("Dragon Riders"),
	Dragons("Dragons"),
	DragonsTeams("Dragons Teams"),
	Draw("Draw My Thing"),
	Evolution("Evolution"),
	FlappyBird("Flappy Bird"),
	Gladiators("Gladiators"),
	Gravity("Gravity"),
	Halloween("Halloween Horror"),
	Halloween2016("Halloween Horror 2016"),
	HideSeek("Block Hunt"),
	Horse("Horseback"),
	Lobbers("Bomb Lobbers"),
	SurvivalGames("Survival Games"),
	SurvivalGamesTeams("Survival Games Teams"),
	Micro("Micro Battle"),
	MineStrike("MineStrike"),
	MineWare("MineWare"),
	MinecraftLeague("MCL"),
	MilkCow("Milk the Cow"),
	HOG("Heroes of GWEN"),
	MonsterLeague("MonsterLeague"),
	MonsterMaze("Monster Maze"),
	NanoGames("Nano Games"),
	Paintball("Super Paintball"),
	Quiver("One in the Quiver"),
	QuiverTeams("One in the Quiver Teams"),
	Runner("Runner"),
	SearchAndDestroy("Search and Destroy"),
	Sheep("Sheep Quest"),
	Skyfall("Skyfall"),
	Skywars("Skywars"),
	Smash("Super Smash Mobs"),
	SmashTeams("Super Smash Mobs Teams", "Super Smash Mobs"),
	SmashDomination("Super Smash Mobs Domination", "Super Smash Mobs"),
	Snake("Snake"),
	SneakyAssassins("Sneaky Assassins"),
	SpeedBuilders("Speed Builders"),
	SnowFight("Snow Fight"),
	Spleef("Super Spleef"),
	SpleefTeams("Super Spleef Teams"),
	Stacker("Super Stacker"),
	SquidShooter("Squid Shooter"),
	Tug("Tug of Wool"),
	TurfWars("Turf Wars"),
	UHC("Ultra Hardcore"),
	WitherAssault("Wither Assault"),
	Wizards("Wizards"),
	ZombieSurvival("Zombie Survival"),	
	
	// Build States
	Upload("Upload"),
	Submissions("Submissions"),
	InProgress("In Progress"),
	
	None("None");
	
	String _name;
	String _lobbyName;

	GameType(String name)
	{
		_name = name;
		_lobbyName = name;
	}
	
	GameType(String name, String lobbyName)
	{
		_name = name;
		_lobbyName = lobbyName;
	}

	public String GetName()
	{
		return _name;
	}
	
	public String GetLobbyName()
	{
		return _lobbyName;
	}

	public static GameType match(String string)
	{
		GameType gameType = null;
		string = string.toLowerCase();
		for (GameType type : values())
		{
			if (type.name().toLowerCase().startsWith(string) || type.GetName().toLowerCase().startsWith(string))
			{
				gameType = type;
			}
		}
		return gameType;
	}

	public List<String> getMapNames()
	{
		File mapsFolder = new File("map" + File.separator + GetName());

		if (!mapsFolder.exists())
		{
			return null;
		}

		List<String> mapNames = new ArrayList<>();

		File[] files = mapsFolder.listFiles();

		if (files == null)
		{
			return null;
		}

		for (File file : files)
		{
			if (!file.isDirectory())
			{
				continue;
			}

			mapNames.add(file.getName());
		}

		return mapNames;
	}

	public static List<String> getAllMapNames()
	{
		List<String> mapNames = new ArrayList<>();

		for (GameType gameType : GameType.values())
		{
			List<String> gameMapNames = gameType.getMapNames();

			if (gameMapNames == null)
			{
				continue;
			}

			mapNames.addAll(gameMapNames);
		}

		return mapNames;
	}
}