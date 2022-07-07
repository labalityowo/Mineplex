package mineplex.core.twofactor;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.BiConsumer;

import org.bukkit.entity.Player;

import mineplex.core.Managers;
import mineplex.core.account.CoreClientManager;
import mineplex.core.account.ILoginProcessor;

public class TwoFactorRepository
{
	private static final String INSERT_SECRET_KEY = "INSERT INTO twofactor (accountId,secretKey) VALUES (?,?);";
	private static final String DELETE_SECRET_KEY = "DELETE FROM twofactor WHERE accountId=?;";
	private static final String INSERT_LOGIN = "INSERT INTO twofactor_history (accountId,ip,loginTime) VALUES (?,?,NOW());";
	private static final String DELETE_RECENT_LOGINS = "DELETE FROM twofactor_history WHERE accountId=? AND loginTime >= DATE_SUB(NOW(), INTERVAL 1 DAY);";
	private final CoreClientManager _clientManager = Managers.require(CoreClientManager.class);
	private final DataSource _dataSource;

	public TwoFactorRepository(DataSource source)
	{
		_dataSource = source;
	}

	public ILoginProcessor buildSecretKeyLoginProcessor(BiConsumer<UUID, String> consumer)
	{
		return new ILoginProcessor()
		{
			@Override
			public String getName()
			{
				return "Two-factor auth secret key grabber";
			}

			@Override
			public void processLoginResultSet(String playerName, UUID uuid, int accountId, ResultSet resultSet) throws SQLException
			{
				if (resultSet.next())
				{
					consumer.accept(uuid, resultSet.getString(1));
				}
			}

			@Override
			public String getQuery(int accountId, String uuid, String name)
			{
				return "SELECT secretKey FROM twofactor WHERE accountId=" + accountId + ";";
			}
		};
	}

	public ILoginProcessor buildLastIpLoginProcessor(BiConsumer<UUID, String> consumer)
	{
		return new ILoginProcessor()
		{
			@Override
			public String getName()
			{
				return "Two-factor auth last login grabber";
			}

			@Override
			public void processLoginResultSet(String playerName, UUID uuid, int accountId, ResultSet resultSet) throws SQLException
			{
				if (resultSet.next())
				{
					consumer.accept(uuid, resultSet.getString(1));
				}
			}

			@Override
			public String getQuery(int accountId, String uuid, String name)
			{
				return "SELECT ip FROM twofactor_history WHERE accountId=" + accountId + " AND loginTime >= DATE_SUB(NOW(), INTERVAL 1 DAY) ORDER BY loginTime DESC;";
			}
		};
	}

	public CompletableFuture<Void> saveSecret(Player player, String secret)
	{
		int accountId = _clientManager.Get(player).getAccountId();

		return CompletableFuture.runAsync(() ->
		{
			try (Connection connection = _dataSource.getConnection())
			{
				PreparedStatement statement = connection.prepareStatement(INSERT_SECRET_KEY);
				statement.setInt(1, accountId);
				statement.setString(2, secret);
				statement.executeUpdate();
			}
			catch (SQLException e)
			{
				throw new CompletionException(e);
			}
		});
	}

	public CompletableFuture<Void> saveLogin(Player player, String ip)
	{
		int accountId = _clientManager.Get(player).getAccountId();

		return CompletableFuture.runAsync(() ->
		{
			try (Connection connection = _dataSource.getConnection())
			{
				PreparedStatement statement = connection.prepareStatement(INSERT_LOGIN);
				statement.setInt(1, accountId);
				statement.setString(2, ip);
				statement.executeUpdate();
			}
			catch (SQLException e)
			{
				throw new CompletionException(e);
			}
		});
	}

	public CompletableFuture<Void> deletePlayerData(int accountId)
	{
		return CompletableFuture.runAsync(() ->
		{
			try (Connection connection = _dataSource.getConnection())
			{
				PreparedStatement statement = connection.prepareStatement(DELETE_SECRET_KEY);
				statement.setInt(1, accountId);
				statement.executeUpdate();

				statement = connection.prepareStatement(DELETE_RECENT_LOGINS);
				statement.setInt(1, accountId);
				statement.executeUpdate();
			}
			catch (SQLException e)
			{
				throw new CompletionException(e);
			}
		});
	}
}
