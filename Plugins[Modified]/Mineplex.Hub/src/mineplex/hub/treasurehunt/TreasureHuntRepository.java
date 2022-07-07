package mineplex.hub.treasurehunt;

import mineplex.serverdata.database.DBPool;
import mineplex.serverdata.database.RepositoryBase;
import mineplex.serverdata.database.column.ColumnInt;

public class TreasureHuntRepository extends RepositoryBase
{

	private static final String INSERT_TREASURE = "INSERT INTO accountTreasureHunt VALUES (?,?);";

	TreasureHuntRepository()
	{
		super(DBPool.getAccount());
	}

	void saveTreasure(int accountId, int treasureId)
	{
		executeInsert(INSERT_TREASURE, null,
				new ColumnInt("accountId", accountId),
				new ColumnInt("treasureId", treasureId)
		);
	}
}
