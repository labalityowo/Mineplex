package mineplex.core.stats.game;

import mineplex.core.MiniPlugin;
import mineplex.core.ReflectivelyCreateMiniPlugin;
import mineplex.core.common.timing.TimingManager;
import mineplex.core.game.GameDisplay;
import mineplex.core.stats.StatsManager;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@ReflectivelyCreateMiniPlugin
public class GameStatisticsManager extends MiniPlugin
{

	private final StatsManager _statsManager;

	private final GameStatisticsRepository _repository;

	private final Map<String, Integer> _gameMaps;

	private GameStatisticsManager()
	{
		super("Game Statistics");

		_statsManager = require(StatsManager.class);
		_repository = new GameStatisticsRepository(_statsManager);
		_gameMaps = Collections.synchronizedMap(new HashMap<>());
	}

	public void getMapId(Consumer<Integer> callback, GameDisplay gameType, String mapName)
	{
		if (_gameMaps.containsKey(mapName))
		{
			if (callback != null)
			{
				callback.accept(_gameMaps.get(mapName));
			}
		}
		else
		{
			runAsync(() ->
					_repository.getMapId(mapId ->
					{
						_gameMaps.put(mapName, mapId);

						if (callback != null)
						{
							runSync(() -> callback.accept(mapId));
						}
					}, gameType.getGameId(), mapName));
		}
	}

	public void saveGameStats(GameStats gameStats)
	{
		runAsync(() ->
		{
			TimingManager.start("Save Game Stats");
			_repository.saveGame(gameStats);
			TimingManager.stop("Save Game Stats");
		});
	}

}
