package mineplex.core.customdata;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;

import mineplex.core.MiniDbClientPlugin;
import mineplex.core.ReflectivelyCreateMiniPlugin;
import mineplex.core.customdata.repository.CustomDataRepository;

@ReflectivelyCreateMiniPlugin
public class CustomDataManager extends MiniDbClientPlugin<Map<String, Integer>>
{
	private final CustomDataRepository _repository;

	private CustomDataManager()
	{
		super("Custom Data");

		_repository = new CustomDataRepository(ClientManager, this);
	}

	public CustomDataRepository getRepository()
	{
		return _repository;
	}

	@Override
	public void processLoginResultSet(String playerName, UUID uuid, int accountId, ResultSet resultSet) throws SQLException
	{
		Map<String, Integer> data = new HashMap<>();

		while (resultSet.next())
		{
			data.put(_repository.getKey(resultSet.getInt("customDataId")), resultSet.getInt("data"));
		}

		Set(uuid, data);
	}

	@Override
	public String getQuery(int accountId, String uuid, String name)
	{
		return "SELECT accountId, customDataId, data FROM accountCustomData INNER JOIN customData ON customData.id = accountCustomData.customDataId WHERE accountId = " + accountId + ";";
	}

	protected Map<String, Integer> addPlayer(UUID uuid)
	{
		return new HashMap<>();
	}

	public void saveData(Player player)
	{
		final int accountId = getClientManager().getAccountId(player);

		if (accountId == -1)
		{
			return;
		}

		runAsync(() -> _repository.saveData(player.getUniqueId(), accountId));
	}

	public int getData(Player player, String key)
	{
		return Get(player).getOrDefault(key, -1);
	}
}
