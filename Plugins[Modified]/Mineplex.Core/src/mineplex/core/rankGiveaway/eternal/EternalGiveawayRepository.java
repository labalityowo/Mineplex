package mineplex.core.rankGiveaway.eternal;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Types;

import org.bukkit.plugin.java.JavaPlugin;

import mineplex.core.database.MinecraftRepository;
import mineplex.serverdata.Region;
import mineplex.serverdata.database.DBPool;
import mineplex.serverdata.database.RepositoryBase;
import mineplex.serverdata.database.column.ColumnInt;
import mineplex.serverdata.database.column.ColumnVarChar;

public class EternalGiveawayRepository extends RepositoryBase
{
	private static final String ADD_ETERNAL = "INSERT INTO eternalGiveaway (accountId, region, serverName) VALUES (?, ?, ?)";

	private int _eternalCount;

	public EternalGiveawayRepository(JavaPlugin plugin)
	{
		super(DBPool.getAccount());
		_eternalCount = 0;
	}

	@Override
	protected void initialize()
	{

	}

	@Override
	protected void update()
	{

	}

	public boolean addEternal(int accountId, Region region, String serverName)
	{
		return 1 == executeUpdate(ADD_ETERNAL, new ColumnInt("accountId", accountId), new ColumnVarChar("region", 10, region.name()), new ColumnVarChar("serverName", 64, serverName));
	}

	public boolean canGiveaway(Region region)
	{
		try (Connection connection = getConnection();
			 CallableStatement callableStatement = connection.prepareCall("{call check_eternalGiveaway(?, ?, ?)}"))
		{
			callableStatement.setString(1, region.name());
			callableStatement.registerOutParameter(2, Types.BOOLEAN);
			callableStatement.registerOutParameter(3, Types.INTEGER);
			callableStatement.executeUpdate();

			boolean pass = callableStatement.getBoolean(2);
			int eternalCount = callableStatement.getInt(3);

			_eternalCount = eternalCount;
			return pass;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return false;
	}

	public int getEternalCount()
	{
		return _eternalCount;
	}
}