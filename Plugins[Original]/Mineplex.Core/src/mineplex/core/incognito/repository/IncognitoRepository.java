package mineplex.core.incognito.repository;

import mineplex.core.incognito.IncognitoManager;
import mineplex.serverdata.database.DBPool;
import mineplex.serverdata.database.RepositoryBase;
import mineplex.serverdata.database.column.ColumnInt;

public class IncognitoRepository extends RepositoryBase
{
	private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS incognitoStaff (accountId INT NOT NULL, status TINYINT(1) DEFAULT '0', PRIMARY KEY (accountId));";
	private static final String INSERT_STATUS = "INSERT INTO incognitoStaff (accountId, status) VALUES (?, ?);";
	private static final String UPDATE_STATUS = "UPDATE incognitoStaff SET status=? WHERE accountId=?;";
	
	public IncognitoRepository(IncognitoManager incognitoManager)
	{
		super(DBPool.getAccount());
	}
	
	public void setStatus(int accountId, boolean status)
	{
		if (executeUpdate(UPDATE_STATUS, new ColumnInt("status", status ? 1 : 0), new ColumnInt("accountId", accountId)) <= 0)
			executeInsert(INSERT_STATUS, null, new ColumnInt("accountId", accountId), new ColumnInt("status", status ? 1 : 0));
	}
	
	protected void initialize()
	{
		executeUpdate(CREATE_TABLE);
	}
}