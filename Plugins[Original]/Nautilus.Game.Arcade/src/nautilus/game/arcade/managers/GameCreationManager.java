package nautilus.game.arcade.managers;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import mineplex.core.common.Pair;
import mineplex.core.common.timing.TimingManager;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.combat.CombatManager.AttackReason;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.GameMode;
import nautilus.game.arcade.GameType;
import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.game.Game.GameState;
import nautilus.game.arcade.managers.voting.Vote;
import nautilus.game.arcade.managers.voting.VotingManager;
import nautilus.game.arcade.managers.voting.types.GameVote;
import nautilus.game.arcade.managers.voting.types.MapVote;

public class GameCreationManager implements Listener
{

	private static final int MAX_ATTEMPTS = 50;

	final ArcadeManager Manager;
	private final VotingManager _votingManager;

	private final List<Game> _ended = new ArrayList<>();

	private GameType _nextGame = null;

	private String _lastMode = null;
	private final List<GameType> _lastGames = new ArrayList<>();
	private Map<GameType, List<String>> _maps;

	public String MapPref = null;
	public GameType MapSource = null;
	public GameMode ModePref = null;

	public GameCreationManager(ArcadeManager manager)
	{
		Manager = manager;
		_votingManager = new VotingManager(manager);

		Manager.registerEvents(this);
	}

	@EventHandler
	public void nextGame(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST || Manager.GetGameList().isEmpty())
		{
			return;
		}

		while (_lastGames.size() > Manager.GetGameList().size() - 1)
		{
			_lastGames.remove(_lastGames.size() - 1);
		}

		if (Manager.GetGame() == null && _ended.isEmpty())
		{
			createGame();
		}

		checkMapVoting();

		//Archive Game
		if (Manager.GetGame() != null && Manager.GetGame().GetState() == GameState.Dead)
		{
			Manager.GetGame().disable();
			_votingManager.deactivate();

			//Schedule Cleanup
			_ended.add(Manager.GetGame());

			//Lobby Display
			Manager.GetLobby().displayLast(Manager.GetGame());

			Manager.SetGame(null);
		}

		//Clean Archived Games
		Iterator<Game> gameIterator = _ended.iterator();

		while (gameIterator.hasNext())
		{
			Game game = gameIterator.next();

			TimingManager.start("GameCreationManager - Attempting Removal - " + game.GetName());

			//Cleaned
			if (game.WorldData == null || game.WorldData.World == null)
			{
				gameIterator.remove();
			}
			else
			{
				boolean removedPlayers = false;

				if (UtilTime.elapsed(game.GetStateTime(), 20000))
				{
					TimingManager.start("GameCreationManager - Kick Players - " + game.GetName());

					for (Player player : game.WorldData.World.getPlayers())
					{
						System.out.println("Kicking [" + player.getName() + "] with Validity [" + player.isValid() + "] with Online [" + player.isOnline() + "]");

						player.remove();
						player.kickPlayer("Dead World");
					}

					removedPlayers = true;

					TimingManager.stop("GameCreationManager - Kick Players - " + game.GetName());
				}

				//Clean
				if (removedPlayers || game.WorldData.World.getPlayers().isEmpty())
				{
					if (game.WorldData.World.getPlayers().isEmpty())
					{
						System.out.println("World Player Count [" + game.WorldData.World.getPlayers().size() + "]");
					}

					TimingManager.start("GameCreationManager - Uninit World - " + game.GetName());

					game.WorldData.Uninitialize();
					game.WorldData = null;
					gameIterator.remove();

					TimingManager.stop("GameCreationManager - Uninit World - " + game.GetName());
				}
			}

			TimingManager.stop("GameCreationManager - Attempting Removal - " + game.GetName());
		}
	}

	private void createGame()
	{
		GameType gameType = null;

		if (_votingManager.getFinishedVote() != null)
		{
			Vote vote = _votingManager.getFinishedVote();

			if (vote instanceof GameVote)
			{
				GameVote gameVote = ((GameVote) _votingManager.getFinishedVote());

				gameType = gameVote.getWinner();
				MapPref = gameVote.getWinningMapVote().getWinner().getName();
			}
			else if (vote instanceof MapVote)
			{
				MapPref = ((MapVote) vote).getWinner().getName();
			}

			_votingManager.updateMapRatings();
			_votingManager.deactivate();
		}
		else if (_votingManager.isVoteBlocking())
		{
			return;
		}
		else if (!Manager.GetServerConfig().MapVoting && !Manager.GetServerConfig().GameVoting || _votingManager.canStartVote())
		{
			System.out.println("Next Game Preference: " + _nextGame);
			System.out.println("Map Preference: " + MapPref);
			System.out.println("Gamemode Preference: " + ModePref);
		}

		// /game set or map vote
		if (_nextGame != null)
		{
			gameType = _nextGame;
		}

		//Pick Game
		if (gameType == null)
		{
			// If game voting is enabled
			if (Manager.GetServerConfig().GameVoting)
			{
				if (!_votingManager.canStartVote())
				{
					return;
				}

				List<GameType> typesToVote = new ArrayList<>(Manager.GetGameList());

				// Don't let the game that was just played appear
				if (!_lastGames.isEmpty())
				{
					typesToVote.remove(_lastGames.get(0));
				}

				// While there are more than 3 games, remove a random one
				while (typesToVote.size() > VotingManager.GAMES_TO_VOTE_ON)
				{
					typesToVote.remove(UtilMath.r(typesToVote.size()));
				}

				Map<GameType, List<String>> mapsToVote = new HashMap<>();

				typesToVote.forEach(votingGameType -> mapsToVote.put(votingGameType, getMapsToVote(votingGameType)));

				_votingManager.callVote(new GameVote(Manager, typesToVote, mapsToVote));
				return;
			}
			else
			{
				for (int i = 0; i < MAX_ATTEMPTS; i++)
				{
					gameType = UtilAlg.Random(Manager.GetGameList());
					ModePref = randomGameMode(Manager.GetServerConfig().GameModeList, gameType);

					if (!_lastGames.contains(gameType))
					{
						break;
					}
				}
			}
		}

		Class<? extends Game> gameClass = gameType.getGameClass();

		_nextGame = null;

		//Reset Changes
		Manager.GetCreature().SetDisableCustomDrops(false);
		Manager.GetDamage().resetConfiguration();
		Manager.GetExplosion().resetConfiguration();
		Manager.getCosmeticManager().setHideParticles(false);
		Manager.getCosmeticManager().getGadgetManager().setShowWeaponNames(false);
		Manager.GetDamage().GetCombatManager().setUseWeaponName(AttackReason.CustomWeaponName);
		ItemStackFactory.Instance.SetUseCustomNames(false);

		//Champions
		Manager.toggleChampionsModules(gameType);

		_lastGames.add(0, gameType);

		// GameModes
		if (gameType.hasGameModes())
		{
			List<String> gameModes = Manager.GetServerConfig().GameModeList;
			GameMode mode = null;

			// First check if there is an overriding preference
			if (ModePref != null)
			{
				mode = getModeByName(gameType.getGameModes(), ModePref.getName());
			}
			else if (!gameModes.isEmpty())
			{
				mode = randomGameMode(gameModes, gameType);
			}

			if (mode != null)
			{
				gameClass = mode.getGameClass();
				_lastMode = mode.getName();
			}
		}

		ModePref = null;

		// No map voting, load the maps now
		boolean loadMapsNow = !Manager.GetServerConfig().MapVoting || MapPref != null;

		try
		{
			if (loadMapsNow)
			{
				loadMaps(gameType);
			}

			// Create Game instance
			Game game = gameClass.getConstructor(ArcadeManager.class).newInstance(Manager);

			if (loadMapsNow)
			{
				game.WorldData.Initialize();
			}

			Manager.SetGame(game);
		}
		catch (NoSuchMethodException ex)
		{
			System.err.println("Is the constructor for " + gameType.getName() + " using only one argument?");
			ex.printStackTrace();
			return;
		}
		catch (InvocationTargetException ex)
		{
			ex.getCause().printStackTrace();
			return;
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			return;
		}

		if (Manager.GetGame() == null)
		{
			return;
		}

		TimingManager.start("registerEvents");
		Manager.GetGame().getLifetime().start(GameState.PreLoad);
		// Need to manually call the the event
		UtilServer.CallEvent(new GameStateChangeEvent(Manager.GetGame(), GameState.PreLoad));
		TimingManager.stop("registerEvents");

		Manager.GetLobby().displayNext(Manager.GetGame());

		if (loadMapsNow)
		{
			Manager.GetGame().SetState(GameState.Loading);
		}
	}

	private void checkMapVoting()
	{
		Game game = Manager.GetGame();

		if (game == null || game.GetState() != GameState.PreLoad || MapPref != null || !Manager.GetServerConfig().MapVoting || Manager.GetServerConfig().GameVoting)
		{
			return;
		}

		if (_votingManager.getFinishedVote() != null)
		{
			MapPref = _votingManager.getFinishedVote().getWinner().getName();
			_votingManager.deactivate();
			game.SetState(GameState.Loading);

			// Load maps for the running game
			loadMaps(game.GetType());

			// Load the world data
			game.WorldData.Initialize();
		}
		else if (!_votingManager.isVoteInProgress() && _votingManager.canStartVote())
		{
			_votingManager.callVote(new MapVote(Manager, game.GetType(), getMapsToVote(game.GetType())));
		}
	}

	private void loadMaps(GameType gameType)
	{
		_maps = new HashMap<>();

		// Map
		for (GameType type : Game.getWorldHostNames(gameType))
		{
			_maps.put(type, Manager.LoadFiles(type.getName()));
		}

		if (MapPref != null)
		{
			MapPref = MapPref.replace(" ", "");

			System.out.println("Map Preference: " + MapPref + " in " + MapSource);

			Map<GameType, List<String>> matches = new HashMap<>();

			for (Entry<GameType, List<String>> entry : _maps.entrySet())
			{
				GameType entryType = entry.getKey();
				List<String> maps = entry.getValue();

				if (MapSource != null && entryType != MapSource)
				{
					continue;
				}

				List<String> matchList = new ArrayList<>();

				maps.forEach(map ->
				{
					if (map.replace(" ", "").toLowerCase().contains(MapPref.toLowerCase()))
					{
						matchList.add(map);
						System.out.print("Map Preference Found: " + map);
					}
				});

				if (!matchList.isEmpty())
				{
					matches.put(entryType, matchList);
				}
			}

			if (!matches.isEmpty())
			{
				_maps = matches;
			}

			MapPref = null;
			MapSource = null;
		}
		else
		{
			System.out.println("Map Preference: None");
		}
	}

	private List<String> getMapsToVote(GameType gameType)
	{
		// Load the maps for this game type
		loadMaps(gameType);

		// Merge maps from all sources
		List<String> mapsToVote = _maps.values().stream()
				.flatMap(Collection::stream)
				.collect(Collectors.toList());

		// While there are more than 5 maps, remove a random one
		while (mapsToVote.size() > VotingManager.MAPS_TO_VOTE_ON)
		{
			mapsToVote.remove(UtilMath.r(mapsToVote.size()));
		}

		return mapsToVote;
	}

	public Pair<GameType, String> getMapFile()
	{
		GameType gameType = null;

		int gameTypeIndex = UtilMath.r(_maps.size());
		int index = 0;

		for (GameType mapType : _maps.keySet())
		{
			if (index++ == gameTypeIndex)
			{
				gameType = mapType;
				break;
			}
		}

		return Pair.create(gameType, UtilAlg.Random(_maps.get(gameType)));
	}

	private GameMode randomGameMode(List<String> modes, GameType type)
	{
		List<GameMode> possible = new ArrayList<>(Arrays.asList(type.getGameModes()));
		possible.removeIf(gameMode -> !modes.contains(gameMode.getName()));

		// If there's none or one mode for this gametype just use that one.
		if (possible.isEmpty())
		{
			return null;
		}
		else if (possible.size() == 1)
		{
			return possible.get(0);
		}

		if (_lastMode != null)
		{
			possible.removeIf(gameMode -> _lastMode.equals(gameMode.getName()));
		}

		return UtilAlg.Random(possible);
	}

	private GameMode getModeByName(GameMode[] gameModes, String gameModeName)
	{
		for (GameMode modes : gameModes)
		{
			if (modes.getName().equals(gameModeName))
			{
				return modes;
			}
		}

		return null;
	}

	public void setNextGameType(GameType type)
	{
		_nextGame = type;
		_votingManager.deactivate();
	}

	public GameType getNextGameType()
	{
		return _nextGame;
	}

	public VotingManager getVotingManager()
	{
		return _votingManager;
	}
}
