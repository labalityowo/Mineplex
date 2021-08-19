package nautilus.game.arcade.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.GameMode;
import nautilus.game.arcade.GameType;
import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.managers.GameCreationManager;

public class SetCommand extends CommandBase<ArcadeManager>
{
	public static final String MODE_PREFIX = "@e";
	public static final String SOURCE_PREFIX = "@s";
	public static final String MAP_PREFIX = "@m";

	public SetCommand(ArcadeManager plugin)
	{
		super(plugin, GameCommand.Perm.GAME_COMMAND_DUMMY_PERM, "set");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		if (!Plugin.canPlayerUseGameCmd(caller))
		{
			return;
		}

		if (args.length == 0)
		{
			caller.sendMessage(F.help(String.format("/game set <gametype> [%s(gamemode)] [%s(mapsource)] [%s(mapname)]", MODE_PREFIX, SOURCE_PREFIX, MAP_PREFIX), "Set the current game or next game", ChatColor.DARK_RED));
			return;
		}

		if (!isValid(args))
		{
			UtilPlayer.message(caller, F.main("Game", "The order of the arguments is invalid"));
			return;
		}

		String game = args[0];

		//Parse Game
		List<GameType> matches = matchGameType(game, false);

		if (matches.size() == 0)
		{
			UtilPlayer.message(caller, F.main("Game", "Could not find a GameType matching " + F.elem(game)));
			return;
		}

		if (matches.size() > 1)
		{
			UtilPlayer.message(caller, F.main("Game", "Found multiple GameTypes matching " + F.elem(game)));
			UtilPlayer.sendMatches(caller, "Game", matches, Enum::name);
			return;
		}

		GameType gameType = matches.get(0);

		String gameModeStr = findGameMode(args);
		GameMode selectedMode = null;
		String mapSourceStr = findGameSource(args);
		GameType selectedSource = null;
		String mapStr = findMap(args);
		String selectedMap = null;

		if (gameModeStr != null)
		{
			if (!gameType.hasGameModes())
			{
				UtilPlayer.message(caller, F.main("Game", "The selected GameType, " + F.elem(game) + ", does not have any GameModes"));
				return;
			}
			List<GameMode> matchedGameModes = matchGameMode(gameType, gameModeStr, false);
			if (matchedGameModes.size() == 0)
			{
				UtilPlayer.message(caller, F.main("Game", "Could not find a GameMode matching " + F.elem(gameModeStr)));
				return;
			}
			if (matchedGameModes.size() > 1)
			{
				UtilPlayer.message(caller, F.main("Game", "Found multiple GameModes matching " + F.elem(gameModeStr)));
				UtilPlayer.sendMatches(caller, "Game", matchedGameModes, gameMode -> gameMode.getName().replaceAll(" ", ""));
				return;
			}
			selectedMode = matchedGameModes.get(0);
		}

		if (mapSourceStr != null)
		{
			List<GameType> matchedGameTypes = getSources(gameType, mapSourceStr, false);
			if (matchedGameTypes.size() == 0)
			{
				UtilPlayer.message(caller, F.main("Game", "Could not find a MapSource matching " + F.elem(mapSourceStr)));
				return;
			}
			if (matchedGameTypes.size() > 1)
			{
				UtilPlayer.message(caller, F.main("Game", "Found multiple MapSource matching " + F.elem(mapSourceStr)));
				UtilPlayer.sendMatches(caller, "Game", matchedGameTypes, Enum::name);
				return;
			}
			selectedSource = matchedGameTypes.get(0);
		}

		if (mapStr != null)
		{
			List<String> matchedMaps;

			// No particular source specified, we'll use all of them
			if (selectedSource == null)
			{
				List<GameType> mapTypes = Arrays.asList(Game.getWorldHostNames(gameType));
				matchedMaps = matchMaps(mapTypes, mapStr, false);
			}
			else
			{
				matchedMaps = matchMaps(Collections.singletonList(selectedSource), mapStr, false);
			}

			if (matchedMaps.size() == 0)
			{
				UtilPlayer.message(caller, F.main("Game", "Could not find a Map matching " + F.elem(mapStr)));
				return;
			}
			if (matchedMaps.size() > 1)
			{
				UtilPlayer.message(caller, F.main("Game", "Found multiple Maps matching " + F.elem(mapStr)));
				UtilPlayer.sendMatches(caller, "Game", matchedMaps);
				return;
			}

			selectedMap = matchedMaps.get(0);
		}

		GameCreationManager creationManager = Plugin.GetGameCreationManager();

		if (selectedMode != null)
		{
			UtilPlayer.message(caller, F.main("Game", "Game Mode preference set to " + F.elem(selectedMode.getName())));
			creationManager.ModePref = selectedMode;
		}
		if (selectedSource != null)
		{
			UtilPlayer.message(caller, F.main("Game", "Map Source preference set to " + F.elem(selectedSource.name())));
			creationManager.MapSource = selectedSource;
		}
		if (selectedMap != null)
		{
			UtilPlayer.message(caller, F.main("Game", "Map preference set to " + F.elem(selectedMap)));
			creationManager.MapPref = selectedMap;
		}

		if (Plugin.GetGame() == null)
		{
			creationManager.setNextGameType(gameType);
		}
		else
		{
			Plugin.GetGame().setGame(matches.get(0), caller, true);
		}

		if (UtilServer.isTestServer())
		{
			Plugin.GetGameList().clear();
			Plugin.GetGameList().add(matches.get(0));
		}
	}

	private List<GameType> matchGameType(String input, boolean isTabCompletion)
	{
		return matchGameType(input, GameType.values(), isTabCompletion);
	}

	private List<GameType> matchGameType(String input, GameType[] sources, boolean isTabCompletion)
	{
		input = input.toLowerCase();

		List<GameType> matches = new ArrayList<>();
		for (GameType type : sources)
		{
			String match = type.name().toLowerCase();
			if (match.equals(input))
			{
				return Collections.singletonList(type);
			}
			else if (isTabCompletion ? match.startsWith(input) : match.contains(input))
			{
				matches.add(type);
			}
		}

		if (matches.isEmpty())
		{
			for (GameType type : sources)
			{
				String match = type.getDisplay().getName().replaceAll(" ", "").toLowerCase();
				if (match.equals(input))
				{
					return Collections.singletonList(type);
				}
				else if (isTabCompletion ? match.startsWith(input) : match.contains(input))
				{
					matches.add(type);
				}
			}
		}

		return matches;
	}

	private List<GameMode> matchGameMode(GameType type, String input, boolean isTabCompletion)
	{
		input = input.toLowerCase();

		if (!type.hasGameModes()) return Collections.emptyList();

		List<GameMode> matches = new ArrayList<>();

		for (GameMode gameMode : type.getGameModes())
		{
			String match = gameMode.getName().replaceAll(" ", "").toLowerCase();
			if (match.equals(input))
			{
				return Collections.singletonList(gameMode);
			}
			else if (isTabCompletion ? match.startsWith(input) : match.contains(input))
			{
				matches.add(gameMode);
			}
		}

		return matches;
	}

	private List<GameType> getSources(GameType type, String input, boolean isTabCompletion)
	{
		return matchGameType(input, Game.getWorldHostNames(type), isTabCompletion);
	}

	private List<String> matchMaps(List<GameType> source, String input, boolean isTabCompletion)
	{
		input = input.toLowerCase();

		List<String> allMapNames = source.stream().flatMap(type -> Plugin.LoadFiles(type.getName()).stream()).collect(Collectors.toList());

		List<String> matches = new ArrayList<>();

		for (String map : allMapNames)
		{
			String match = map.replaceAll(" ", "").toLowerCase();
			if (match.equals(input))
			{
				return Collections.singletonList(map.replaceAll(" ", ""));
			}
			else if (isTabCompletion ? match.startsWith(input) : match.contains(input))
			{
				matches.add(map.replaceAll(" ", ""));
			}
		}

		return matches;
	}

	private boolean isValid(String[] args)
	{
		boolean hasGameSource = false;
		boolean hasMap = false;
		for (String string : args)
		{
			if (string.startsWith(MODE_PREFIX))
			{
				if (hasGameSource || hasMap)
					return false;
			}
			else if (string.startsWith(SOURCE_PREFIX))
			{
				if (hasMap)
					return false;
				hasGameSource = true;
			}
			else if (string.startsWith(MAP_PREFIX))
			{
				hasMap = true;
			}
		}

		return true;
	}

	private String findGameMode(String[] args)
	{
		for (String string : args)
		{
			if (string.startsWith(MODE_PREFIX))
			{
				return string.substring(MODE_PREFIX.length());
			}
		}
		return null;
	}

	private String findGameSource(String[] args)
	{
		for (String string : args)
		{
			if (string.startsWith(SOURCE_PREFIX))
			{
				return string.substring(SOURCE_PREFIX.length());
			}
		}
		return null;
	}

	private String findMap(String[] args)
	{
		for (String string : args)
		{
			if (string.startsWith(MAP_PREFIX))
			{
				return string.substring(MAP_PREFIX.length());
			}
		}
		return null;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String commandLabel, String[] args)
	{
		if (!(sender instanceof Player) || !Plugin.canPlayerUseGameCmd((Player) sender))
		{
			return null;
		}

		if (args.length == 1)
		{
			return matchGameType(args[0], true).stream().map(Enum::name).collect(Collectors.toList());
		}
		else
		{
			if (isValid(args))
			{
				List<GameType> matches = matchGameType(args[0], true);

				if (matches.size() == 1)
				{
					GameType gameType = matches.get(0);

					String gameModeStr = findGameMode(args);
					String gameSourceStr = findGameSource(args);
					String mapStr = findMap(args);

					String lastArg = args[args.length - 1];
					if (lastArg.startsWith(MODE_PREFIX))
					{
						if (gameType.hasGameModes())
						{
							return matchGameMode(gameType, gameModeStr, true).stream().map(mode -> MODE_PREFIX + mode.getName().replaceAll(" ", "")).collect(Collectors.toList());
						}
					}
					else if (lastArg.startsWith(SOURCE_PREFIX))
					{
						return getSources(gameType, gameSourceStr, true).stream().map(type -> SOURCE_PREFIX + type.name().replaceAll(" ", "")).collect(Collectors.toList());
					}
					else if (lastArg.startsWith(MAP_PREFIX))
					{
						// No particular source specified, we'll use all of them
						if (gameSourceStr == null)
						{
							List<GameType> mapTypes = Arrays.asList(Game.getWorldHostNames(gameType));
							return matchMaps(mapTypes, mapStr, true).stream().map(str -> MAP_PREFIX + str).collect(Collectors.toList());
						}

						List<GameType> sources = getSources(gameType, gameSourceStr, true);

						// If a source has been provided, it must be valid
						if (sources.size() != 1)
							return null;

						return matchMaps(sources, mapStr, true).stream().map(str -> MAP_PREFIX + str).collect(Collectors.toList());
					}
				}
			}
		}

		return null;
	}
}