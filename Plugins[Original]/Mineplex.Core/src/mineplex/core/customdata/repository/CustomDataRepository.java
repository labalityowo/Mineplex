package mineplex.core.customdata.repository;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import mineplex.core.account.CoreClientManager;
import mineplex.core.customdata.CustomDataManager;
import mineplex.serverdata.database.DBPool;
import mineplex.serverdata.database.RepositoryBase;
import mineplex.serverdata.database.column.ColumnInt;
import mineplex.serverdata.database.column.ColumnVarChar;

public class CustomDataRepository extends RepositoryBase
{

	private static final String SELECT_KEYS = "SELECT id, name FROM customData;";
	private static final String INSERT_KEY = "INSERT INTO customData (name) VALUES (?);";
	private static final String UPDATE_DATA = "UPDATE accountCustomData SET data = ? WHERE accountId = ? AND customDataId = ?;";
	private static final String INSERT_DATA = "INSERT INTO accountCustomData (accountId, customDataId, data) VALUES (?, ?, ?);";

	private final CoreClientManager _clientManager;
	private final CustomDataManager _customDataManager;

	private final Map<String, Integer> _dataKeys;

	public CustomDataRepository(CoreClientManager clientManager, CustomDataManager customDataManager)
	{
		super(DBPool.getAccount());

		_clientManager = clientManager;
		_customDataManager = customDataManager;

		_dataKeys = new HashMap<>();
	}

	@Override
	protected void initialize()
	{
		downloadDataKeys();
	}

	private void downloadDataKeys()
	{
		executeQuery(SELECT_KEYS, resultSet ->
		{
			_dataKeys.clear();

			while (resultSet.next())
			{
				_dataKeys.put(resultSet.getString("name"), resultSet.getInt("id"));
			}
		});
	}

	public void saveData(UUID uuid, int accountId)
	{
		Map<String, Integer> data = _customDataManager.Get(uuid);

		for (String dataKey : data.keySet())
		{
			int dataId = _dataKeys.get(dataKey);
			int dataVal = data.get(dataKey);

			if (executeUpdate(
					UPDATE_DATA,
					new ColumnInt("data", dataVal),
					new ColumnInt("account", accountId),
					new ColumnInt("customData", dataId)) < 1)
			{
				// Not already in the DB
				executeUpdate(
						INSERT_DATA,
						new ColumnInt("account", accountId),
						new ColumnInt("customData", dataId),
						new ColumnInt("data", dataVal)
				);
			}
		}
	}

	public void registerKey(String key)
	{
		if (_dataKeys.containsKey(key))
		{
			return;
		}

		executeUpdate(INSERT_KEY, new ColumnVarChar("name", 100, key));
		downloadDataKeys();
	}

	public String getKey(int id)
	{
		for (Map.Entry<String, Integer> cur : _dataKeys.entrySet())
		{
			if (cur.getValue() == id)
			{
				return cur.getKey();
			}
		}

		return null;
	}

	public CoreClientManager getClientManager()
	{
		return _clientManager;
	}
}
