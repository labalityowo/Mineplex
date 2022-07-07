package mineplex.core.game.winstreaks;

import mineplex.serverdata.database.DBPool;
import mineplex.serverdata.database.RepositoryBase;
import mineplex.serverdata.database.column.ColumnInt;

class WinStreakRepository extends RepositoryBase
{

	private static final String UPDATE_OR_INSERT = "INSERT INTO accountWinStreak VALUES (?,?,?) ON DUPLICATE KEY UPDATE value=value+1";
	private static final String DELETE = "DELETE FROM accountWinStreak WHERE accountId=? AND gameId=?";

	WinStreakRepository()
	{
		super(DBPool.getAccount());
	}

	void incrementWinStreak(int accountId, int gameId)
	{
		executeUpdate(UPDATE_OR_INSERT,
				new ColumnInt("accountId", accountId),
				new ColumnInt("gameId", gameId),
				new ColumnInt("value", 1)
		);
	}

	void removeWinStreak(int accountId, int gameId)
	{
		executeUpdate(DELETE,
				new ColumnInt("accountId", accountId),
				new ColumnInt("gameId", gameId)
		);
	}
}
