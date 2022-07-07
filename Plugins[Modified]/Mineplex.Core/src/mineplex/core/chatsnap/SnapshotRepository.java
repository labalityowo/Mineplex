package mineplex.core.chatsnap;

import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import mineplex.core.common.util.UtilTime;
import mineplex.serverdata.database.DBPool;

/**
 * Class responsible for publishing snapshots on the website via Redis and a separate Report server.
 */
public class SnapshotRepository
{
	private static final String URL_PREFIX = "http://report.mineplex.com/chatsnap/view.php?token=";

	private static final int TOKEN_CHARS = 8;
	private static final SecureRandom SECURE_RANDOM = new SecureRandom();

	public static String getURL(String token)
	{
		return URL_PREFIX + token;
	}

	public static String generateToken()
	{
		// 6 bits per character, round to nearest byte
		int byteAmount = (int) Math.ceil((TOKEN_CHARS * 6) / 8.0);
		byte[] bytes = new byte[byteAmount];
		SECURE_RANDOM.nextBytes(bytes);

		String token = Base64.getUrlEncoder().encodeToString(bytes);
		token = replaceDashes(token);

		return token;
	}

	private static String replaceDashes(String token)
	{
		for (int i = 0; i < token.length(); i++)
		{
			char originalChar = token.charAt(i);
			char newChar = originalChar;

			while (newChar == '-')
			{
				byte[] replacementBytes = new byte[1];
				SECURE_RANDOM.nextBytes(replacementBytes);
				newChar = Base64.getUrlEncoder().encodeToString(replacementBytes).charAt(0);
			}

			token = token.replaceFirst(String.valueOf(originalChar), String.valueOf(newChar));
		}

		return token;
	}

	private static final String INSERT_SNAPSHOT = "INSERT INTO snapshots (token, creatorId) VALUES (?, ?);";
	private static final String INSERT_MESSAGE = "INSERT INTO snapshotMessages (senderId, `server`, `time`, message, snapshotType) VALUES (?, ?, ?, ?, ?);";
	private static final String INSERT_MESSAGE_RECIPIENT = "INSERT INTO snapshotRecipients (messageId, recipientId) VALUES (?, ?);";
	private static final String INSERT_MESSAGE_MAPPING = "INSERT INTO snapshotMessageMap (snapshotId, messageId) VALUES (?, ?);";
	private static final String GET_ID_FROM_TOKEN = "SELECT snapshots.id FROM snapshots WHERE snapshots.token = ?;";
	private static final String GET_METADATA = "SELECT token, creatorId FROM snapshots WHERE id = ?;";
	private static final String SET_TOKEN = "UPDATE snapshots SET token = ? WHERE id = ?;";
	private static final String GET_USER_SNAPSHOTS = "SELECT snapshots.id FROM snapshots WHERE snapshots.creatorId = ?;";

	private final String _serverName;
	private final Logger _logger;

	public SnapshotRepository(String serverName, Logger logger)
	{
		_serverName = serverName;
		_logger = logger;
	}

	public CompletableFuture<SnapshotMetadata> createSnapshot(Integer creatorAccountId)
	{
		return CompletableFuture.supplyAsync(() ->
		{
			try (Connection connection = DBPool.getAccount().getConnection())
			{
				return createSnapshot(connection, creatorAccountId);
			}
			catch (SQLException e)
			{
				throw new RuntimeException(e);
			}
		});
	}

	private SnapshotMetadata createSnapshot(Connection connection, Integer creatorAccount) throws SQLException
	{
		String token = getUnusedToken(connection);
		PreparedStatement insertSnapshotStatement = connection.prepareStatement(INSERT_SNAPSHOT, new String[]{"id"});
		insertSnapshotStatement.setString(1, token);

		if (creatorAccount != null)
		{
			insertSnapshotStatement.setInt(2, creatorAccount);
		}
		else
		{
			insertSnapshotStatement.setNull(2, Types.INTEGER);
		}

		insertSnapshotStatement.execute();

		try (ResultSet resultSet = insertSnapshotStatement.getGeneratedKeys())
		{
			if (resultSet.next())
			{
				return new SnapshotMetadata(resultSet.getInt(1), token, creatorAccount);
			}
			else
			{
				throw new IllegalStateException("Query did not return a snapshot id.");
			}
		}
	}

	private String getUnusedToken(Connection connection) throws SQLException
	{
		String token;

		do
		{
			token = generateToken();
		}
		while(getByToken(connection, token).isPresent());

		return token;
	}

	private Optional<Integer> getByToken(Connection connection, String token) throws SQLException
	{
		try (PreparedStatement statement = connection.prepareStatement(GET_ID_FROM_TOKEN))
		{
			statement.setString(1, token);

			try (ResultSet resultSet = statement.executeQuery())
			{
				if (resultSet.next())
				{
					int snapshotId = resultSet.getInt("id");
					return Optional.of(snapshotId);
				}
				else
				{
					return Optional.empty();
				}
			}
		}
	}

	public CompletableFuture<SnapshotMetadata> getSnapshotMetadata(Connection connection, int snapshotId)
	{
		CompletableFuture<SnapshotMetadata> future = CompletableFuture.supplyAsync(() ->
		{
			try
			{
				try (PreparedStatement statement = connection.prepareStatement(GET_METADATA))
				{
					statement.setInt(1, snapshotId);

					ResultSet resultSet = statement.executeQuery();
					if (resultSet.next())
					{
						String token = resultSet.getString("token");
						if (resultSet.wasNull())
						{
							// assign token to snapshot if it doesn't have one
							token = getUnusedToken(connection);
							setToken(connection, snapshotId, token);
						}

						Integer creatorId = resultSet.getInt("creatorId");
						if (resultSet.wasNull()) creatorId = null;

						return new SnapshotMetadata(snapshotId, token, creatorId);
					}
					else
					{
						return null;
					}
				}
			}
			catch (SQLException e)
			{
				throw new RuntimeException(e);
			}
		});

		future.exceptionally(throwable ->
		{
			_logger.log(Level.SEVERE, "Error whilst getting snapshot metadata for id: " + snapshotId, throwable);
			return null;
		});

		return future;
	}

	public CompletableFuture<List<Integer>> getUserSnapshots(int creatorId)
	{
		CompletableFuture<List<Integer>> future = CompletableFuture.supplyAsync(() ->
		{
			try (Connection connection = DBPool.getAccount().getConnection())
			{
				PreparedStatement preparedStatement = connection.prepareStatement(GET_USER_SNAPSHOTS);
				preparedStatement.setInt(1, creatorId);

				ResultSet resultSet = preparedStatement.executeQuery();
				List<Integer> snapshotIds = new ArrayList<>();

				while (resultSet.next())
				{
					snapshotIds.add(resultSet.getInt("id"));
				}

				return snapshotIds;
			}
			catch (SQLException e)
			{
				throw new RuntimeException(e);
			}
		});

		future.exceptionally(throwable ->
		{
			_logger.log(Level.SEVERE, "Error getting snapshots for user " + creatorId + ".");
			return new ArrayList<>();
		});

		return future;
	}

	private void setToken(Connection connection, int snapshotId, String token) throws SQLException
	{
		try (PreparedStatement setTokenStatement = connection.prepareStatement(SET_TOKEN))
		{
			setTokenStatement.setString(1, token);
			setTokenStatement.setInt(2, snapshotId);
			setTokenStatement.execute();
		}
	}

	public CompletableFuture<Void> insertMessages(int snapshotId, Collection<SnapshotMessage> messages)
	{
		CompletableFuture<Void> future = CompletableFuture.supplyAsync(() ->
		{
			try (Connection connection = DBPool.getAccount().getConnection())
			{
				try (PreparedStatement insertSnapshotStatement = connection.prepareStatement(INSERT_MESSAGE, new String[]{"id"}))
				{
					try (PreparedStatement insertRecipientStatement = connection.prepareStatement(INSERT_MESSAGE_RECIPIENT))
					{
						try (PreparedStatement insertMappingStatement = connection.prepareStatement(INSERT_MESSAGE_MAPPING))
						{
							for (SnapshotMessage message : messages)
							{
								try
								{
									insertMessage(insertSnapshotStatement, insertRecipientStatement, insertMappingStatement, snapshotId, message);
								}
								catch (Exception e)
								{
									_logger.log(Level.SEVERE, "Error inserting snapshot message.", e);
								}
							}

							insertRecipientStatement.executeBatch();
							insertMappingStatement.executeBatch();
							return null;
						}
					}
				}
			}
			catch (SQLException e)
			{
				throw new RuntimeException(e);
			}
		});

		future.exceptionally(throwable ->
		{
			_logger.log(Level.SEVERE, "Error whilst inserting messages into snapshot.", throwable);
			return null;
		});

		return future;
	}

	private void insertMessage(PreparedStatement insertSnapshotStatement, PreparedStatement insertRecipientStatement, PreparedStatement insertMappingStatement, int snapshotId, SnapshotMessage message) throws SQLException
	{
		boolean freshInsert = insertMessage(insertSnapshotStatement, message);

		long messageId = message.getId().orElseThrow(() ->
				new IllegalStateException("Message id not present (perhaps insert failed?)."));

		if (freshInsert)
		{
			insertRecipients(insertRecipientStatement, messageId, message.getRecipientIds());
		}

		if (!message.getLinkedSnapshots().contains(snapshotId))
		{
			insertMessageMapping(insertMappingStatement, snapshotId, messageId);
			message.addLinkedSnapshot(snapshotId);
		}
	}

	private boolean insertMessage(PreparedStatement insertSnapshotStatement, SnapshotMessage message) throws SQLException
	{
		Optional<Long> messageIdOptional = message.getId();
		boolean freshInsert = !messageIdOptional.isPresent();

		if (freshInsert)
		{
			insertSnapshotStatement.setInt(1, message.getSenderId());
			insertSnapshotStatement.setString(2, _serverName);
			insertSnapshotStatement.setTimestamp(3, UtilTime.toTimestamp(message.getSentTime()));
			insertSnapshotStatement.setString(4, message.getMessage());
			insertSnapshotStatement.setInt(5, message.getType().getId());
			insertSnapshotStatement.execute();

			try (ResultSet resultSet = insertSnapshotStatement.getGeneratedKeys())
			{
				if (resultSet.next())
				{
					message._id = resultSet.getLong(1);
				}
				else
				{
					throw new IllegalStateException("Query did not return a message id.");
				}
			}
		}

		return freshInsert;
	}

	private void insertRecipients(PreparedStatement insertRecipientStatement, long messageId, Collection<Integer> recipients) throws SQLException
	{
		for (int recipientId : recipients)
		{
			insertRecipientStatement.setLong(1, messageId);
			insertRecipientStatement.setInt(2, recipientId);
			insertRecipientStatement.addBatch();
		}

		insertRecipientStatement.executeBatch();
	}

	private void insertMessageMapping(PreparedStatement insertMappingStatement, int snapshotId, long messageId) throws SQLException
	{
		insertMappingStatement.setInt(1, snapshotId);
		insertMappingStatement.setLong(2, messageId);
		insertMappingStatement.execute();
	}
}
