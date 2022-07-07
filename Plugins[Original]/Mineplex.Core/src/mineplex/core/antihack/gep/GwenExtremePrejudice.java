package mineplex.core.antihack.gep;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

import mineplex.core.MiniPlugin;
import mineplex.serverdata.database.DBPool;

public class GwenExtremePrejudice extends MiniPlugin
{
	private final List<String> _addresses = new ArrayList<>();
	
	@SuppressWarnings("deprecation")
	public GwenExtremePrejudice(JavaPlugin plugin)
	{
		super("GEP", plugin);
		
		int ticks = 20 * 5 * 60;
		int offset = 20 * (int)Math.floor(Math.random() * 10) * 60;
		ticks += offset;
		
		Bukkit.getScheduler().scheduleAsyncRepeatingTask(plugin, () ->
		{
			refreshIPList();
		}, 0, ticks);
	}
	
	private void refreshIPList()
	{
		try (Connection c = DBPool.getAccount().getConnection())
		{
			List<String> addresses = new ArrayList<>();
			ResultSet rs = c.prepareStatement("SELECT ipAddress FROM gepAddresses;").executeQuery();
			while (rs.next())
			{
				addresses.add(rs.getString(1));
			}
			runSync(() ->
			{
				for (Player player : Bukkit.getOnlinePlayers())
				{
					if (addresses.contains(player.getAddress().getAddress().getHostAddress()))
					{
						player.removeMetadata("GWENEXTREMEPREJUDICE", getPlugin());
						player.setMetadata("GWENEXTREMEPREJUDICE", new FixedMetadataValue(getPlugin(), true));
					}
					else
					{
						player.removeMetadata("GWENEXTREMEPREJUDICE", getPlugin());
						player.setMetadata("GWENEXTREMEPREJUDICE", new FixedMetadataValue(getPlugin(), false));
					}
				}
				_addresses.clear();
				_addresses.addAll(addresses);
			});
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent event)
	{
		Player player = event.getPlayer();
		if (!_addresses.isEmpty() && _addresses.contains(player.getAddress().getAddress().getHostAddress()))
		{
			player.removeMetadata("GWENEXTREMEPREJUDICE", getPlugin());
			player.setMetadata("GWENEXTREMEPREJUDICE", new FixedMetadataValue(getPlugin(), true));
		}
		else
		{
			player.setMetadata("GWENEXTREMEPREJUDICE", new FixedMetadataValue(getPlugin(), false));
		}
	}
}