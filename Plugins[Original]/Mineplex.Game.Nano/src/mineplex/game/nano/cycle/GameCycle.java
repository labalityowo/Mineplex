package mineplex.game.nano.cycle;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import mineplex.core.ReflectivelyCreateMiniPlugin;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.weight.WeightSet;
import mineplex.core.game.nano.NanoDisplay;
import mineplex.game.nano.GameManager;
import mineplex.game.nano.NanoManager;
import mineplex.game.nano.game.Game;
import mineplex.game.nano.game.Game.GameState;
import mineplex.game.nano.game.GameType;
import mineplex.game.nano.game.event.GameStateChangeEvent;

@ReflectivelyCreateMiniPlugin
public class GameCycle extends GameManager
{

	private static final int RECENT_GAMES_SIZE = 5;

	// Preferences
	private GameType _gamePreference;
	private String _mapPreference;

	// Recent Games
	private final List<GameType> _recentGames;

	// Testing mode
	private boolean _testingMode;

	private GameCycle()
	{
		super("Game Cycle");

		_recentGames = new ArrayList<>(RECENT_GAMES_SIZE);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void playerJoin(PlayerJoinEvent event)
	{
		if (_manager.canStartGame())
		{
			runSyncLater(() -> checkForDeadGame(false), 10);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void gameDeath(GameStateChangeEvent event)
	{
		if (event.getState() != GameState.Dead)
		{
			return;
		}

		checkForDeadGame(false);
	}

	public void checkForDeadGame(boolean force)
	{
		Game game = _manager.getGame();

		// No Game. Make a new one
		if (game == null)
		{
			if (force || _manager.canStartGame())
			{
				createGame();
			}
		}
		// Dead Game. Kill it
		else if (game.getState() == GameState.Dead)
		{
			game.getLifetime().end();
			_manager.setGame(null);

			// Make the next game.
			if (force || _manager.canStartGame())
			{
				createGame();
			}
			// Cannot start so move all players to lobby
			else
			{
				_manager.getLobbyManager().ensureInLobby();
			}

			_manager.runSyncTimer(new BukkitRunnable()
			{
				int attempts = 0;

				@Override
				public void run()
				{
					if (killDeadGame(game, ++attempts))
					{
						log("Successfully killed " + game.getClass().getSimpleName());
						cancel();
					}
				}
			}, 40, 10);
		}
	}

	private boolean killDeadGame(Game game, int attempts)
	{
		List<Player> players = game.getMineplexWorld().getWorld().getPlayers();
		boolean tooLong = attempts > 5;

		if (tooLong)
		{
			log("Took too long, " + players.toString() + " are still in world. Next game failed to load?");

			players.forEach(player ->
			{
				player.remove();
				player.kickPlayer("Dead World");
			});
		}

		if (players.isEmpty() || tooLong)
		{
			log(game.getClass().getSimpleName() + " world is empty, killing...");
			game.getGameWorld().unloadWorld();
			return true;
		}

		log("Unable to kill " + game.getClass().getSimpleName() + ", players are still in world. Attempt: " + attempts);
		return false;
	}

	private void createGame()
	{
		String error = createGameError();

		if (error == null)
		{
			log("Successfully created " + _manager.getGame().getGameType().getName());
		}
		else
		{
			log("Failed to create game! Error: {" + error + "}, Game Preference : {" + _gamePreference + "}, Map Preference : {" + _mapPreference + "}!");
		}
	}

	private String createGameError()
	{
		GameType gameType = getNextGameType();

		if (gameType == null)
		{
			return "getNextGameType was null";
		}

		File map = getNextMap(gameType);

		if (map == null)
		{
			return "getNextMap was null";
		}

		_gamePreference = null;
		_mapPreference = null;

		if (_recentGames.size() == RECENT_GAMES_SIZE)
		{
			_recentGames.remove(_recentGames.size() - 1);
		}

		_recentGames.add(0, gameType);

		try
		{
			Game game = gameType.getGameClass().getConstructor(NanoManager.class).newInstance(_manager);

			_manager.setGame(game);
			game.setupGameWorld(map);
		}
		catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e)
		{
			e.printStackTrace();
			return e.getMessage();
		}

		return null;
	}

	private GameType getNextGameType()
	{
		if (_gamePreference != null)
		{
			log("Setting by Game Preference : " + _gamePreference);
			return _gamePreference;
		}

		// Testing mode
		if (_testingMode && !_recentGames.isEmpty())
		{
			log("Setting by last game");
			return _recentGames.get(0);
		}

		// The games mapped to the number of people that have them favourited
		Map<NanoDisplay, Integer> favourites = _manager.getFavourite().getFavourites();
		WeightSet<GameType> weightedGames = new WeightSet<>();

		// Out of all games
		Arrays.stream(GameType.values())
				// Remove recent games and ones which no one has sent as their favourite
				.filter(game -> !_recentGames.contains(game))
				// Add the games to a weighted set
				// Add 1 to all weights to prevent it throwing an error when favourites are empty
				.forEach(gameType -> weightedGames.add(1 + favourites.getOrDefault(gameType.getDisplay(), 0), gameType));

		log("Recent Games : " + _recentGames);

		// Get a random game, a more popular game has a higher chance of coming up
		GameType gameType = weightedGames.generateRandom();

		log("Setting by Favourite : " + gameType);

		return gameType;
	}

	private File getNextMap(GameType gameType)
	{
		File directory = new File(gameType.getMapDirectory());

		if (!directory.exists() && !directory.mkdirs())
		{
			log("Failed to create non-existent dirs");
			return null;
		}

		File[] mapZips = directory.listFiles((dir, name) -> name.endsWith(".zip"));

		if (mapZips == null)
		{
			return null;
		}

		log("Found maps: " + Arrays.stream(mapZips)
				.map(File::getName)
				.collect(Collectors.joining(",")));

		File file;

		if (_mapPreference == null)
		{
			log("Setting by Map Random");
			file = UtilMath.randomElement(mapZips);
		}
		else
		{
			log("Setting by Map Preference : " + _mapPreference);
			file = new File(directory + File.separator + _mapPreference + ".zip");

			if (!file.exists())
			{
				log("Map Preference : " + _mapPreference + " did not exist!");
				_mapPreference = null;
				return getNextMap(gameType);
			}
		}

		return file != null && file.exists() ? file : null;
	}

	public void setNextGameMap(GameType gameType, String map)
	{
		_gamePreference = gameType;
		_mapPreference = map;
	}

	public void setTestingMode(boolean testingMode)
	{
		_testingMode = testingMode;
	}

	public boolean isTestingMode()
	{
		return _testingMode;
	}
}
