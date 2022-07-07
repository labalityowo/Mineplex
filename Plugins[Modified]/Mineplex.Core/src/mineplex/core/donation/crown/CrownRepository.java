package mineplex.core.donation.crown;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.bukkit.Bukkit;

import mineplex.core.common.util.Callback;
import mineplex.core.common.util.UtilServer;
import mineplex.core.server.util.TransactionResponse;
import mineplex.serverdata.database.DBPool;

public class CrownRepository
{
	private static final String CREATE_TABLE = "CREATE TABLE accountCrowns (accountId INT(11) NOT NULL, crownCount INT NOT NULL, PRIMARY KEY (accountId), FOREIGN KEY (accountId) REFERENCES accounts(id));";
	private static final String REWARD_ACCOUNT_CROWNS = "INSERT INTO accountCrowns (accountId, crownCount) VALUES (?, ?) ON DUPLICATE KEY UPDATE crownCount=crownCount+VALUES(crownCount);";
	private static final String CONSUME_ACCOUNT_CROWNS = "UPDATE accountCrowns SET crownCount=(crownCount - ?) WHERE accountId=? AND crownCount >= ?;";
	private static final String SET_ACCOUNT_CROWNS = "INSERT INTO accountCrowns (accountId, crownCount) VALUES (?, ?) ON DUPLICATE KEY UPDATE crownCount=VALUES(crownCount);";

	public CrownRepository() {}

	public void rewardCrowns(final Callback<Boolean> callback, final int accountId, final int crowns)
	{
		Bukkit.getScheduler().runTaskAsynchronously(UtilServer.getPlugin(), () ->
		{
			try (Connection connection = DBPool.getAccount().getConnection())
			{
				PreparedStatement statement = connection.prepareStatement(REWARD_ACCOUNT_CROWNS);
				statement.setInt(1, accountId);
				statement.setInt(2, crowns);
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
	
	public void consumeCrowns(final Callback<TransactionResponse> callback, final int accountId, final int crowns)
	{
		Bukkit.getScheduler().runTaskAsynchronously(UtilServer.getPlugin(), () ->
		{
			try (Connection connection = DBPool.getAccount().getConnection())
			{
				String baseStmt = "INSERT INTO accountCrowns (accountId, crownCount) VALUES (" + accountId + ", 0) ON DUPLICATE KEY UPDATE crownCount=crownCount;";
				PreparedStatement statement = connection.prepareStatement(baseStmt + CONSUME_ACCOUNT_CROWNS);
				statement.setInt(1, crowns);
				statement.setInt(2, accountId);
				statement.setInt(3, crowns);
				statement.execute();
				
				statement.getMoreResults();
				
				final TransactionResponse response = statement.getUpdateCount() > 0 ? TransactionResponse.Success : TransactionResponse.InsufficientFunds;

				if (callback != null)
				{
					Bukkit.getScheduler().runTask(UtilServer.getPlugin(), () -> callback.run(response));
				}
			}
			catch (SQLException e)
			{
				e.printStackTrace();

				if (callback != null)
				{
					Bukkit.getScheduler().runTask(UtilServer.getPlugin(), () -> callback.run(TransactionResponse.Failed));
				}
			}
		});
	}

	public void setCrowns(final Callback<Boolean> callback, final int accountId, final int crowns)
	{
		if (crowns < 0)
		{
			throw new IllegalArgumentException("Crowns cannot be negative");
		}

		Bukkit.getScheduler().runTaskAsynchronously(UtilServer.getPlugin(), () ->
		{
			try (Connection connection = DBPool.getAccount().getConnection())
			{
				PreparedStatement statement = connection.prepareStatement(SET_ACCOUNT_CROWNS);
				statement.setInt(1, accountId);
				statement.setInt(2, crowns);
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