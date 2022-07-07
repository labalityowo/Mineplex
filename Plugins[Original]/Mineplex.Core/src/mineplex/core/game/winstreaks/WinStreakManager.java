package mineplex.core.game.winstreaks;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;

import mineplex.core.MiniDbClientPlugin;
import mineplex.core.ReflectivelyCreateMiniPlugin;
import mineplex.core.game.GameDisplay;

@ReflectivelyCreateMiniPlugin
public class WinStreakManager extends MiniDbClientPlugin<Map<Integer, Integer>>
{

	private final WinStreakRepository _repository;

	private WinStreakManager()
	{
		super("Win Streaks");

		_repository = new WinStreakRepository();
	}

	@Override
	protected Map<Integer, Integer> addPlayer(UUID uuid)
	{
		return new HashMap<>();
	}

	@Override
	public void processLoginResultSet(String playerName, UUID uuid, int accountId, ResultSet resultSet) throws SQLException
	{
		Map<Integer, Integer> map = Get(uuid);

		while (resultSet.next())
		{
			map.put(resultSet.getInt("gameId"), resultSet.getInt("value"));
		}
	}

	@Override
	public String getQuery(int accountId, String uuid, String name)
	{
		return "SELECT gameId, value FROM accountWinStreak WHERE accountId=" + accountId + ";";
	}

	public int getCurrentStreak(Player player, GameDisplay game)
	{
		return Get(player).getOrDefault(game.getGameId(), 0);
	}

	public void incrementWinStreak(Player player, GameDisplay game)
	{
		Map<Integer, Integer> map = Get(player);
		Integer newValue = map.computeIfPresent(game.getGameId(), (gameId, value) -> ++value);

		if (newValue == null)
		{
			newValue = 1;
			map.put(game.getGameId(), newValue);
		}

		runAsync(() -> _repository.incrementWinStreak(ClientManager.getAccountId(player), game.getGameId()));
	}

	public void removeWinStreak(Player player, GameDisplay game)
	{
		Map<Integer, Integer> map = Get(player);
		Integer oldValue = map.remove(game.getGameId());

		if (oldValue != null)
		{
			runAsync(() -> _repository.removeWinStreak(ClientManager.getAccountId(player), game.getGameId()));
		}
	}
}
