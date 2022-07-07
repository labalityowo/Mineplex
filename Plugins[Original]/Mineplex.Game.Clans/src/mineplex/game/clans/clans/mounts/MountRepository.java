package mineplex.game.clans.clans.mounts;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Consumer;

import org.bukkit.plugin.java.JavaPlugin;

import mineplex.core.common.Pair;
import mineplex.serverdata.Utility;
import mineplex.serverdata.database.DBPool;
import mineplex.serverdata.database.RepositoryBase;
import mineplex.serverdata.database.column.ColumnInt;
import mineplex.serverdata.database.column.ColumnVarChar;

/**
 * Database repository class for mounts
 */
public class MountRepository extends RepositoryBase
{
	private static final String CREATE_MOUNTS_TABLE = "CREATE TABLE IF NOT EXISTS accountClansMounts (id INT NOT NULL AUTO_INCREMENT,"
			+ "accountId INT NOT NULL,"
			+ "serverId INT NOT NULL,"
            + "mountTypeId INT NOT NULL,"
			+ "mountSkinId INT NOT NULL,"
            + "INDEX accountIndex (accountId),"
			+ "INDEX serverIndex (serverId),"
            + "INDEX accountServerIndex(accountId, serverId),"
            + "INDEX typeIndex (mountTypeId),"
			+ "INDEX skinIndex (mountSkinId),"
            + "PRIMARY KEY (id));";
	
	private static final String CREATE_MOUNT_STATS_TABLE = "CREATE TABLE IF NOT EXISTS clansMountStats (mountId INT NOT NULL,"
            + "statToken VARCHAR(20) NOT NULL,"
            + "PRIMARY KEY (mountId));";
	
	private static final String INSERT_MOUNT = "INSERT INTO accountClansMounts (accountId, serverId, mountTypeId, mountSkinId) VALUES (?, ?, ?, ?);";
	private static final String SAVE_MOUNT = "UPDATE accountClansMounts SET mountSkinId=? WHERE id=?;";
	private static final String SAVE_MOUNT_STATS = "INSERT INTO clansMountStats (mountId, statToken) VALUES (?, ?) ON DUPLICATE KEY UPDATE statToken=VALUES(statToken);";
	private static final String DELETE_MOUNT = "DELETE FROM accountClansMounts WHERE id=?;";
	private static final String DELETE_MOUNT_STATS = "DELETE FROM clansMountStats WHERE mountId=?;";
	
	private final int _serverId;
	private MountManager _mountManager;
	
	public MountRepository(JavaPlugin plugin, MountManager mountManager, int serverId)
	{
		super(DBPool.getAccount());
		
		_mountManager = mountManager;
		_serverId = serverId;
	}
	
	/**
	 * Saves a mount into the database
	 * @param accountId The owner's account id
	 * @param token The mount token to save
	 * @param statToken The stat token to save
	 */
	public void saveMount(final int accountId, final MountToken token, final MountStatToken statToken)
	{
		_mountManager.runAsync(() ->
		{
			try (Connection connection = getConnection();)
			{
				if (token.Id == -1)
				{
					executeInsert(connection, INSERT_MOUNT, idResult ->
					{
						if (idResult.next())
						{
							token.Id = idResult.getInt(1);
						}
					}, null, new ColumnInt("accountId", accountId), new ColumnInt("serverId", _serverId), new ColumnInt("mountTypeId", token.Type.getId()), new ColumnInt("mountSkinId", token.Skin == null ? -1 : token.Skin.getId()));
				}
				else
				{
					executeUpdate(connection, SAVE_MOUNT, null, new ColumnInt("mountSkinId", token.Skin == null ? -1 : token.Skin.getId()), new ColumnInt("id", token.Id));
				}
				if (token.Id == -1)
				{
					return;
				}
				executeUpdate(connection, SAVE_MOUNT_STATS, null, new ColumnInt("mountId", token.Id), new ColumnVarChar("statToken", 20, Utility.serialize(statToken)));
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
		});
	}
	
	/**
	 * Saves a list of mounts into the database
	 * @param accountId The owner's account id
	 * @param tokens The list of token pairs to save
	 */
	public void saveMounts(final int accountId, final List<Pair<MountToken, MountStatToken>> tokens)
	{
		_mountManager.runAsync(() ->
		{
			try (Connection connection = getConnection())
			{
				for (Pair<MountToken, MountStatToken> tokenPair : tokens)
				{
					MountToken token = tokenPair.getLeft();
					MountStatToken statToken = tokenPair.getRight();

					if (token.Id == -1)
					{
						executeInsert(connection, INSERT_MOUNT, idResult ->
						{
							if (idResult.next())
							{
								token.Id = idResult.getInt(1);
							}
						}, null, new ColumnInt("accountId", accountId), new ColumnInt("serverId", _serverId), new ColumnInt("mountTypeId", token.Type.getId()), new ColumnInt("mountSkinId", token.Skin == null ? -1 : token.Skin.getId()));
					}
					else
					{
						executeUpdate(connection, SAVE_MOUNT, null, new ColumnInt("mountSkinId", token.Skin == null ? -1 : token.Skin.getId()), new ColumnInt("id", token.Id));
					}
					if (token.Id == -1)
					{
						continue;
					}
					executeUpdate(connection, SAVE_MOUNT_STATS, null, new ColumnInt("mountId", token.Id), new ColumnVarChar("statToken", 100, Utility.serialize(statToken)));
				}
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
		});
	}
	
	/**
	 * Deletes a mount from the database
	 * @param token The mount to delete
	 */
	public void deleteMount(final MountToken token, final Consumer<Integer> after)
	{
		if (token.Id == -1)
		{
			return;
		}
		_mountManager.runAsync(() ->
		{
			executeUpdate(DELETE_MOUNT, new ColumnInt("id", token.Id));
			executeUpdate(DELETE_MOUNT_STATS, new ColumnInt("mountId", token.Id));
			if (after != null)
			{
				_mountManager.runSync(() ->
				{
					after.accept(token.Id);
				});
			}
		});
	}
	
	/**
	 * Deletes an array from the database
	 * @param ids The mount ids to delete
	 */
	public void deleteMounts(final int[] ids, final Runnable after)
	{
		if (ids.length <= 0)
		{
			return;
		}
		_mountManager.runAsync(() ->
		{
			String idList = ids[0] + "";
			for (int i = 1; i < ids.length; i++)
			{
				idList += ("," + ids[i]);
			}
			executeUpdate(DELETE_MOUNT.replace("id=?;", "id IN (" + idList + ");"));
			executeUpdate(DELETE_MOUNT_STATS.replace("mountId=?;", "mountId IN (" + idList + ");"));
			if (after != null)
			{
				_mountManager.runSync(after);
			}
		});
	}
}