package mineplex.core.titles;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.bukkit.entity.Player;

import mineplex.core.Managers;
import mineplex.core.account.CoreClientManager;
import mineplex.core.titles.tracks.Track;
import mineplex.serverdata.database.DBPool;
import mineplex.serverdata.database.RepositoryBase;

public class TitlesRepository extends RepositoryBase
{
	/*
		CREATE TABLE `accountTitle` (
		  `accountId` int(11) NOT NULL,
		  `trackName` VARCHAR(16) NOT NULL,
		  PRIMARY KEY (`accountId`),
		  CONSTRAINT `accountTitle_account` FOREIGN KEY (`accountId`) REFERENCES `accounts` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
		);
	 */
	private static final String UPSERT_TITLE = "INSERT INTO accountTitle (accountId, trackName) VALUES (?, ?) ON DUPLICATE KEY UPDATE trackName = ?;";
	private static final String DELETE_TITLE = "DELETE FROM accountTitle WHERE accountId = ?;";

	private final CoreClientManager _coreClientManager = Managers.require(CoreClientManager.class);

	public TitlesRepository()
	{
		super(DBPool.getAccount());
	}

	void savePlayerSelection(Player player, Track track)
	{
		int accountId = this._coreClientManager.getAccountId(player);
		if (accountId == -1)
		{
			System.err.print("Got account id -1 for player " + player.getName() + ". Aborting track saving");
			return;
		}
		try (Connection connection = getConnection())
		{
			if (track != null)
			{
				PreparedStatement statement = connection.prepareStatement(UPSERT_TITLE);
				statement.setInt(1, accountId);
				statement.setString(2, track.getId());
				statement.setString(3, track.getId());
				statement.executeUpdate();
			}
			else
			{
				PreparedStatement statement = connection.prepareStatement(DELETE_TITLE);
				statement.setInt(1, accountId);
				statement.executeUpdate();
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
}
