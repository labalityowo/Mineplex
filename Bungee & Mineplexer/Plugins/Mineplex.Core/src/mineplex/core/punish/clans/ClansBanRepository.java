package mineplex.core.punish.clans;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.bukkit.plugin.java.JavaPlugin;

import mineplex.core.Managers;
import mineplex.core.account.CoreClientManager;
import mineplex.core.common.util.UtilServer;
import mineplex.serverdata.database.DBPool;
import mineplex.serverdata.database.RepositoryBase;
import mineplex.serverdata.database.column.ColumnInt;
import mineplex.serverdata.database.column.ColumnVarChar;

public class ClansBanRepository extends RepositoryBase
{
	private static final String BAN_PLAYER = "INSERT INTO clanBans (uuid, admin, reason, banTime, unbanTime, permanent, removed, removeAdmin, removeReason) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);";
	private static final String REMOVE_BAN = "UPDATE clanBans SET removed = 1, removeAdmin = ?, removeReason = ? WHERE id = ?;";
	private static final String GET_ALL_BANS = "SELECT * FROM clanBans WHERE uuid = ?;";
	
	public ClansBanRepository(JavaPlugin plugin)
	{
		super(DBPool.getAccount());
	}

	public CompletableFuture<Optional<ClansBan>> ban(UUID uuid, String admin, long time, String reason)
	{
		return CompletableFuture.supplyAsync(() ->
		{
			try (Connection conn = DBPool.getAccount().getConnection())
			{
				Timestamp banTime = new Timestamp(System.currentTimeMillis());
				Timestamp unbanTime = new Timestamp(System.currentTimeMillis() + time);

				PreparedStatement stmt = conn.prepareStatement(BAN_PLAYER, Statement.RETURN_GENERATED_KEYS);
				stmt.setString(1, uuid.toString());
				stmt.setString(2, admin);
				stmt.setString(3, reason);
				stmt.setTimestamp(4, banTime);
				stmt.setTimestamp(5, unbanTime);
				stmt.setBoolean(6, time == -1);
				stmt.setBoolean(7, false);
				stmt.setString(8, null);
				stmt.setString(9, null);
				stmt.executeUpdate();

				ResultSet resultSet = stmt.getGeneratedKeys();
				if (resultSet.next())
				{
					int id = resultSet.getInt(1);
					return Optional.of(new ClansBan(id, uuid, admin, reason, banTime, unbanTime, time == -1, false, null, null));
				}
				else
				{
					return Optional.empty();
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
				return Optional.empty();
			}
		});
	}

	public CompletableFuture<ClansBanClient> loadClient(UUID uuid)
	{
		return CompletableFuture.supplyAsync(() ->
		{
			try (Connection conn = DBPool.getAccount().getConnection())
			{
				PreparedStatement stmt = conn.prepareStatement(GET_ALL_BANS);
				stmt.setString(1, uuid.toString());

				List<ClansBan> bans = new ArrayList<>();

				ResultSet resultSet = stmt.executeQuery();
				while (resultSet.next())
				{
					int id = resultSet.getInt(1);
					String ruuid = resultSet.getString(2);
					String admin = resultSet.getString(3);
					String reason = resultSet.getString(4);
					Timestamp banTime = resultSet.getTimestamp(5);
					Timestamp unbanTime = resultSet.getTimestamp(6);
					boolean permanent = resultSet.getBoolean(7);
					boolean removed = resultSet.getBoolean(8);
					String removeAdmin = resultSet.getString(9);
					String removeReason = resultSet.getString(10);

					bans.add(new ClansBan(id, UUID.fromString(ruuid), admin, reason, banTime, unbanTime, permanent, removed, removeAdmin, removeReason));
				}

				return new ClansBanClient(uuid, bans);
			}
			catch (Exception e)
			{
				e.printStackTrace();
				return new ClansBanClient(uuid, new ArrayList<>());
			}
		});
	}

	public CompletableFuture<Optional<ClansBanClient>> loadClient(String name)
	{
		// Yes, this is garbage.
		// Yes, it would be better implemented in a functional language.
		return CompletableFuture.supplyAsync(() -> Managers.get(CoreClientManager.class).loadUUIDFromDB(name)).thenCompose(uuid ->
		{
			if (uuid == null)
			{
				CompletableFuture<Optional<ClansBanClient>> future = new CompletableFuture<>();
				future.complete(Optional.empty());
				return future;
			}
			else
			{
				return loadClient(uuid).thenApply(Optional::of);
			}
		});
	}
	
	public void removeBan(ClansBan ban, String admin, String reason, Runnable onComplete)
	{
		UtilServer.runAsync(() ->
		{
			executeUpdate(REMOVE_BAN, new ColumnVarChar("removeAdmin", admin.length(), admin), new ColumnVarChar("removeReason", reason.length(), reason), new ColumnInt("id", ban.getId()));
			
			if (onComplete != null)
			{
				UtilServer.runSync(onComplete);
			}
		});
	}
}