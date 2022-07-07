package mineplex.core.giveaway;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.UUID;

import mineplex.core.database.MinecraftRepository;
import org.bukkit.plugin.java.JavaPlugin;

import mineplex.serverdata.database.DBPool;
import mineplex.serverdata.database.RepositoryBase;
import mineplex.serverdata.database.ResultSetCallable;
import mineplex.serverdata.database.column.ColumnInt;
import mineplex.serverdata.database.column.ColumnVarChar;
import mineplex.serverdata.Region;

public class GiveawayRepository extends RepositoryBase
{
	private static final String INSERT_GIVEAWAY = "INSERT INTO Account.accountGiveaway (giveawayId, accountId, cooldownId, region, serverName, time, uuid) VALUES (?, ?, ?, ?, ?, now(), ?)";
	private static final String LOAD_GIVEAWAY = "SELECT id, name, prettyName, header, message, max, notifyNetwork, notifyCooldown, canWinTwice FROM Account.giveaway WHERE enabled = TRUE";
	private static final String LOAD_COOLDOWN = "SELECT id, name, cooldown FROM Account.giveawayCooldown";

	public GiveawayRepository(JavaPlugin plugin)
	{
		super(DBPool.getAccount());
	}

	public boolean canGiveaway(int accountId, String giveawayName, String cooldownName)
	{
		try (Connection connection = getConnection();
			 CallableStatement callableStatement = connection.prepareCall("{call check_giveaway(?, ?, ?, ?)}"))
		{
			callableStatement.setInt(1, accountId);
			callableStatement.setString(2, giveawayName);
			callableStatement.setString(3, cooldownName);
			callableStatement.registerOutParameter(4, Types.BOOLEAN);
			callableStatement.executeUpdate();

			boolean pass = callableStatement.getBoolean(4);
			return pass;
		}
		catch (Exception e)
		{
		}
		return false;
	}

	public boolean addGiveaway(int accountId, int giveawayId, int cooldownId, Region region, String serverName, UUID uuid)
	{
		return 1 == executeUpdate(INSERT_GIVEAWAY, new ColumnInt("giveawayId", giveawayId), new ColumnInt("accountId", accountId),
				new ColumnInt("cooldownId", cooldownId), new ColumnVarChar("region", 10, region.name()), new ColumnVarChar("serverName", 64, serverName),
				new ColumnVarChar("uuid", 32, uuid.toString().replaceAll("-", "")));
	}

	public HashMap<String, Giveaway> loadGiveaways()
	{
		final HashMap<String, Giveaway> map = new HashMap<String, Giveaway>();
		executeQuery(LOAD_GIVEAWAY, new ResultSetCallable()
		{
			@Override
			public void processResultSet(ResultSet resultSet) throws SQLException
			{
				while (resultSet.next())
				{
					int id = resultSet.getInt(1);
					String name = resultSet.getString(2);
					String prettyName = resultSet.getString(3);
					String header = resultSet.getString(4);
					String message = resultSet.getString(5);
					int max = resultSet.getInt(6);
					boolean notifyNetwork = resultSet.getBoolean(7);
					int notifyCooldown = resultSet.getInt(8);
					boolean canWinTwice = resultSet.getBoolean(9);

					Giveaway giveaway = new Giveaway(id, name, prettyName, header, message, notifyNetwork, notifyCooldown);
					map.put(name, giveaway);
				}
			}
		});
		return map;
	}

	public HashMap<String, GiveawayCooldown> loadCooldowns()
	{
		final HashMap<String, GiveawayCooldown> map = new HashMap<String, GiveawayCooldown>();
		executeQuery(LOAD_COOLDOWN, new ResultSetCallable()
		{
			@Override
			public void processResultSet(ResultSet resultSet) throws SQLException
			{
				while (resultSet.next())
				{
					int id = resultSet.getInt(1);
					String name = resultSet.getString(2);
					int cooldown = resultSet.getInt(3);
					GiveawayCooldown cd = new GiveawayCooldown(id, name, cooldown);
					map.put(name, cd);
				}
			}
		});
		return map;
	}

	public PlayerGiveawayData loadPlayerGiveaway(ResultSet resultSet) throws SQLException
	{
		PlayerGiveawayData giveawayData = new PlayerGiveawayData();

		while (resultSet.next())
		{
			String name = resultSet.getString(1);
			String uuid = resultSet.getString(2);
			GiveawayReward reward = new GiveawayReward(name, uuid);
			giveawayData.addGiveawayReward(reward);
		}

		return giveawayData;
	}
}
