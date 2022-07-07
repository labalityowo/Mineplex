package mineplex.core.aprilfools;

import mineplex.core.account.CoreClient;
import mineplex.serverdata.database.DBPool;
import mineplex.serverdata.database.RepositoryBase;
import mineplex.serverdata.database.column.ColumnInt;
import mineplex.serverdata.database.column.ColumnVarChar;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class AprilFoolsRepository extends RepositoryBase
{

	private static final String GET_TREASURE = "SELECT * FROM aprilFoolsTreasure WHERE accountId=?;";
	private static final String INSERT_TREASURE = "INSERT INTO aprilFoolsTreasure VALUES (?,?);";

	public AprilFoolsRepository()
	{
		super(DBPool.getAccount());
	}

	public void getTreasure(Consumer<List<Integer>> response, CoreClient client)
	{
		executeQuery(GET_TREASURE, resultSet ->
		{
			List<Integer> found = new ArrayList<>();

			while (resultSet.next())
			{
				found.add(resultSet.getInt("treasureId"));
			}

			response.accept(found);
		}, new ColumnInt("accountId", client.getAccountId()));
	}

	public void saveTreasure(CoreClient client, int treasureId)
	{
		int accountId = client.getAccountId();

		executeInsert(INSERT_TREASURE, null,
				new ColumnInt("accountId", accountId),
				new ColumnInt("treasureId", treasureId)
		);
	}
}
