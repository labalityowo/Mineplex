package mineplex.core.antihack.compedaccount;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

import mineplex.core.common.util.UtilServer;
import mineplex.serverdata.database.DBPool;
import mineplex.serverdata.database.RepositoryBase;

public class CompromisedAccountRepository extends RepositoryBase
{
	private static final String INSERT_BAN = "INSERT INTO gwenPunishments (accountId, ip, type) VALUES (?, ?, ?)";

	public CompromisedAccountRepository()
	{
		super(DBPool.getAccount());
	}

	public CompletableFuture<Void> insertPunishment(int accountId, String ip, String type)
	{
		CompletableFuture<Void> future = new CompletableFuture<>();

		CompletableFuture.runAsync(() -> {
			try (Connection connection = getConnection())
			{
				try (PreparedStatement preparedStatement = connection.prepareStatement(INSERT_BAN))
				{
					preparedStatement.setInt(1, accountId);
					preparedStatement.setString(2, ip);
					preparedStatement.setString(3, type);
					preparedStatement.executeUpdate();
				}
			}
			catch (SQLException ex)
			{
				future.completeExceptionally(ex);
				return;
			}
			future.complete(null);
		});

		return future;
	}
}
