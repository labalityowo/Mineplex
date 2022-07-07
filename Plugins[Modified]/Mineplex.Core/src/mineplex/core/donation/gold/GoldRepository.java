package mineplex.core.donation.gold;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.bukkit.Bukkit;

import mineplex.core.common.util.Callback;
import mineplex.core.common.util.UtilServer;
import mineplex.serverdata.database.DBPool;

public class GoldRepository
{
	private static final int DUPLICATE_PRIMARY_KEY_ERROR_CODE = 1062;
	private static final String CREATE_TABLE = "CREATE TABLE clansGold (serverId int(11) not null, accountId int(11) not null, gold int not null, primary key (serverId, accountId), index valueIndex (serverId, accountId, gold), index goldIndex (serverId, gold), foreign key (serverId) references clanServer(id), foreign key (accountId) references accounts(id))";
	private static final String INSERT_ACCOUNT_GOLD = "INSERT INTO clansGold (serverId, accountId, gold) VALUES (?, ?, ?);";
	private static final String UPDATE_ACCOUNT_GOLD = "UPDATE clansGold SET gold=gold+? WHERE serverId=? AND accountId=? AND (gold+? > 0 OR gold+? = 0);";
	private static final String SET_ACCOUNT_GOLD = "INSERT INTO clansGold (serverId, accountId, gold) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE gold=VALUES(gold);";

	public GoldRepository() {}

	public void rewardGold(final Callback<Boolean> callback, final int serverId, final int accountId, final int gold)
	{
		Bukkit.getScheduler().runTaskAsynchronously(UtilServer.getPlugin(), () ->
		{
			try (Connection connection = DBPool.getAccount().getConnection())
			{
				try (PreparedStatement statement = connection.prepareStatement(INSERT_ACCOUNT_GOLD))
				{
					statement.setInt(1, serverId);
					statement.setInt(2, accountId);
					statement.setInt(3, Math.max(gold, 0));
					statement.executeUpdate();
					
					final boolean success = gold >= 0;
					if (callback != null)
					{
						Bukkit.getScheduler().runTask(UtilServer.getPlugin(), () -> callback.run(success));
					}
				}
				catch (SQLException ex)
				{
					if (ex.getErrorCode() == DUPLICATE_PRIMARY_KEY_ERROR_CODE)
					{
						try (PreparedStatement statement = connection.prepareStatement(UPDATE_ACCOUNT_GOLD))
						{
							statement.setInt(1, gold);
							statement.setInt(2, serverId);
							statement.setInt(3, accountId);
							statement.setInt(4, gold);
							statement.setInt(5, gold);
							int updateCount = statement.executeUpdate();
							
							final boolean success = updateCount > 0;
							
							if (callback != null)
							{
								Bukkit.getScheduler().runTask(UtilServer.getPlugin(), () -> callback.run(success));
							}
						}
					}
					else
					{
						ex.printStackTrace();
						if (callback != null)
						{
							Bukkit.getScheduler().runTask(UtilServer.getPlugin(), () -> callback.run(false));
						}
					}
				}
			}
			catch (SQLException e)
			{
				e.printStackTrace();

				if (callback != null)
				{
					Bukkit.getScheduler().runTask(UtilServer.getPlugin(), () -> callback.run(false));
				}
			}
		});
	}

	public void setGold(final Callback<Boolean> callback, final int serverId, final int accountId, final int gold)
	{
		if (gold < 0)
		{
			throw new IllegalArgumentException("Gold cannot be negative");
		}

		Bukkit.getScheduler().runTaskAsynchronously(UtilServer.getPlugin(), () ->
		{
			try (Connection connection = DBPool.getAccount().getConnection())
			{
				PreparedStatement statement = connection.prepareStatement(SET_ACCOUNT_GOLD);
				statement.setInt(1, serverId);
				statement.setInt(2, accountId);
				statement.setInt(3, gold);
				statement.executeUpdate();

				if (callback != null)
				{
					Bukkit.getScheduler().runTask(UtilServer.getPlugin(), () -> callback.run(true));
				}
			}
			catch (SQLException e)
			{
				e.printStackTrace();

				if (callback != null)
				{
					Bukkit.getScheduler().runTask(UtilServer.getPlugin(), () -> callback.run(false));
				}
			}
		});
	}
}