package mineplex.core.rankGiveaway.titangiveaway;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Types;

import org.bukkit.plugin.java.JavaPlugin;

import mineplex.serverdata.Region;
import mineplex.serverdata.database.DBPool;
import mineplex.serverdata.database.RepositoryBase;
import mineplex.serverdata.database.column.ColumnInt;
import mineplex.serverdata.database.column.ColumnVarChar;

public class TitanGiveawayRepository extends RepositoryBase
{
	private static final String ADD_TITAN = "INSERT INTO titanGiveaway (accountId, region, serverName) VALUES (?, ?, ?)";

	private int _titanCount;

	public TitanGiveawayRepository(JavaPlugin plugin)
	{
		super(DBPool.getAccount());
		_titanCount = 0;
	}

	public boolean addTitan(int accountId, Region region, String serverName)
	{
		return 1 == executeUpdate(ADD_TITAN, new ColumnInt("accountId", accountId), new ColumnVarChar("region", 10, region.name()), new ColumnVarChar("serverName", 64, serverName));
	}

	public boolean canGiveaway(Region region)
	{
		try (Connection connection = getConnection();
			 CallableStatement callableStatement = connection.prepareCall("{call check_titanGiveaway(?, ?, ?)}"))
		{
			callableStatement.setString(1, region.name());
			callableStatement.registerOutParameter(2, Types.BOOLEAN);
			callableStatement.registerOutParameter(3, Types.INTEGER);
			callableStatement.executeUpdate();

			boolean pass = callableStatement.getBoolean(2);
			int titanCount = callableStatement.getInt(3);

			_titanCount = titanCount;
			return pass;
		}
		catch (Exception e)
		{
		}
		return false;
	}

	public int getTitanCount()
	{
		return _titanCount;
	}
}