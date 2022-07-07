package mineplex.core.mission;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import mineplex.core.common.util.EnclosedObject;
import mineplex.serverdata.database.DBPool;
import mineplex.serverdata.database.RepositoryBase;
import mineplex.serverdata.database.column.ColumnInt;

public class MissionRepository extends RepositoryBase
{

	private static final String START_MISSION = "INSERT INTO accountMissions (accountId, missionId, length, x, y, startTime) VALUES ({0},{1},{2},{3},{4},{5});";
	private static final String INCREMENT_PROGRESS = "UPDATE accountMissions SET progress=progress+{0} WHERE accountId={1} AND missionId={2};";
	private static final String IS_MISSION_COMPLETE = "SELECT complete FROM accountMissions WHERE accountId=? AND missionId=?;";
	private static final String COMPLETE_MISSION = "UPDATE accountMissions SET complete=1 WHERE accountId=? AND missionId=?;";
	private static final String DISCARD_MISSION = "UPDATE accountMissions SET complete=-1 WHERE accountId=? AND missionId=?;";
	private static final String CLEAR_PROGRESS = "DELETE FROM accountMissions WHERE accountId=? AND missionId=?;";

	static MissionQuery startMission(int accountId, Runnable callback, PlayerMission mission)
	{
		return new MissionQuery(START_MISSION, accountId, callback, accountId, mission.getId(), mission.getLength().ordinal(), mission.getRequiredProgress(), mission.getY(), System.currentTimeMillis());
	}

	static MissionQuery incrementProgress(int accountId, Runnable callback, int progress, Mission mission)
	{
		return new MissionQuery(INCREMENT_PROGRESS, accountId, callback, progress, accountId, mission.getId());
	}

	static class MissionQuery
	{
		private final String _query;
		private final int _accountId;
		private final Runnable _callback;

		MissionQuery(String query, int accountId, Runnable callback, Object... args)
		{
			for (int i = 0; i < args.length; i++)
			{
				query = query.replace("{" + i + "}", String.valueOf(args[i]));
			}

			_query = query;
			_accountId = accountId;
			_callback = callback;
		}
	}

	private final List<MissionQuery> _queries;
	private final Object _lock;

	MissionRepository()
	{
		super(DBPool.getAccount());

		_queries = new ArrayList<>();
		_lock = new Object();
	}

	public void addQueryToQueue(MissionQuery query)
	{
		if (query._accountId == -1)
		{
			return;
		}

		_queries.add(query);
	}

	public void bulkProcess()
	{
		synchronized (_lock)
		{
			if (_queries.isEmpty())
			{
				return;
			}

			String sqlQuery = _queries.stream()
					.map(query -> query._query)
					.collect(Collectors.joining());

			if (executeUpdate(sqlQuery) > 0)
			{
				_queries.forEach(query -> query._callback.run());
			}

			_queries.clear();
		}
	}

	public boolean completeMission(int accountId, int missionId)
	{
		return !isComplete(accountId, missionId) && executeUpdate(COMPLETE_MISSION, new ColumnInt("accountId", accountId), new ColumnInt("missionId", missionId)) > 0;
	}

	public boolean isComplete(int accountId, int missionId)
	{
		if (accountId == -1)
		{
			return false;
		}

		EnclosedObject<Boolean> complete = new EnclosedObject<>(false);

		executeQuery(IS_MISSION_COMPLETE, resultSet ->
				{
					while (resultSet.next())
					{
						complete.Set(resultSet.getByte("complete") == PlayerMission.COMPLETE);
					}
				},
				new ColumnInt("accountId", accountId),
				new ColumnInt("missionId", missionId)
		);

		return complete.Get();
	}

	public boolean discardMission(int accountId, int missionId)
	{
		return accountId != -1 && executeUpdate(DISCARD_MISSION,
				new ColumnInt("accountId", accountId),
				new ColumnInt("missionId", missionId)
		) > 0;
	}

	public boolean clearMission(int accountId, int missionId)
	{
		return accountId != -1 && executeUpdate(CLEAR_PROGRESS,
				new ColumnInt("accountId", accountId),
				new ColumnInt("missionId", missionId)
		) > 0;
	}
}
