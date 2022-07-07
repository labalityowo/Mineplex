package mineplex.core.progression;

import mineplex.core.common.util.UtilServer;
import mineplex.core.database.MinecraftRepository;
import mineplex.core.progression.data.PlayerKit;
import mineplex.core.progression.util.SQLStatement;
import mineplex.serverdata.database.DBPool;
import mineplex.serverdata.database.RepositoryBase;

import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

/**
 * Handles all things database related.
 */
public class KitProgressionRepository extends RepositoryBase
{

	private final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS `kitProgression` (" +
	  "`uuid` VARCHAR(36), " +
	  "`kitId` VARCHAR(64), " +
	  "`level` INT, " +
	  "`xp` INT, " +
	  "`upgrade_level` INT, " +
	  "`default` TINYINT, PRIMARY KEY(uuid, kitId))";
	private final String INSERT_OR_UPDATE = "INSERT INTO `kitProgression` VALUES(?,?,?,?,?,?) ON DUPLICATE KEY UPDATE " +
	  "`level` = ?, `xp` = ?, `upgrade_level` = ?, `default` = ?";
	private final String QUERY_KITS = "SELECT * FROM `kitProgression` WHERE `uuid` = ?;";

	private KitProgressionManager _kitProgressionManager;

	public KitProgressionRepository(KitProgressionManager plugin)
	{
		super(DBPool.getAccount());
		_kitProgressionManager = plugin;
	}

	/**
	 * Inserts data for a kit into the database
	 * This will update the info if the kit is already in there
	 *
	 * @param playerKit The reference to the Player Kit
	 * @param kit       The kit's INTERNAL name
	 */
	public void insertOrUpdate(PlayerKit playerKit, String kit)
	{
		async(() -> {
			int level = playerKit.getLevel(kit);
			int upgradeLevel = playerKit.getUpgradeLevel(kit);
			int xp = playerKit.getXp(kit);
			int defaultType = playerKit.isDefault(kit) ? 1 : 0;
			Connection connection = getConnection();
			try
			{
				PreparedStatement statement = new SQLStatement(INSERT_OR_UPDATE)
				  .set(1, playerKit.getUuid())
				  .set(2, kit)
				  .set(3, level)
				  .set(4, xp)
				  .set(5, upgradeLevel)
				  .set(6, defaultType)
				  .set(7, level)
				  .set(8, xp)
				  .set(9, upgradeLevel)
				  .set(10, defaultType)
				  .prepare(connection);

				int effect = executeUpdate(statement);

				if (effect == -1)
				{
					//Something went wrong uh oh
					_kitProgressionManager.getPlugin().getLogger().severe("Inserting new Kit Data for " + playerKit.getUuid() + " failed!");
				}
			} catch (SQLException e)
			{
				e.printStackTrace();
			} finally
			{
				if (connection != null)
				{
					try
					{
						connection.close();
					} catch (SQLException e)
					{
						e.printStackTrace();
					}
				}
			}
		});

	}

	/**
	 * Update whether or not the specified kit is a default kit
	 *
	 * @param playerKit The player's kit data object
	 * @param kit       The INTERNAL name of the kit
	 * @param def       The integer representing the boolean value (1 for true 0 for false)
	 */
	public void updateDefault(PlayerKit playerKit, String kit, int def)
	{
		async(() -> {
			Connection connection = getConnection();
			String update = "UPDATE `kitProgression` SET `default` = ? " + "WHERE `uuid` = ? AND`kitId` = ?";

			try
			{
				PreparedStatement statement = new SQLStatement(update)
				  .set(1, def)
				  .set(2, playerKit.getUuid())
				  .set(3, kit)
				  .prepare(connection);
				int effect = executeUpdate(statement);
				if (effect == -1)
				{
					_kitProgressionManager.getPlugin().getLogger().severe("Updating default value for" + playerKit.getUuid().toString() + "'s kit failed!");
				}
			} catch (SQLException e)
			{
				e.printStackTrace();
			} finally
			{
				if (connection != null)
				{
					try
					{
						connection.close();
					} catch (SQLException e)
					{
						e.printStackTrace();
					}
				}
			}
		});
	}

	/**
	 * Possibly remove
	 * @param uuid
	 */
	public void loadInfoSync(UUID uuid)
	{
		PlayerKit playerKit = new PlayerKit(uuid);
		try (Connection connection = getConnection())
		{
			PreparedStatement preparedStatement = new SQLStatement(QUERY_KITS).set(1, uuid).prepare(connection);
			ResultSet resultSet = preparedStatement.executeQuery();
			while (resultSet.next())
			{
				String kitId = resultSet.getString("kitId");

				int level = resultSet.getInt("level");
				int xp = resultSet.getInt("xp");
				int upgradeLevel = resultSet.getInt("upgrade_level");
				boolean def = resultSet.getInt("default") == 1;

				playerKit.setLevel(level, kitId);
				playerKit.setXp(xp, kitId);
				playerKit.setUpgradeLevel(upgradeLevel, kitId);
				playerKit.setDefault(def, kitId);
			}
		} catch (SQLException e)
		{
			e.printStackTrace();
		}

		_kitProgressionManager.getDataManager().add(playerKit);
	}

	@SuppressWarnings("Duplicates")
	public void getInfo(UUID uuid)
	{
		async(() -> {
			PlayerKit playerKit = new PlayerKit(uuid);
			try (Connection connection = getConnection())
			{
				PreparedStatement preparedStatement = new SQLStatement(QUERY_KITS).set(1, uuid).prepare(connection);
				ResultSet resultSet = preparedStatement.executeQuery();
				while (resultSet.next())
				{
					String kitId = resultSet.getString("kitId");

					int level = resultSet.getInt("level");
					int xp = resultSet.getInt("xp");
					int upgradeLevel = resultSet.getInt("upgrade_level");
					boolean def = resultSet.getInt("default") == 1;

					playerKit.setLevel(level, kitId);
					playerKit.setXp(xp, kitId);
					playerKit.setUpgradeLevel(upgradeLevel, kitId);
					playerKit.setDefault(def, kitId);
				}
			} catch (SQLException e)
			{
				e.printStackTrace();
			}

			_kitProgressionManager.getDataManager().add(playerKit);
		});
	}


	/**
	 * Internal method for updating the table
	 *
	 * @param preparedStatement The statement to execute
	 * @return The amount of rows effected
	 */
	private int executeUpdate(PreparedStatement preparedStatement)
	{
		try
		{
			return preparedStatement.executeUpdate();
		} catch (SQLException e)
		{
			e.printStackTrace();
			return -1;
		} finally
		{
			try
			{
				if (preparedStatement != null)
				{
					preparedStatement.close();
				}
			} catch (SQLException e)
			{
				e.printStackTrace();
			}
		}
	}

	private void async(Runnable runnable)
	{
		UtilServer.runAsync(runnable);
	}

}
