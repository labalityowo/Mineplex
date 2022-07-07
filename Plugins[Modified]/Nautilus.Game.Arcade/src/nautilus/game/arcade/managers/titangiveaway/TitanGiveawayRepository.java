package nautilus.game.arcade.managers.titangiveaway;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Types;

import mineplex.core.database.MinecraftRepository;
import mineplex.serverdata.database.DBPool;
import mineplex.serverdata.database.RepositoryBase;

import org.bukkit.plugin.java.JavaPlugin;

public class TitanGiveawayRepository extends RepositoryBase
{
	private int _titanGiveawayCount;

	public TitanGiveawayRepository(JavaPlugin plugin)
	{
		super(DBPool.getAccount());
		_titanGiveawayCount = 0;
	}

	public boolean canGiveaway()
	{
		try (Connection connection = getConnection();
			 CallableStatement callableStatement = connection.prepareCall("{call check_titanGiveaway(?)}"))
		{
			callableStatement.registerOutParameter(1, Types.BOOLEAN);


		}
		catch (Exception e)
		{
		}

		return false;
	}
}
