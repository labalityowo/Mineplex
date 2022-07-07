package mineplex.core.youtube;

import mineplex.serverdata.database.DBPool;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class YoutubeRepository
{
	private final YoutubeManager _manager;

	public YoutubeRepository(YoutubeManager manager)
	{
		_manager = manager;
	}

	public void attemptYoutube(Player player, YoutubeClient client, Runnable runnable)
	{
		int accountId = _manager.getClientManager().Get(player).getAccountId();

		Bukkit.getScheduler().runTaskAsynchronously(_manager.getPlugin(), () ->
		{
			try (Connection connection = DBPool.getAccount().getConnection())
			{
				PreparedStatement statement = connection.prepareStatement("REPLACE INTO youtube (accountId, clicktime) VALUES (?, ?)");
				statement.setInt(1, accountId);
				statement.setDate(2, Date.valueOf(client.getClickDate()));
				statement.executeUpdate();

				runnable.run();
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
		});
	}
	
	public void attemptSpecificYoutube(Player player, YoutubeClient client, Runnable runnable)
	{
		int accountId = _manager.getClientManager().Get(player).getAccountId();

		Bukkit.getScheduler().runTaskAsynchronously(_manager.getPlugin(), () ->
		{
			try (Connection connection = DBPool.getAccount().getConnection())
			{
				PreparedStatement statement = connection.prepareStatement("REPLACE INTO specificYoutube (accountId, clicktime) VALUES (?, ?)");
				statement.setInt(1, accountId);
				statement.setDate(2, Date.valueOf(client.getSpecificDate()));
				statement.executeUpdate();

				runnable.run();
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
		});
	}
}