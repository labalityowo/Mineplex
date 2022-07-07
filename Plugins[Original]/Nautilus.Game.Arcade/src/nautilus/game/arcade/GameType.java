package nautilus.game.arcade;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.MinecraftVersion;
import mineplex.core.common.Pair;
import mineplex.core.game.GameDisplay;
import mineplex.core.itemstack.ItemBuilder;

import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.game.games.alieninvasion.AlienInvasion;
import nautilus.game.arcade.game.games.baconbrawl.BaconBrawl;
import nautilus.game.arcade.game.games.barbarians.Barbarians;
import nautilus.game.arcade.game.games.basketball.Basketball;
import nautilus.game.arcade.game.games.battleroyale.BattleRoyaleSolo;
import nautilus.game.arcade.game.games.bossbattles.BossBattles;
import nautilus.game.arcade.game.games.bridge.Bridge;
import nautilus.game.arcade.game.games.bridge.modes.OverpoweredBridge;
import nautilus.game.arcade.game.games.build.Build;
import nautilus.game.arcade.game.games.buildmavericks.BuildMavericks;
import nautilus.game.arcade.game.games.cakewars.CakeWars;
import nautilus.game.arcade.game.games.cakewars.modes.CakeWarsDuos;
import nautilus.game.arcade.game.games.cakewars.modes.OPCakeWars;
import nautilus.game.arcade.game.games.castleassault.CastleAssault;
import nautilus.game.arcade.game.games.castleassault.CastleAssaultTDM;
import nautilus.game.arcade.game.games.castlesiegenew.CastleSiegeNew;
import nautilus.game.arcade.game.games.champions.ChampionsCTF;
import nautilus.game.arcade.game.games.champions.ChampionsDominate;
import nautilus.game.arcade.game.games.champions.ChampionsTDM;
import nautilus.game.arcade.game.games.christmas.Christmas;
import nautilus.game.arcade.game.games.christmasnew.ChristmasNew;
import nautilus.game.arcade.game.games.deathtag.DeathTag;
import nautilus.game.arcade.game.games.dragonescape.DragonEscape;
import nautilus.game.arcade.game.games.dragons.Dragons;
import nautilus.game.arcade.game.games.dragons.DragonsTeams;
import nautilus.game.arcade.game.games.draw.Draw;
import nautilus.game.arcade.game.games.event.EventGame;
import nautilus.game.arcade.game.games.evolution.Evolution;
import nautilus.game.arcade.game.games.gladiators.Gladiators;
import nautilus.game.arcade.game.games.gravity.Gravity;
import nautilus.game.arcade.game.games.halloween.Halloween;
import nautilus.game.arcade.game.games.halloween2016.Halloween2016;
import nautilus.game.arcade.game.games.hideseek.HideSeek;
import nautilus.game.arcade.game.games.lobbers.BombLobbers;
import nautilus.game.arcade.game.games.micro.Micro;
import nautilus.game.arcade.game.games.milkcow.MilkCow;
import nautilus.game.arcade.game.games.minecraftleague.MinecraftLeague;
import nautilus.game.arcade.game.games.minestrike.Minestrike;
import nautilus.game.arcade.game.games.mineware.BawkBawkBattles;
import nautilus.game.arcade.game.games.moba.MobaClassic;
import nautilus.game.arcade.game.games.monstermaze.MonsterMaze;
import nautilus.game.arcade.game.games.paintball.Paintball;
import nautilus.game.arcade.game.games.quiver.Quiver;
import nautilus.game.arcade.game.games.quiver.QuiverTeamBase;
import nautilus.game.arcade.game.games.quiver.QuiverTeams;
import nautilus.game.arcade.game.games.rings.ElytraRings;
import nautilus.game.arcade.game.games.runner.Runner;
import nautilus.game.arcade.game.games.sheep.SheepGame;
import nautilus.game.arcade.game.games.skyfall.SoloSkyfall;
import nautilus.game.arcade.game.games.skyfall.TeamSkyfall;
import nautilus.game.arcade.game.games.skywars.SoloSkywars;
import nautilus.game.arcade.game.games.skywars.TeamSkywars;
import nautilus.game.arcade.game.games.smash.SoloSuperSmash;
import nautilus.game.arcade.game.games.smash.SuperSmashDominate;
import nautilus.game.arcade.game.games.smash.SuperSmashTraining;
import nautilus.game.arcade.game.games.smash.TeamSuperSmash;
import nautilus.game.arcade.game.games.snake.Snake;
import nautilus.game.arcade.game.games.sneakyassassins.SneakyAssassins;
import nautilus.game.arcade.game.games.snowfight.SnowFight;
import nautilus.game.arcade.game.games.speedbuilders.SpeedBuilders;
import nautilus.game.arcade.game.games.spleef.Spleef;
import nautilus.game.arcade.game.games.squidshooters.SquidShooters;
import nautilus.game.arcade.game.games.survivalgames.SurvivalGamesNewSolo;
import nautilus.game.arcade.game.games.survivalgames.SurvivalGamesNewTeams;
import nautilus.game.arcade.game.games.survivalgames.modes.OverpoweredSGSolo;
import nautilus.game.arcade.game.games.survivalgames.modes.OverpoweredSGTeams;
import nautilus.game.arcade.game.games.tug.TugOfWool;
import nautilus.game.arcade.game.games.turfforts.TurfForts;
import nautilus.game.arcade.game.games.typewars.TypeWars;
import nautilus.game.arcade.game.games.uhc.UHCSolo;
import nautilus.game.arcade.game.games.uhc.UHCSoloSpeed;
import nautilus.game.arcade.game.games.uhc.UHCTeams;
import nautilus.game.arcade.game.games.uhc.UHCTeamsSpeed;
import nautilus.game.arcade.game.games.valentines.Valentines;
import nautilus.game.arcade.game.games.wither.WitherGame;
import nautilus.game.arcade.game.games.wizards.Wizards;
import nautilus.game.arcade.game.games.zombiesurvival.ZombieSurvival;
import nautilus.game.arcade.managers.voting.Voteable;

public enum GameType implements Voteable
{	
	//Mini
	BaconBrawl(BaconBrawl.class, GameDisplay.BaconBrawl),
	Barbarians(Barbarians.class, GameDisplay.Barbarians),
	Basketball(Basketball.class, GameDisplay.Basketball),
	BossBattles(BossBattles.class, GameDisplay.BossBattles),
	Bridge(Bridge.class, new GameMode[]
			{
				new GameMode(OverpoweredBridge.class, "OP Bridges"),
			}, GameDisplay.Bridge),
	CastleAssault(CastleAssault.class, GameDisplay.CastleAssault),
	CastleAssaultTDM(CastleAssaultTDM.class, GameDisplay.CastleAssaultTDM),
	CastleSiege(CastleSiegeNew.class, GameDisplay.CastleSiege),
	ChampionsCTF(ChampionsCTF.class, GameDisplay.ChampionsCTF),
	ChampionsDominate(ChampionsDominate.class, GameDisplay.ChampionsDominate),
	ChampionsTDM(ChampionsTDM.class, GameDisplay.ChampionsTDM),
	Christmas(Christmas.class, GameDisplay.Christmas, new Pair[] 
			{
				//Pair.create(MinecraftVersion.ALL, "http://file.mineplex.com/ResChristmas.zip")
				Pair.create(MinecraftVersion.Version1_8, "http://198.20.72.74/ResChristmas.zip")
			}, true),
	ChristmasNew(ChristmasNew.class, GameDisplay.ChristmasNew, new Pair[]
			{
				//Pair.create(MinecraftVersion.Version1_8, "http://file.mineplex.com/ResChristmas2.zip"),
				//Pair.create(MinecraftVersion.Version1_9, "http://file.mineplex.com/ResChristmas219.zip")
				Pair.create(MinecraftVersion.Version1_8, "http://198.20.72.74/ResChristmas2.zip"),
				Pair.create(MinecraftVersion.Version1_9, "http://198.20.72.74/ResChristmas219.zip")
			}, false),
	DeathTag(DeathTag.class, GameDisplay.DeathTag),
	DragonEscape(DragonEscape.class, GameDisplay.DragonEscape),
	Dragons(Dragons.class, new GameMode[]
			{
				new GameMode(DragonsTeams.class, "Teams Mode")
			}, GameDisplay.Dragons),
	Draw(Draw.class, GameDisplay.Draw, new Pair[]
			{
				//Pair.create(MinecraftVersion.ALL, "http://file.mineplex.com/ResDrawMyThing.zip")
				Pair.create(MinecraftVersion.Version1_8, "http://198.20.72.74/ResDrawMyThing.zip"),
				Pair.create(MinecraftVersion.Version1_13, "http://198.20.72.74/ResDrawMyThing113.zip")
			}, true),
	ElytraRings(ElytraRings.class, GameDisplay.ElytraRings),
	Evolution(Evolution.class, GameDisplay.Evolution),
	Gravity(Gravity.class, GameDisplay.Gravity),
	Halloween(Halloween.class, GameDisplay.Halloween, new Pair[] 
			{
				//Pair.create(MinecraftVersion.ALL, "http://file.mineplex.com/ResHalloween.zip")
				Pair.create(MinecraftVersion.Version1_8, "http://198.20.72.74/ResHalloween.zip")
			}, true),
	Halloween2016(Halloween2016.class, GameDisplay.Halloween2016),
	HideSeek(HideSeek.class, GameDisplay.HideSeek),
	Lobbers(BombLobbers.class, GameDisplay.Lobbers),
	Micro(Micro.class, GameDisplay.Micro),
	MilkCow(MilkCow.class, GameDisplay.MilkCow),
	MineStrike(Minestrike.class, GameDisplay.MineStrike, new Pair[] 
			{
				//Pair.create(MinecraftVersion.Version1_8, "http://file.mineplex.com/ResMinestrike.zip"),
				//Pair.create(MinecraftVersion.Version1_9, "http://file.mineplex.com/ResMinestrike19.zip")
				Pair.create(MinecraftVersion.Version1_8, "http://198.20.72.74/ResMinestrike.zip"),
				Pair.create(MinecraftVersion.Version1_9, "http://198.20.72.74/ResMinestrike19.zip"),
				Pair.create(MinecraftVersion.Version1_13, "http://198.20.72.74/ResMinestrike113.zip")
			}, true),
	BawkBawkBattles(BawkBawkBattles.class, GameDisplay.BawkBawkBattles),
	MinecraftLeague(MinecraftLeague.class, GameDisplay.Minecraft_League),
	Paintball(Paintball.class, GameDisplay.Paintball),
	Quiver(Quiver.class, GameDisplay.Quiver),
	QuiverPayload(QuiverTeamBase.class, GameDisplay.QuiverPayload),
	QuiverTeams(QuiverTeams.class, GameDisplay.QuiverTeams),
	Runner(Runner.class, GameDisplay.Runner),
	Sheep(SheepGame.class, GameDisplay.Sheep),
	TypeWars(TypeWars.class, GameDisplay.TypeWars),
	
	Smash(SoloSuperSmash.class, GameDisplay.Smash),
	SmashDomination(SuperSmashDominate.class, GameDisplay.SmashDomination),
	SmashTeams(TeamSuperSmash.class, GameDisplay.SmashTeams, new GameType[]{GameType.Smash}, false),
	SmashTraining(SuperSmashTraining.class, GameDisplay.SmashTraining),
	Snake(Snake.class, GameDisplay.Snake),
	SneakyAssassins(SneakyAssassins.class, GameDisplay.SneakyAssassins),
	SnowFight(SnowFight.class, GameDisplay.SnowFight),
	SpeedBuilders(SpeedBuilders.class, GameDisplay.SpeedBuilders),
	Spleef(Spleef.class, GameDisplay.Spleef),
	SurvivalGames(SurvivalGamesNewSolo.class, new GameMode[]
			{
					new GameMode(OverpoweredSGSolo.class, "Overpowered")
			}, GameDisplay.SurvivalGames),
	SurvivalGamesTeams(SurvivalGamesNewTeams.class, new GameMode[]
			{
					new GameMode(OverpoweredSGTeams.class, "Overpowered")
			}, GameDisplay.SurvivalGamesTeams, new GameType[]{GameType.SurvivalGames}, false),
	TurfWars(TurfForts.class, GameDisplay.TurfWars),
	UHC(UHCTeams.class, GameDisplay.UHC),
	UHCSolo(UHCSolo.class, GameDisplay.UHCSolo, new GameType[] { GameType.UHC }, false),
	UHCSoloSpeed(UHCSoloSpeed.class, GameDisplay.UHCSoloSpeed, new GameType[] { GameType.UHC }, false),
	UHCTeamsSpeed(UHCTeamsSpeed.class, GameDisplay.UHCTeamsSpeed, new GameType[] { GameType.UHC }, false),
	WitherAssault(WitherGame.class, GameDisplay.WitherAssault),
	Wizards(Wizards.class, GameDisplay.Wizards, new Pair[] 
			{
				//Pair.create(MinecraftVersion.ALL, "http://file.mineplex.com/ResWizards.zip")
				Pair.create(MinecraftVersion.Version1_8, "http://198.20.72.74/ResWizards.zip")
			}, true),
	ZombieSurvival(ZombieSurvival.class, GameDisplay.ZombieSurvival),
	Build(Build.class, GameDisplay.Build),
	BuildMavericks(BuildMavericks.class, GameDisplay.BuildMavericks),
	Skywars(SoloSkywars.class, GameDisplay.Skywars),
	SkywarsTeams(TeamSkywars.class, GameDisplay.SkywarsTeams, new GameType[]{GameType.Skywars}, false),
	MonsterMaze(MonsterMaze.class, GameDisplay.MonsterMaze),
	Gladiators(Gladiators.class, GameDisplay.Gladiators),
	Skyfall(SoloSkyfall.class, GameDisplay.Skyfall),
	SkyfallTeams(TeamSkyfall.class, GameDisplay.SkyfallTeams, new GameType[]{GameType.Skyfall}, false),

	Valentines(Valentines.class, GameDisplay.Valentines),

	AlienInvasion(AlienInvasion.class, GameDisplay.AlienInvasion),

	MOBA(MobaClassic.class, GameDisplay.MOBA),

	BattleRoyale(BattleRoyaleSolo.class, GameDisplay.BattleRoyale, new Pair[]
			{
				//Pair.create(MinecraftVersion.Version1_8, "http://file.mineplex.com/ResStrikeGames18.zip"),
				//Pair.create(MinecraftVersion.Version1_9, "http://file.mineplex.com/ResStrikeGames19.zip")
				Pair.create(MinecraftVersion.Version1_8, "http://198.20.72.74/ResStrikeGames18.zip"),
				Pair.create(MinecraftVersion.Version1_9, "http://198.20.72.74/ResStrikeGames19.zip")
			}, true),

	CakeWars4(CakeWars.class, new GameMode[]
			{
				new GameMode(OPCakeWars.class, "Sugar Rush")
			},GameDisplay.CakeWars4),
	CakeWarsDuos(CakeWarsDuos.class, GameDisplay.CakeWarsDuos),

	SquidShooters(SquidShooters.class, GameDisplay.SquidShooter),
	Tug(TugOfWool.class, GameDisplay.Tug),

	Event(EventGame.class, GameDisplay.Event, new GameType[]{
		GameType.BaconBrawl, GameType.Barbarians, GameType.Bridge, GameType.Build, GameType.Build,
		GameType.CastleSiege, GameType.ChampionsDominate, GameType.ChampionsTDM, GameType.Christmas,
		GameType.DeathTag, GameType.DragonEscape, GameType.Dragons,
		GameType.Draw, GameType.Evolution, GameType.Gravity, GameType.Halloween, GameType.HideSeek,
		GameType.Micro, GameType.MilkCow, GameType.MineStrike, GameType.BawkBawkBattles,
		GameType.Paintball, GameType.Quiver, GameType.QuiverTeams, GameType.Runner,
		GameType.Sheep, GameType.Skywars, GameType.SkywarsTeams, GameType.Smash, GameType.SmashDomination, GameType.SmashTeams,
		GameType.Snake, GameType.SneakyAssassins, GameType.SnowFight, GameType.Spleef,
		GameType.SurvivalGames, GameType.SurvivalGamesTeams, GameType.TurfWars, GameType.UHC,
		GameType.WitherAssault, GameType.Wizards, GameType.ZombieSurvival}, true),

	;

	GameDisplay _display;
	boolean _enforceResourcePack;
	GameType[] _mapSource;
	boolean _ownMaps;
	Pair<MinecraftVersion, String>[] _resourcePacks;
	Class<? extends Game> _gameClass;
	
	private GameMode[] _gameModes;
	private boolean _gameMaps;
	
	private int _gameId;	// Unique identifying id for this gamemode (used for statistics)
	public int getGameId() { return _gameId; }

	GameType(Class<? extends Game> gameClass, GameDisplay display)
	{
		this(gameClass, new GameMode[]{}, display, null, false, null, true, false);
	}

	GameType(Class<? extends Game> gameClass, GameDisplay display, Pair<MinecraftVersion, String>[] resourcePackUrl, boolean enforceResourcePack)
	{
		this(gameClass, new GameMode[]{}, display, resourcePackUrl, enforceResourcePack, null, true, false);
	}
	
	GameType(Class<? extends Game> gameClass, GameDisplay display, GameType[] mapSource, boolean ownMap)
	{
		this(gameClass, new GameMode[]{}, display, null, false, mapSource, ownMap, false);
	}
	
	GameType(Class<? extends Game> gameClass, GameMode[] gameModes, GameDisplay display)
	{
		this(gameClass, gameModes, display, null, false, null, true, false);
	}

	GameType(Class<? extends Game> gameClass, GameMode[] gameModes, GameDisplay display, Pair<MinecraftVersion, String>[] resourcePackUrl, boolean enforceResourcePack)
	{
		this(gameClass, gameModes, display, resourcePackUrl, enforceResourcePack, null, true, false);
	}
	
	GameType(Class<? extends Game> gameClass, GameMode[] gameModes, GameDisplay display, GameType[] mapSource, boolean ownMap)
	{
		this(gameClass, gameModes, display, null, false, mapSource, ownMap, false);
	}
	
	GameType(Class<? extends Game> gameClass, GameMode[] gameModes, GameDisplay display, Pair<MinecraftVersion, String>[] resourcePackUrls, boolean enforceResourcePack, GameType[] mapSource, boolean ownMaps, boolean gameMaps)
	{
		_display = display;
		_gameId = display.getGameId();
		_gameClass = gameClass;
		_gameModes = gameModes;
		_resourcePacks = resourcePackUrls;
		_enforceResourcePack = enforceResourcePack;
		_mapSource = mapSource;
		_ownMaps = ownMaps;
		_gameMaps = gameMaps;
	}
	
	public Class<? extends Game> getGameClass()
	{
		return _gameClass;
	}
	
	public GameMode[] getGameModes()
	{
		return _gameModes;
	}

	public boolean isEnforceResourcePack(Game game)
	{
		GameMode gameMode = getGameMode(game.getClass());

		return gameMode == null ? _enforceResourcePack : gameMode.enforceResourcePack();
	}	

	public Pair<MinecraftVersion, String>[] getResourcePackUrls(Game game)
	{
		GameMode gameMode = getGameMode(game.getClass());

		return gameMode == null ? _resourcePacks : gameMode.getResPackURLs();
	}
		
	public GameType[] getMapSource()
	{
		return _mapSource;
	}

	public boolean ownMaps()
	{
		return _ownMaps;
	}

	public GameDisplay getDisplay()
	{
		return this._display;
	}

	@Override
	public String getName()
	{
		return _display.getName();
	}

	@Override
	public String getDisplayName()
	{
		return getName();
	}

	public String GetLobbyName()
	{
		return _display.getLobbyName();
	}
	
	public Material GetMaterial()
	{
		return _display.getMaterial();
	}
	
	public byte GetMaterialData()
	{
		return _display.getMaterialData();
	}

	@Override
	public ItemStack getItemStack()
	{
		return new ItemBuilder(GetMaterial(), GetMaterialData()).build();
	}

	public GameMode getGameMode(Class<? extends Game> game)
	{
		for (GameMode mode : getGameModes())
		{
			if (game.equals(mode.getGameClass()))
			{
				return mode;
			}
		}

		return null;
	}

	public boolean hasGameModes()
	{
		return _gameModes.length != 0;
	}
}
