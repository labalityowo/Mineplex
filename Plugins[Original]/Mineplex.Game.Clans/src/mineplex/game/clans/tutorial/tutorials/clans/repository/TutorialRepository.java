package mineplex.game.clans.tutorial.tutorials.clans.repository;

import java.util.UUID;

import mineplex.core.account.CoreClientManager;
import mineplex.core.common.util.EnclosedObject;
import mineplex.core.common.util.UUIDFetcher;
import mineplex.core.database.MinecraftRepository;
import mineplex.serverdata.database.DBPool;
import mineplex.serverdata.database.RepositoryBase;
import mineplex.serverdata.database.column.ColumnInt;
import mineplex.serverdata.database.column.ColumnVarChar;

public class TutorialRepository extends RepositoryBase
{
	private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS clansTutorial (uuid VARCHAR(36), timesPlayed INT, PRIMARY KEY (uuid));";
	private static final String GET = "SELECT * FROM clansTutorial WHERE uuid = ?;";
	private static final String INSERT = "INSERT INTO clansTutorial (uuid, timesPlayed) VALUES (?, ?);";
	private static final String UPDATE = "UPDATE clansTutorial SET timesPlayed=? WHERE uuid=?;";
	
	private CoreClientManager _clientManager;
	
	public TutorialRepository(CoreClientManager clientManager)
	{
		super(DBPool.getAccount());
		
		_clientManager = clientManager;
	}
	
	public void SetTimesPlayed(UUID uuid, int timesPlayed)
	{
		// Prevent duplicate entries for individuals
		executeQuery(GET, result ->
		{
			if (result.next())
				executeUpdate(UPDATE, new ColumnInt("timesPlayed", timesPlayed), new ColumnVarChar("uuid", 36, uuid.toString()));
			else
				executeUpdate(INSERT, new ColumnVarChar("uuid", 36, uuid.toString()), new ColumnInt("timesPlayed", timesPlayed));
		}, new ColumnVarChar("uuid", 36, uuid.toString()));
	}
	
	public int GetTimesPlayed(UUID uuid)
	{
		EnclosedObject<Integer> status = new EnclosedObject<>();
		
		executeQuery(GET, result ->
		{
			if (result.next())
				status.Set(result.getInt("timesPlayed"));
			else
				status.Set(0);
		}, new ColumnVarChar("uuid", 36, uuid.toString()));
		
		return status.Get();
	}
	
	public int GetTimesPlayed(String name)
	{
		return GetTimesPlayed(UUIDFetcher.getUUIDOf(name));
	}
	
	protected void initialize()
	{
		executeUpdate(CREATE_TABLE);
	}
}