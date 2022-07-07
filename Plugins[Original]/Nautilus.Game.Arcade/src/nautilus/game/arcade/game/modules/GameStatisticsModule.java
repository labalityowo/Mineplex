package nautilus.game.arcade.game.modules;

import mineplex.core.Managers;
import mineplex.core.common.util.UtilServer;
import mineplex.core.stats.event.StatChangeEvent;
import mineplex.core.stats.game.GameStatisticsManager;
import mineplex.core.stats.game.GameStats;
import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.Game.GameState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GameStatisticsModule extends Module
{

	private static final GameStatisticsManager STATS_MANAGER;

	static
	{
		STATS_MANAGER = Managers.require(GameStatisticsManager.class);
	}

	private GameStats _currentGame;
	private final List<String> _statsToListen;
	private final Map<UUID, Integer> _playerId;

	public GameStatisticsModule()
	{
		_statsToListen = new ArrayList<>();
		_playerId = new HashMap<>();
	}

	public void addStatListener(String stat)
	{
		_statsToListen.add(stat);
	}

	@Override
	protected void setup()
	{
		_currentGame = new GameStats(-1, UtilServer.getRegion(), getGame().GetType().getDisplay());
	}

	@Override
	public void cleanup()
	{
		if (getGame().getArcadeManager().IsRewardStats() && _currentGame.isValid())
		{
			STATS_MANAGER.saveGameStats(_currentGame);
		}
	}

	@EventHandler
	public void live(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Live)
		{
			return;
		}

		int id = 0;
		for (Player player : getGame().GetPlayers(true))
		{
			_playerId.put(player.getUniqueId(), ++id);
		}

		STATS_MANAGER.getMapId(mapId -> _currentGame.setMapId(mapId), getGame().GetType().getDisplay(), getGame().WorldData.MapName);
		_currentGame.setStartTime(System.currentTimeMillis());
	}

	@EventHandler
	public void end(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.End)
		{
			return;
		}

		_currentGame.setEndTime(System.currentTimeMillis());
	}

	@EventHandler
	public void statChange(StatChangeEvent event)
	{
		Player player = event.getPlayer();
		String stat = event.getStatName();
		long change = event.getValueAfter() - event.getValueBefore();

		if (_statsToListen.contains(getGame().GetName() + "." + stat) && _playerId.containsKey(player.getUniqueId()))
		{
			int playerId = _playerId.get(player.getUniqueId());
			_currentGame.getStats().putIfAbsent(playerId, new HashMap<>());
			_currentGame.getStats().get(playerId).put(stat, change);
		}
	}
}
