package mineplex.core.achievement.leveling;

import mineplex.serverdata.database.DBPool;
import mineplex.serverdata.database.RepositoryBase;
import mineplex.serverdata.database.column.ColumnInt;

public class LevelingRepository extends RepositoryBase
{

	private static final String CLAIM_REWARD = "INSERT INTO accountLevelReward VALUES (?,?);";

	LevelingRepository()
	{
		super(DBPool.getAccount());
	}

	public boolean claimReward(int accountId, int level)
	{
		return executeInsert(CLAIM_REWARD, null,
				new ColumnInt("accountId", accountId),
				new ColumnInt("level", level)
		) > 0;
	}
}
