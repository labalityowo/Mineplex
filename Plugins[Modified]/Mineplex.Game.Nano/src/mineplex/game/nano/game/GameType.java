package mineplex.game.nano.game;

import java.io.File;

import mineplex.core.game.GameDisplay;
import mineplex.core.game.nano.NanoDisplay;
import mineplex.game.nano.NanoManager;
import mineplex.game.nano.game.games.bawkbawk.BawkBawk;
import mineplex.game.nano.game.games.chickenshoot.ChickenShoot;
import mineplex.game.nano.game.games.colourchange.ColourChange;
import mineplex.game.nano.game.games.copycat.CopyCat;
import mineplex.game.nano.game.games.deathtag.DeathTag;
import mineplex.game.nano.game.games.dropper.Dropper;
import mineplex.game.nano.game.games.findores.FindOres;
import mineplex.game.nano.game.games.hotpotato.HotPotato;
import mineplex.game.nano.game.games.jumprope.JumpRope;
import mineplex.game.nano.game.games.kingslime.KingSlime;
import mineplex.game.nano.game.games.microbattle.MicroBattle;
import mineplex.game.nano.game.games.minekart.MineKart;
import mineplex.game.nano.game.games.musicminecart.MusicMinecarts;
import mineplex.game.nano.game.games.oits.SnowballTrouble;
import mineplex.game.nano.game.games.parkour.Parkour;
import mineplex.game.nano.game.games.quick.Quick;
import mineplex.game.nano.game.games.redgreenlight.RedGreenLight;
import mineplex.game.nano.game.games.reversetag.ReverseTag;
import mineplex.game.nano.game.games.slimecycles.SlimeCycles;
import mineplex.game.nano.game.games.spleef.Spleef;
import mineplex.game.nano.game.games.sploor.Sploor;
import mineplex.game.nano.game.games.territory.Territory;
import mineplex.game.nano.game.games.wizards.Wizards;

public enum GameType
{

	HOT_POTATO(HotPotato.class, NanoDisplay.HOT_POTATO),
	REVERSE_TAG(ReverseTag.class, NanoDisplay.REVERSE_TAG),
	MICRO_BATTLE(MicroBattle.class, NanoDisplay.MICRO_BATTLE, mapsFromArcade(GameDisplay.Micro)),
	DEATH_TAG(DeathTag.class, NanoDisplay.DEATH_TAG, mapsFromArcade(GameDisplay.DeathTag)),
	RED_GREEN_LIGHT(RedGreenLight.class, NanoDisplay.RED_GREEN_LIGHT),
	COLOUR_CHANGE(ColourChange.class, NanoDisplay.COLOUR_CHANGE),
	SPLOOR(Sploor.class, NanoDisplay.SPLOOR),
	KING_SLIME(KingSlime.class, NanoDisplay.KING_SLIME),
	SPLEEF(Spleef.class, NanoDisplay.SPLEEF, mapsFromArcade(GameDisplay.Runner)),
	JUMP_ROPE(JumpRope.class, NanoDisplay.JUMP_ROPE),
	FIND_ORES(FindOres.class, NanoDisplay.FIND_ORES),
	MUSIC_MINECARTS(MusicMinecarts.class, NanoDisplay.MUSIC_MINECARTS),
//	MOB_FARM(MobFarm.class, NanoDisplay.MOB_FARM),
	CHICKEN_SHOOT(ChickenShoot.class, NanoDisplay.CHICKEN_SHOOT),
	SLIME_CYCLES(SlimeCycles.class, NanoDisplay.SLIME_CYCLES),
	TERRITORY(Territory.class, NanoDisplay.TERRITORY),
	COPY_CAT(CopyCat.class, NanoDisplay.COPY_CAT),
	SNOWBALL_TROUBLE(SnowballTrouble.class, NanoDisplay.SNOWBALL_TROUBLE, mapsFromArcade(GameDisplay.Quiver)),
	PARKOUR(Parkour.class, NanoDisplay.PARKOUR),
	BAWK_BAWK(BawkBawk.class, NanoDisplay.BAWK_BAWK),
	QUICK(Quick.class, NanoDisplay.QUICK),
	DROPPER(Dropper.class, NanoDisplay.DROPPER),
	MINEKART(MineKart.class, NanoDisplay.MINEKART),
	WIZARDS(Wizards.class, NanoDisplay.WIZARDS, mapsFromArcade(GameDisplay.Wizards)),

	;

	private static String maps()
	{
		return ".." + File.separator + ".." + File.separator + "update" + File.separator + "maps";
	}

	private static String mapsFromArcade(GameDisplay display)
	{
		return maps() + File.separator + display.getName();
	}

	private static String mapsFromNano(NanoDisplay display)
	{
		return mapsFromArcade(NanoManager.getGameDisplay()) + File.separator + display.getName();
	}

	private final Class<? extends Game> _gameClass;
	private final NanoDisplay _display;
	private final String _mapDirectory;

	GameType(Class<? extends Game> gameClass, NanoDisplay display)
	{
		this(gameClass, display, mapsFromNano(display));
	}

	GameType(Class<? extends Game> gameClass, NanoDisplay display, String mapDirectory)
	{
		_gameClass = gameClass;
		_display = display;
		_mapDirectory = mapDirectory;
	}

	public Class<? extends Game> getGameClass()
	{
		return _gameClass;
	}

	public NanoDisplay getDisplay()
	{
		return _display;
	}

	public String getName()
	{
		return _display.getName();
	}

	public String getMapDirectory()
	{
		return _mapDirectory;
	}
}
