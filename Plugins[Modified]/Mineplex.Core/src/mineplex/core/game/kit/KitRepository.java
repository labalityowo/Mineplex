package mineplex.core.game.kit;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import mineplex.core.account.CoreClientManager;
import mineplex.core.account.ILoginProcessor;
import mineplex.core.common.util.EnclosedObject;
import mineplex.core.game.MineplexGameManager;
import mineplex.core.game.kit.upgrade.KitStat;
import mineplex.serverdata.database.DBPool;
import mineplex.serverdata.database.RepositoryBase;
import mineplex.serverdata.database.column.ColumnVarChar;

public class KitRepository extends RepositoryBase
{

	private static final String GET_LEGACY_KITS = "SELECT * FROM kitProgression WHERE uuid=?";
	private static final String DELETE_LEGACY_KITS = "DELETE FROM kitProgression WHERE uuid=?";

	public KitRepository(MineplexGameManager manager)
	{
		super(DBPool.getAccount());

		CoreClientManager clientManager = manager.getClientManager();

		clientManager.addStoredProcedureLoginProcessor(new ILoginProcessor()
		{
			@Override
			public String getName()
			{
				return "KitBasicInfo";
			}

			@Override
			public void processLoginResultSet(String playerName, UUID uuid, int accountId, ResultSet resultSet) throws SQLException
			{
				Map<GameKit, PlayerKitData> kitData = manager.Get(uuid);

				while (resultSet.next())
				{
					manager.getKitFrom(resultSet.getInt("kitId")).ifPresent(kit ->
					{
						try
						{
							kitData.put(kit, new PlayerKitData(resultSet.getBoolean("active")));
						}
						catch (SQLException e)
						{
							e.printStackTrace();
						}
					});
				}
			}

			@Override
			public String getQuery(int accountId, String uuid, String name)
			{
				return "SELECT * FROM accountKits WHERE accountId=" + accountId + ";";
			}
		});
		clientManager.addStoredProcedureLoginProcessor(new ILoginProcessor()
		{
			@Override
			public String getName()
			{
				return "KitStats";
			}

			@Override
			public void processLoginResultSet(String playerName, UUID uuid, int accountId, ResultSet resultSet) throws SQLException
			{
				Map<GameKit, PlayerKitData> kitData = manager.Get(uuid);

				while (resultSet.next())
				{
					manager.getKitFrom(resultSet.getInt("kitId")).ifPresent(kit ->
					{
						PlayerKitData data = kitData.get(kit);

						if (data != null)
						{
							try
							{
								KitStat.getById(resultSet.getInt("statId")).ifPresent(stat ->
								{
									try
									{
										data.getStats().put(stat, resultSet.getInt("value"));
									}
									catch (SQLException e)
									{
										e.printStackTrace();
									}
								});
							}
							catch (SQLException e)
							{
								e.printStackTrace();
							}
						}
					});
				}
			}

			@Override
			public String getQuery(int accountId, String uuid, String name)
			{
				return "SELECT * FROM accountKitStats WHERE accountId=" + accountId + ";";
			}
		});
	}

	public List<LegacyKit> getLegacyKits(UUID uuid)
	{
		EnclosedObject<Boolean> success = new EnclosedObject<>(true);
		List<LegacyKit> kits = new ArrayList<>();

		executeQuery(
				GET_LEGACY_KITS,
				resultSet ->
				{
					while (resultSet.next())
					{
						kits.add(new LegacyKit(resultSet.getString("kitId"), resultSet.getInt("xp"), resultSet.getInt("level"), resultSet.getInt("upgrade_level")));
					}
				},
				() -> success.Set(false),
				new ColumnVarChar("uuid", 36, uuid.toString())
		);

		return success.Get() ? kits : null;
	}

	public boolean deleteLegacyKits(UUID uuid)
	{
		EnclosedObject<Boolean> success = new EnclosedObject<>(true);

		executeUpdate(
				DELETE_LEGACY_KITS,
				() -> success.Set(false),
				new ColumnVarChar("uuid", 36, uuid.toString())
		);

		return success.Get();
	}

	boolean executeKitOperation(KitOperations operations)
	{
		int accountId = operations.getAccountId();

		if (accountId == -1 || operations.getQuery().isEmpty())
		{
			return false;
		}

		EnclosedObject<Boolean> success = new EnclosedObject<>(true);

		executeInsert(operations.getQuery(), null, () -> success.Set(false));

		return success.Get();
	}
}
