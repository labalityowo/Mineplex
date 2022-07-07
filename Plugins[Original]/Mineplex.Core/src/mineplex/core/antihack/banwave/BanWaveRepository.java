package mineplex.core.antihack.banwave;

import mineplex.core.common.util.Callback;
import mineplex.core.database.MinecraftRepository;
import mineplex.serverdata.database.DBPool;
import mineplex.serverdata.database.RepositoryBase;
import mineplex.serverdata.database.column.ColumnInt;
import mineplex.serverdata.database.column.ColumnLong;
import mineplex.serverdata.database.column.ColumnVarChar;

public class BanWaveRepository extends RepositoryBase
{
	private static final String INITIALIZE_PENDING_TABLE = "CREATE TABLE IF NOT EXISTS banwavePending (" +
			"accountId INT(11) NOT NULL, " +
			"timeToBan BIGINT UNSIGNED NOT NULL, " +
			"hacktype VARCHAR(64), " +
			"message VARCHAR(255), " +
			"vl INT, " +
			"server VARCHAR(32), " +
			"PRIMARY KEY (accountId)," +
			"FOREIGN KEY (accountId) REFERENCES accounts(id))";

	private static final String INITIALIZED_PROCESSED_TABLE = "CREATE TABLE IF NOT EXISTS banwaveProcessed (" +
			"id INT NOT NULL AUTO_INCREMENT, " +
			"accountId INT(11) NOT NULL, " +
			"timeToBan BIGINT UNSIGNED NOT NULL, " +
			"hacktype VARCHAR(64), " +
			"message VARCHAR(255), " +
			"vl INT, " +
			"server VARCHAR(32), " +
			"PRIMARY KEY (id)," +
			"FOREIGN KEY (accountId) REFERENCES accounts(id))";
	private static final String QUERY_PENDING = "SELECT * FROM banwavePending WHERE accountId = ?";
	private static final String INSERT_PENDING = "INSERT IGNORE INTO banwavePending (accountId, timeToBan, hacktype, message, vl, server) VALUES (?, ?, ?, ?, ?, ?)";

	private static final String PROCESS_WAVE_FOR_ACCOUNT = "INSERT INTO banwaveProcessed SELECT 0, accountId, timeToBan, hacktype, message, vl, server FROM banwavePending WHERE accountId = ?";
	private static final String DELETE_PENDING = "DELETE FROM banwavePending WHERE accountId = ?";

	BanWaveRepository()
	{
		super(DBPool.getAccount());
	}

	void getPendingBanWaveInfo(int accountId, Callback<BanWaveInfo> callback)
	{
		executeQuery(QUERY_PENDING, resultSet ->
		{
			if (resultSet.next())
			{
				BanWaveInfo info = new BanWaveInfo();
				info.setAccountId(resultSet.getInt(1));
				info.setTimeToBan(resultSet.getLong(2));
				info.setHackType(resultSet.getString(3));
				info.setMessage(resultSet.getString(4));
				info.setVl(resultSet.getInt(5));
				info.setServer(resultSet.getString(6));

				callback.run(info);
			}
		}, new ColumnInt("accountId", accountId));
	}

	boolean insertBanWaveInfo(int accountId, long timeToBan, String hackType, String message, int vl, String server)
	{
		int affectedRows = executeInsert(INSERT_PENDING, null,
				new ColumnInt("accountId", accountId),
				new ColumnLong("timeToBan", timeToBan),
				new ColumnVarChar("hacktype", 64, hackType),
				new ColumnVarChar("message", 255, message),
				new ColumnInt("vl", vl),
				new ColumnVarChar("server", 32, server)
		);
		return affectedRows > 0;
	}

	void flagDone(BanWaveInfo info)
	{
		flagDone(info.getAccountId());
	}

	public void flagDone(int accountId)
	{
		executeUpdate(PROCESS_WAVE_FOR_ACCOUNT, new ColumnInt("id", accountId));
		executeUpdate(DELETE_PENDING, new ColumnInt("id", accountId));
	}
}
