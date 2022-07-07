package mineplex.core.game;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.DyeColor;
import org.bukkit.Material;

public enum GameDisplay implements Display
{
	//Mini
	BaconBrawl("Bacon Brawl", Material.PORK, (byte)0, GameCategory.NONE, 1, true),
	Barbarians("A Barbarians Life", Material.WOOD_AXE, (byte)0, GameCategory.NONE, 2, false),
	BossBattles("Boss Battles", Material.SKULL_ITEM, (byte) 0, GameCategory.EVENT, 55, false),
	Bridge("The Bridges", Material.IRON_PICKAXE, (byte)0, GameCategory.HARDCORE, 3, true),
	CastleAssault("Castle Assault", Material.DIAMOND_CHESTPLATE, (byte)0, GameCategory.NONE, 67, true),
	CastleAssaultTDM("Castle Assault TDM", Material.DIAMOND_CHESTPLATE, (byte)0, GameCategory.NONE, 68, false),
	CastleSiege("Castle Siege", Material.DIAMOND_CHESTPLATE, (byte)0, GameCategory.NONE, 4, true),
	ChampionsDominate("Champions Domination", "Champions", Material.BEACON, (byte)0, GameCategory.HARDCORE, 6, true),
	ChampionsTDM("Champions TDM", "Champions", Material.GOLD_SWORD, (byte)0, GameCategory.NONE, 5, true),
	Christmas("Christmas Chaos", Material.SNOW_BALL, (byte)0, GameCategory.EVENT, 8, false),
	ChristmasNew("Christmas Chaos II", Material.SNOW_BALL, (byte)0, GameCategory.EVENT, 74, false),
	DeathTag("Death Tag", Material.SKULL_ITEM, (byte)0, GameCategory.NONE, 9, true),
	DragonEscape("Dragon Escape", Material.DRAGON_EGG, (byte)0, GameCategory.NONE, 10, true),
	DragonEscapeTeams("Dragon Escape Teams", Material.DRAGON_EGG, (byte)0, GameCategory.NONE, 11, false),
	DragonRiders("Dragon Riders", Material.DRAGON_EGG, (byte)0, GameCategory.NONE, 12, false),
	Dragons("Dragons", Material.ENDER_STONE, (byte)0, GameCategory.NONE, 13, true),
	DragonsTeams("Dragons Teams", Material.DRAGON_EGG, (byte)0, GameCategory.NONE, 14, false),
	Draw("Draw My Thing", Material.BOOK_AND_QUILL, (byte)0, GameCategory.CASUAL, 15, true),
	ElytraRings("Elytra Rings", Material.ELYTRA, (byte) 0, GameCategory.NONE, 61, false),
	Evolution("Evolution", Material.EMERALD, (byte)0, GameCategory.NONE, 16, true),
	Gravity("Gravity", Material.ENDER_PORTAL_FRAME, (byte)0, GameCategory.NONE, 18, false),
	Halloween("Halloween Horror", Material.PUMPKIN, (byte)0, GameCategory.EVENT, 19, false),
	Halloween2016("Pumpkin's Revenge", Material.PUMPKIN, (byte)0, GameCategory.EVENT, 63, false),
	HideSeek("Block Hunt", Material.GRASS, (byte)0, GameCategory.INTERMEDIATE, 20, true),
	HoleInTheWall("Hole in the Wall", Material.STAINED_GLASS, (byte) 2, GameCategory.NONE, 52, false),
	Horse("Horseback", Material.IRON_BARDING, (byte)0, GameCategory.NONE, 21, false),
	
	Micro("Micro Battle", Material.LAVA_BUCKET, (byte)0, GameCategory.CASUAL, 24, true),
	MilkCow("Milk the Cow", Material.MILK_BUCKET, (byte)0, GameCategory.NONE, 27, false),
	MineStrike("MineStrike", Material.TNT, (byte)0, GameCategory.HARDCORE, 25, true),
	BawkBawkBattles("Bawk Bawk Battles", Material.EGG, (byte)0, GameCategory.NONE, 26, true),
	OldMineWare("Old MineWare", Material.PAPER, (byte)0, GameCategory.NONE, 26, false),
	Paintball("Super Paintball", Material.ENDER_PEARL, (byte)0, GameCategory.NONE, 28, true),
	Quiver("One in the Quiver", Material.ARROW, (byte)0, GameCategory.NONE, 29, true),
	QuiverTeams("One in the Quiver Teams", Material.ARROW, (byte)0, GameCategory.NONE, 30, false),
	Runner("Runner", Material.LEATHER_BOOTS, (byte)0, GameCategory.NONE, 31, true),
	SearchAndDestroy("Search and Destroy", Material.TNT, (byte)0, GameCategory.NONE, 32, false),
	Sheep("Sheep Quest", Material.WOOL, (byte)4, GameCategory.NONE, 33, true),

	Smash("Super Smash Mobs", Material.SKULL_ITEM, (byte)4, GameCategory.HARDCORE, 34, true),
	SmashDomination("Super Smash Mobs Domination", "Super Smash Mobs", Material.SKULL_ITEM, (byte)4, GameCategory.NONE, 36, false),
	SmashTeams("Super Smash Mobs Teams", "Super Smash Mobs", Material.SKULL_ITEM, (byte)4, GameCategory.NONE, 35, false),
	SmashTraining("Super Smash Mobs Training", "Super Smash Mobs", Material.SKULL_ITEM, (byte)4, GameCategory.NONE, 34, false),
	Snake("Snake", Material.WOOL, (byte)0, GameCategory.NONE, 37, true),
	SneakyAssassins("Sneaky Assassins", Material.INK_SACK, (byte)0, GameCategory.NONE, 38, true),
	SnowFight("Snow Fight", Material.SNOW_BALL, (byte)0, GameCategory.EVENT, 39, false),
	Spleef("Super Spleef", Material.IRON_SPADE, (byte)0, GameCategory.NONE, 40, true),
	SpleefTeams("Super Spleef Teams", Material.IRON_SPADE, (byte)0, GameCategory.NONE, 41, false),
	SquidShooter("Squid Shooter", Material.INK_SACK, (byte)0, GameCategory.NONE, 43, false),
	Stacker("Super Stacker", Material.BOWL, (byte)0, GameCategory.NONE, 42, false),
	SurvivalGames("Survival Games", Material.IRON_SWORD, (byte)0, GameCategory.INTERMEDIATE, 22, true),
	SurvivalGamesTeams("Survival Games Teams", "Survival Games", Material.IRON_SWORD, (byte)0, GameCategory.INTERMEDIATE, 23, false),
	Tug("Tug of Wool", Material.WHEAT, (byte)0, GameCategory.NONE, 44, false),
	TurfWars("Turf Wars", Material.STAINED_CLAY, (byte)14, GameCategory.CASUAL, 45, true),
	UHC("Ultra Hardcore", Material.GOLDEN_APPLE, (byte)0, GameCategory.NONE, 46, true),
	UHCSolo("Ultra Hardcore Solo", "Ultra Hardcore", Material.GOLDEN_APPLE, (byte)0, GameCategory.NONE, 46, false),
	UHCSoloSpeed("Ultra Hardcore Solo Speed", "Ultra Hardcore", Material.GOLDEN_APPLE, (byte)0, GameCategory.NONE, 67, false),
	UHCTeamsSpeed("Ultra Hardcore Teams Speed", "Ultra Hardcore", Material.GOLDEN_APPLE, (byte)0, GameCategory.NONE, 67, false),
	WitherAssault("Wither Assault", Material.SKULL_ITEM, (byte)1, GameCategory.NONE, 47, true),
	Wizards("Wizards", Material.BLAZE_ROD, (byte)0, GameCategory.NONE, 48, true),
	ZombieSurvival("Zombie Survival", Material.SKULL_ITEM, (byte)2, GameCategory.NONE, 49, false),

	Build("Master Builders", Material.WOOD, (byte)0, GameCategory.CASUAL, 50, true),
	BuildMavericks("Mavericks Master Builders", Material.WOOD, (byte)3, GameCategory.EVENT, 63, false),
	Cards("Craft Against Humanity", Material.MAP, (byte)0, GameCategory.NONE, 51, false),
	Skywars("Skywars", Material.FEATHER, (byte) 0, GameCategory.INTERMEDIATE, 52, true),
	SkywarsTeams("Skywars Teams", "Skywars", Material.FEATHER, (byte)0, GameCategory.INTERMEDIATE, 53, false),
	MonsterMaze("Monster Maze", Material.ROTTEN_FLESH, (byte)0, GameCategory.NONE, 55, true),
	MonsterLeague("Monster League", Material.MINECART, (byte)0, GameCategory.NONE, 56, false),
	
	Lobbers("Bomb Lobbers", Material.FIREBALL, (byte) 0, GameCategory.NONE, 54, true),
	
	Minecraft_League("MC League", Material.DIAMOND_SWORD, (byte)0, GameCategory.NONE, 62, false),

	ChampionsCTF("Champions CTF", "Champions", Material.BANNER, DyeColor.RED.getDyeData(), GameCategory.HARDCORE, 56, true),

	BouncyBalls("Bouncy Balls", Material.SLIME_BALL, (byte)0, GameCategory.NONE, 57, false),
	Gladiators("Gladiators", Material.IRON_SWORD, (byte)0, GameCategory.NONE, 58, true),
	TypeWars("Type Wars", Material.NAME_TAG, (byte) 0, GameCategory.NONE, 59, false),
	
	SpeedBuilders("Speed Builders", Material.QUARTZ_BLOCK, (byte) 0, GameCategory.INTERMEDIATE, 60, true),
	
	Valentines("Valentines Vendetta", Material.LEATHER, (byte)0, GameCategory.EVENT, 61, false),
	
	Skyfall("Skyfall", Material.DIAMOND_BOOTS, (byte)0, GameCategory.NONE, 62, true),
	SkyfallTeams("Skyfall Teams", Material.DIAMOND_BOOTS, (byte)0, GameCategory.NONE, 65, false),
	
	Basketball("Hoops", Material.SLIME_BALL, (byte)0, GameCategory.EVENT, 63, false),
	
	QuiverPayload("One in the Quiver Payload", Material.ARROW, (byte)0, GameCategory.NONE, 64, false),
	
	StrikeGames("Strike Games", Material.DIAMOND_LEGGINGS, (byte) 0, GameCategory.NONE, 66, false),

	AlienInvasion("Alien Invasion", Material.ENDER_STONE, (byte) 0, GameCategory.EVENT, 69, false),

	MOBA("Heroes of GWEN", Material.PRISMARINE, (byte)0, GameCategory.NONE, 70, true),
	MOBATraining("Heroes of GWEN Training", Material.PRISMARINE, (byte)0, GameCategory.NONE, 70, false),

	BattleRoyale("Battle Royale", Material.DIAMOND_SWORD, (byte)0, GameCategory.EVENT, 72, false),

	CakeWars4("Cake Wars Standard", "Cake Wars", Material.CAKE, (byte)0, GameCategory.INTERMEDIATE, 73, true),
	CakeWarsDuos("Cake Wars Duos", "Cake Wars", Material.SUGAR, (byte)0, GameCategory.INTERMEDIATE, 74, false),

	NanoGames("Nano Games", Material.JUKEBOX, (byte)0, GameCategory.CASUAL, 75, false),

	GemHunters("Gem Hunters", Material.EMERALD, (byte) 0, GameCategory.EVENT, 71, false),

	Event("Mineplex Event", Material.CAKE, (byte)0, GameCategory.EVENT, 999, false),
	
	Brawl("Brawl", Material.DIAMOND, (byte) 0, GameCategory.EVENT, 998, false);

	final String _name;
	final String _lobbyName;
	final Material _mat;
	final byte _data;
	final GameCategory _gameCategory;

	private final int _gameId;	// Unique identifying id for this gamemode (used for statistics)
	@Override
	public int getGameId() { return _gameId; }
	
	private boolean _communityFavorite;
	public boolean isCommunityFavoriteOption() { return _communityFavorite; }

	GameDisplay(String name, Material mat, byte data, GameCategory gameCategory, int gameId, boolean communityFavorite)
	{
		this(name, name, mat, data, gameCategory, gameId, communityFavorite);
	}

	GameDisplay(String name, String lobbyName, Material mat, byte data, GameCategory gameCategory, int gameId, boolean communityFavorite)
	{
		_name = name;
		_lobbyName = lobbyName;
		_mat = mat;
		_data = data;
		_gameCategory = gameCategory;
		_gameId = gameId;
		_communityFavorite = communityFavorite;
	}

	@Override
	public String getName()
	{
		return _name;
	}

	public String getLobbyName()
	{
		return _lobbyName;
	}

	@Override
	public Material getMaterial()
	{
		return _mat;
	}

	@Override
	public byte getMaterialData()
	{
		return _data;
	}

	public GameCategory getGameCategory()
	{
		return _gameCategory;
	}

	public static GameDisplay matchName(String name)
	{
		for (GameDisplay display : values())
		{
			if (display.getName().equalsIgnoreCase(name))
			{
				return display;
			}
		}
		return null;
	}

	//May need to actually add this to each game individually, but for now, LobbyName works fine
	public String getKitGameName() 
	{
		return _lobbyName;
	}

	public String getCustomDataKeyName() { return "arcade." + _name.toLowerCase().replaceAll(" ", ".") + "."; }

	private static final Map<Integer, GameDisplay> BY_ID;

	static
	{
		Map<Integer, GameDisplay> byId = new HashMap<>();

		for (GameDisplay gameDisplay : values())
		{
			byId.put(gameDisplay.getGameId(), gameDisplay);
		}
		BY_ID = Collections.unmodifiableMap(byId);
	}

	public static GameDisplay getById(int id)
	{
		return BY_ID.get(id);
	}
}