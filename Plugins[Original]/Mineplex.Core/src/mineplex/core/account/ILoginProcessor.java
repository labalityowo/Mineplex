package mineplex.core.account;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public interface ILoginProcessor
{
	String getName();
	
	void processLoginResultSet(String playerName, UUID uuid, int accountId, ResultSet resultSet) throws SQLException;

	String getQuery(int accountId, String uuid, String name);
}