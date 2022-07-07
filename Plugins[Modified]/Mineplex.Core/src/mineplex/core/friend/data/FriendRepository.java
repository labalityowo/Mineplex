package mineplex.core.friend.data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;

import mineplex.core.friend.FriendStatusType;
import mineplex.core.friend.FriendVisibility;
import mineplex.serverdata.Region;
import mineplex.serverdata.data.DataRepository;
import mineplex.serverdata.data.PlayerStatus;
import mineplex.serverdata.database.DBPool;
import mineplex.serverdata.database.RepositoryBase;
import mineplex.serverdata.database.column.ColumnBoolean;
import mineplex.serverdata.database.column.ColumnInt;
import mineplex.serverdata.database.column.ColumnVarChar;
import mineplex.serverdata.redis.RedisDataRepository;

public class FriendRepository extends RepositoryBase
{

	private static final String CREATE_FRIEND_TABLE = "CREATE TABLE IF NOT EXISTS accountFriend (id INT NOT NULL AUTO_INCREMENT, uuidSource VARCHAR(100), uuidTarget VARCHAR(100), status VARCHAR(100), PRIMARY KEY (id), UNIQUE INDEX uuidIndex (uuidSource, uuidTarget));";
	public static final String GET_VISIBILITY_QUERY = "(SELECT accountFriendData.status FROM accountFriendData WHERE accountId = tA.id) AS visibility";
	private static final String RETRIEVE_MULTIPLE_FRIEND_RECORDS = "SELECT uuidSource, tA.Name, status, tA.lastLogin, now(), uuidTarget, favourite, " + GET_VISIBILITY_QUERY + " FROM accountFriend INNER Join accounts AS fA ON fA.uuid = uuidSource INNER JOIN accounts AS tA ON tA.uuid = uuidTarget WHERE uuidSource IN ";
	private static final String ADD_FRIEND_RECORD = "INSERT INTO accountFriend (uuidSource, uuidTarget, status, created) SELECT fA.uuid AS uuidSource, tA.uuid AS uuidTarget, ?, now() FROM accounts as fA LEFT JOIN accounts AS tA ON tA.name = ? WHERE fA.name = ?;";
	private static final String UPDATE_MUTUAL_RECORD = "UPDATE accountFriend AS aF INNER JOIN accounts as fA ON aF.uuidSource = fA.uuid INNER JOIN accounts AS tA ON aF.uuidTarget = tA.uuid SET aF.status = ? WHERE tA.name = ? AND fA.name = ?;";
	private static final String DELETE_FRIEND_RECORD = "DELETE aF FROM accountFriend AS aF INNER JOIN accounts as fA ON aF.uuidSource = fA.uuid INNER JOIN accounts AS tA ON aF.uuidTarget = tA.uuid WHERE fA.name = ? AND tA.name = ?;";
	private static final String INSERT_VISIBILITY = "INSERT INTO accountFriendData VALUES (?,?);";
	private static final String UPDATE_VISIBILITY = "UPDATE accountFriendData SET status=? WHERE accountId=?;";
	private static final String UPDATE_FAVOURITE = "UPDATE accountFriend AS aF INNER JOIN accounts as fA ON aF.uuidSource = fA.uuid INNER JOIN accounts AS tA ON aF.uuidTarget = tA.uuid SET aF.favourite = ? WHERE tA.name = ? AND fA.name = ?;";

	// Repository holding active PlayerStatus data.
	private final DataRepository<PlayerStatus> _repository;

	public FriendRepository()
	{
		super(DBPool.getAccount());

		_repository = new RedisDataRepository<>(Region.currentRegion(), PlayerStatus.class, "playerStatus");
	}

	public boolean addFriend(final Player caller, String name)
	{
		int rowsAffected = executeUpdate(ADD_FRIEND_RECORD, new ColumnVarChar("status", 100, "Sent"), new ColumnVarChar("name", 100, name), new ColumnVarChar("name", 100, caller.getName()));

		if (rowsAffected > 0)
			return executeUpdate(ADD_FRIEND_RECORD, new ColumnVarChar("status", 100, "Pending"), new ColumnVarChar("name", 100, caller.getName()), new ColumnVarChar("uuid", 100, name)) > 0;

		return false;
	}

	public boolean updateFriend(String caller, String name, String status)
	{
		return executeUpdate(UPDATE_MUTUAL_RECORD, new ColumnVarChar("status", 100, status), new ColumnVarChar("uuid", 100, name), new ColumnVarChar("name", 100, caller)) > 0;
	}

	public boolean removeFriend(String caller, String name)
	{
		int rowsAffected = executeUpdate(DELETE_FRIEND_RECORD, new ColumnVarChar("name", 100, name), new ColumnVarChar("name", 100, caller));

		if (rowsAffected > 0)
			return executeUpdate(DELETE_FRIEND_RECORD, new ColumnVarChar("name", 100, caller), new ColumnVarChar("uuid", 100, name)) > 0;

		return false;
	}

	public Map<String, Set<FriendStatus>> getFriendsForAll(Player... players)
	{
		final Map<String, Set<FriendStatus>> friends = new HashMap<>();

		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(RETRIEVE_MULTIPLE_FRIEND_RECORDS).append("(");

		for (Player player : players)
		{
			stringBuilder
					.append("'")
					.append(player.getUniqueId())
					.append("', ");
		}

		stringBuilder.delete(stringBuilder.length() - 2, stringBuilder.length());
		stringBuilder.append(");");

		executeQuery(stringBuilder.toString(), resultSet ->
		{
			while (resultSet.next())
			{
				FriendStatus friend = new FriendStatus();

				String uuidSource = resultSet.getString(1);
				friend.Name = resultSet.getString(2);
				friend.Status = FriendStatusType.valueOf(resultSet.getString(3));
				friend.LastSeenOnline = resultSet.getTimestamp(5).getTime() - resultSet.getTimestamp(4).getTime();
				friend.UUID = UUID.fromString(resultSet.getString(6));
				friend.Visibility = FriendVisibility.values()[resultSet.getByte("visibility")];
				friend.Favourite = resultSet.getBoolean("favourite");

				Set<FriendStatus> statuses = friends.computeIfAbsent(uuidSource, k -> new HashSet<>());
				statuses.removeIf(other -> friend.Name.equals(other.Name) && friend.LastSeenOnline < other.LastSeenOnline);
				statuses.add(friend);
			}

			// Load the server status of friends for all sources.
			friends.values().forEach(this::loadFriendStatuses);
		});

		return friends;
	}

	public List<FriendStatus> loadClientInformation(ResultSet resultSet) throws SQLException
	{
		List<FriendStatus> statuses = new LinkedList<>();

		while (resultSet.next())
		{
			FriendStatus friend = new FriendStatus();

			friend.Name = resultSet.getString(1);
			friend.Status = FriendStatusType.valueOf(resultSet.getString(2));
			friend.LastSeenOnline = resultSet.getTimestamp(4).getTime() - resultSet.getTimestamp(3).getTime();
			friend.UUID = UUID.fromString(resultSet.getString(5));
			friend.Visibility = FriendVisibility.values()[resultSet.getByte("visibility")];
			friend.Favourite = resultSet.getBoolean("favourite");
			statuses.add(friend);
		}

		loadFriendStatuses(statuses);

		return statuses;
	}

	public void loadFriendStatuses(Collection<FriendStatus> statuses)
	{
		// Generate a set of all friend names
		Set<String> friendUUIDS = statuses.stream()
				.map(status -> status.UUID.toString())
				.collect(Collectors.toSet());

		// Load player statuses into a mapping
		Map<UUID, PlayerStatus> playerStatuses = _repository.getElements(friendUUIDS).stream()
				.collect(Collectors.toMap(PlayerStatus::getUUID, Function.identity()));

		// Load status information into friend data.
		for (FriendStatus friend : statuses)
		{
			PlayerStatus status = playerStatuses.get(friend.UUID);
			friend.Online = (status != null);
			friend.ServerName = friend.Online ? status.getServer() : null;
		}
	}

	public void updatePlayerStatus(UUID playerUUID, PlayerStatus status)
	{
		if (status != null)
		{
			_repository.addElement(status, 60 * 60 * 8);
		}
		else
		{
			_repository.removeElement(playerUUID.toString());
		}
	}

	public boolean updateVisibility(int accountId, FriendVisibility visibility)
	{
		ColumnInt accountIdColumn = new ColumnInt("accountId", accountId), visibilityColumn = new ColumnInt("status", visibility.ordinal());

		if (executeUpdate(UPDATE_VISIBILITY, visibilityColumn, accountIdColumn) > 0)
		{
			return true;
		}

		return executeInsert(INSERT_VISIBILITY, null, accountIdColumn, visibilityColumn) > 0;
	}

	public boolean updateFavourite(String caller, String target, boolean favourite)
	{
		return executeUpdate(UPDATE_FAVOURITE, new ColumnBoolean("favourite", favourite), new ColumnVarChar("name", 100, target), new ColumnVarChar("name", 100, caller)) > 0;
	}

}
