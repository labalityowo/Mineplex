package mineplex.game.clans.clans.nameblacklist.repository;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.plugin.java.JavaPlugin;

import mineplex.core.common.util.Callback;
import mineplex.game.clans.clans.nameblacklist.ClansBlacklist;
import mineplex.serverdata.database.DBPool;
import mineplex.serverdata.database.RepositoryBase;
import mineplex.serverdata.database.column.ColumnTimestamp;
import mineplex.serverdata.database.column.ColumnVarChar;

public class ClanNameBlacklistRepository extends RepositoryBase
{
	private static final String CREATE = "CREATE TABLE IF NOT EXISTS clanNameBlacklist ("
																		   + "clanName VARCHAR(20) NOT NULL, "
																		   + "admin VARCHAR(16) NOT NULL, "
																		   + "added TIMESTAMP,"
																		   + "PRIMARY KEY (clanName));";

	private static final String GET = "SELECT * FROM clanNameBlacklist;";
	private static final String ADD = "INSERT INTO clanNameBlacklist (clanName, admin, added) VALUES (?, ?, ?);";
	private static final String REMOVE = "DELETE FROM clanNameBlacklist WHERE clanName=?;";
	
	private ClansBlacklist _blacklist;
	
	public ClanNameBlacklistRepository(JavaPlugin plugin, ClansBlacklist blacklist)
	{
		super(DBPool.getAccount());
		
		_blacklist = blacklist;
	}
	
	public void add(String name, String mod)
	{
		_blacklist.add(name.toLowerCase());
		executeInsert(ADD, null,
				new ColumnVarChar("clanName", 20, name.toLowerCase()),
				new ColumnVarChar("admin", 16, mod),
				new ColumnTimestamp("added", new Timestamp(System.currentTimeMillis()))
		);
	}
	
	public void loadNames(final Callback<List<String>> callback)
	{
		executeQuery(GET, resultSet ->
		{
			final List<String> list = new ArrayList<>();
			
			while (resultSet.next())
			{
				String clanName = resultSet.getString("clanName");
				String mod = resultSet.getString("admin");
				
				list.add(clanName);
			}
			
			callback.run(list);
		});
	}

	@Override
	protected void update()
	{
		executeUpdate(CREATE);
	}	
}