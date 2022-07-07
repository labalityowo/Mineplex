package mineplex.core.game.nano;

import mineplex.serverdata.database.DBPool;
import mineplex.serverdata.database.RepositoryBase;
import mineplex.serverdata.database.column.ColumnInt;

public class NanoRepository extends RepositoryBase
{

	private static final String ADD_FAVOURITE = "INSERT INTO accountFavouriteNano VALUES (?,?);";
	private static final String REMOVE_FAVOURITE = "DELETE FROM accountFavouriteNano WHERE accountId=? AND gameId=?;";
	private static final String CLEAR_FAVOURITES = "DELETE FROM accountFavouriteNano WHERE accountId=?;";

	NanoRepository()
	{
		super(DBPool.getAccount());
	}

	void setFavourite(int accountId, int gameId, boolean favourite)
	{
		executeInsert(favourite ? ADD_FAVOURITE : REMOVE_FAVOURITE, null, new ColumnInt("accountId", accountId), new ColumnInt("gameId", gameId));
	}

	void clearFavourites(int accountId)
	{
		executeUpdate(CLEAR_FAVOURITES, new ColumnInt("accountId", accountId));
	}
}
