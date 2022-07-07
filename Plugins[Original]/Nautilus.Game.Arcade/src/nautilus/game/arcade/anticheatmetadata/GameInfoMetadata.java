package nautilus.game.arcade.anticheatmetadata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import mineplex.core.antihack.logging.AnticheatMetadata;
import mineplex.core.common.util.UtilServer;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.kit.LinearUpgradeKit;
import nautilus.game.arcade.managers.GameHostManager;
import static mineplex.core.Managers.require;

public class GameInfoMetadata extends AnticheatMetadata
{
	private static final String KEY_GAME_INFO = "game-info";
	private static final String KEY_GAME_MAP = "map";
	private static final String KEY_GAME_TYPE = "type";
	private static final String KEY_GAME_MODE = "mode";
	private static final String KEY_CURRENT_STATE = "current-state";
	private static final String KEY_STATE_START_TIME = "current-state-start-time";
	private static final String KEY_JOIN_GAME_TIME = "join-game-time-ms";

	private static final String KEY_STATE_TIMES = "state-times";

	private static final String KEY_KIT_INFO = "kit-info";
	private static final String KEY_KIT_NAME = "name";
	private static final String KEY_KIT_LEVEL = "level";

	private static final String KEY_MPS = "mps";
	private static final String KEY_OWNER = "owner";

	private static final String KEY_STATS = "stats";

	private static final String KEY_WINNER = "winner";

	private final Map<UUID, JsonArray> _allGames = new HashMap<>();
	private final Map<UUID, JsonObject> _currentGame = new HashMap<>();

	private final ArcadeManager _arcadeManager = require(ArcadeManager.class);

	@Override
	public String getId()
	{
		return "game-info";
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event)
	{
		_allGames.put(event.getPlayer().getUniqueId(), new JsonArray());

		JsonObject currentGame = buildCurrentGame();

		if (currentGame != null)
		{
			_currentGame.put(event.getPlayer().getUniqueId(), currentGame);
		}
	}

	private JsonObject buildCurrentGame()
	{
		Game game = _arcadeManager.GetGame();

		if (game == null)
			return null;

		JsonObject currentGame = new JsonObject();

		JsonObject gameInfo = new JsonObject();
		gameInfo.addProperty(KEY_GAME_MAP, game.WorldData.File);
		gameInfo.addProperty(KEY_GAME_TYPE, game.GetName());
		gameInfo.addProperty(KEY_GAME_MODE, game.GetMode());
		gameInfo.addProperty(KEY_CURRENT_STATE, game.GetState().name());
		gameInfo.addProperty(KEY_STATE_START_TIME, game.GetStateTime());
		gameInfo.addProperty(KEY_JOIN_GAME_TIME, System.currentTimeMillis());

		if (_arcadeManager.GetGameHostManager() != null && _arcadeManager.GetGameHostManager().isPrivateServer())
		{
			GameHostManager gameHostManager = _arcadeManager.GetGameHostManager();

			JsonObject mpsInfo = new JsonObject();
			mpsInfo.addProperty(KEY_OWNER, _arcadeManager.GetHost());

			currentGame.add(KEY_MPS, mpsInfo);
		}

		currentGame.add(KEY_GAME_INFO, gameInfo);

		JsonObject stateStartTimes = new JsonObject();
		stateStartTimes.addProperty(game.GetState().name(), game.GetStateTime());

		currentGame.add(KEY_STATE_TIMES, stateStartTimes);

		return currentGame;
	}

	@EventHandler
	public void onStateChange(GameStateChangeEvent event)
	{
		if (event.GetState() == Game.GameState.Recruit)
		{
			for (Player player : UtilServer.getPlayersCollection())
			{
				if (!_currentGame.containsKey(player.getUniqueId()))
				{
					_currentGame.put(player.getUniqueId(), buildCurrentGame());
				}
			}
		}

		if (event.GetState() == Game.GameState.Live)
		{
			_currentGame.forEach((id, obj) ->
			{
				Player player = Bukkit.getPlayer(id);
				if (player != null)
				{
					Kit kit = event.GetGame().GetKit(player);
					if (kit != null)
					{
						JsonObject kitInfo = new JsonObject();
						kitInfo.addProperty(KEY_KIT_NAME, kit.GetName());

						if (kit instanceof LinearUpgradeKit)
						{
							LinearUpgradeKit pk = (LinearUpgradeKit) kit;
							kitInfo.addProperty(KEY_KIT_LEVEL, pk.getLevel(player));
						}

						obj.add(KEY_KIT_INFO, kitInfo);
					}
				}
			});

		}

		_currentGame.values().forEach(obj ->
		{
			obj.get(KEY_STATE_TIMES).getAsJsonObject().addProperty(event.GetState().name(), System.currentTimeMillis());
		});

		if (event.GetState() == Game.GameState.Dead)
		{
			new ArrayList<>(_currentGame.keySet()).forEach(ent -> archivePlayer(event.GetGame(), ent));
		}
	}

	private void archivePlayer(Game game, UUID uuid)
	{
		JsonObject gameObj = _currentGame.remove(uuid);

		if (gameObj == null)
			return;

		_allGames.get(uuid).add(gameObj);

		if (game == null)
			return;

		Player player = Bukkit.getPlayer(uuid);

		if (player == null)
			return;

		Map<String, Integer> stats = game.GetStats().get(player);

		if (stats != null)
		{
			JsonObject statsObject = new JsonObject();
			stats.forEach(statsObject::addProperty);

			gameObj.add(KEY_STATS, statsObject);
		}

		gameObj.addProperty(KEY_WINNER, game.Winner);
	}

	@Override
	public JsonElement build(UUID player)
	{
		archivePlayer(_arcadeManager.GetGame(), player);

		return _allGames.get(player);
	}

	@Override
	public void remove(UUID player)
	{
		_allGames.remove(player);
		_currentGame.remove(player);
	}
}
