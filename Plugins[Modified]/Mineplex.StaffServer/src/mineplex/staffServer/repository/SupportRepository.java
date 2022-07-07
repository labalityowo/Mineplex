package mineplex.staffServer.repository;

import java.sql.ResultSet;
import java.util.function.Consumer;

import mineplex.serverdata.database.DBPool;
import mineplex.serverdata.database.RepositoryBase;

public class SupportRepository extends RepositoryBase
{
	public SupportRepository()
	{
		super(DBPool.getAccount());
	}

	public void loadBonusLog(int accountId, Consumer<ResultSet> callback)
	{
		executeQuery("SELECT accountId, items.name, itemChange, time FROM bonusLog INNER JOIN items ON itemId = items.id WHERE accountId = " + accountId + " ORDER BY bonusLog.id DESC;", callback::accept);
	}
}
