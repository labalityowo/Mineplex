package mineplex.hub.modules.salesannouncements;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.account.permissions.PermissionGroupHelper;
import mineplex.core.common.util.Callback;
import mineplex.serverdata.database.DBPool;
import mineplex.serverdata.database.RepositoryBase;
import mineplex.serverdata.database.column.ColumnBoolean;
import mineplex.serverdata.database.column.ColumnInt;
import mineplex.serverdata.database.column.ColumnVarChar;

public class SalesAnnouncementRepository extends RepositoryBase
{
	private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS salesAnnouncements (id INT NOT NULL AUTO_INCREMENT, ranks VARCHAR(250), message VARCHAR(256), enabled BOOL, clans BOOL, PRIMARY KEY (id), INDEX typeIndex (clans));";

	private static final String GET_ANNOUNCEMENTS = "SELECT * FROM salesAnnouncements WHERE clans=?;";
	private static final String GET_ANNOUNCEMENT = "SELECT * FROM salesAnnouncements WHERE id=?;";
	private static final String UPDATE_ANNOUNCEMENT_STATUS = "UPDATE salesAnnouncements SET enabled=? WHERE id=?;";
	private static final String INSERT_ANNOUNCEMENT = "INSERT INTO salesAnnouncements (ranks, message, enabled, clans) VALUES(?, ?, ?, ?);";
	private static final String DELETE_ANNOUNCEMENT = "DELETE FROM salesAnnouncements WHERE id=?;";

	private final JavaPlugin _plugin;
	private final boolean _clans;

	public SalesAnnouncementRepository(JavaPlugin plugin, boolean clans)
	{
		super(DBPool.getAccount());
		_plugin = plugin;
		_clans = clans;
	}

	private void runAsync(Runnable runnable)
	{
		Bukkit.getScheduler().runTaskAsynchronously(_plugin, runnable);
	}
	
	private void runSync(Runnable runnable)
	{
		Bukkit.getScheduler().runTask(_plugin, runnable);
	}
	
	public void loadAnnouncements(final Map<Integer, SalesAnnouncementData> map)
	{
		runAsync(() ->
		{
			executeQuery(GET_ANNOUNCEMENTS, resultSet ->
			{
				final List<SalesAnnouncementData> data = new ArrayList<>();
				while (resultSet.next())
				{
					int id = resultSet.getInt("id");
					String rankString = resultSet.getString("ranks");
					List<PermissionGroup> ranks = new ArrayList<>();
					if (rankString.contains(",") && !rankString.startsWith(",") && !rankString.endsWith(","))
					{
						for (String rankStr : rankString.split(","))
						{
							PermissionGroup group = PermissionGroupHelper.getGroupFromLegacy(rankStr);
							ranks.add(group);
						}
					}
					else
					{
						PermissionGroup group = PermissionGroupHelper.getGroupFromLegacy(rankString);
						ranks.add(group);
					}
					PermissionGroup[] displayTo = ranks.toArray(new PermissionGroup[ranks.size()]);
					String message = resultSet.getString("message");
					boolean enabled = resultSet.getBoolean("enabled");
					
					data.add(new SalesAnnouncementData(id, displayTo, message, enabled));
				}
				
				runSync(() ->
				{
					map.clear();
					data.forEach(sData -> map.put(sData.getId(), sData));
				});
			}, new ColumnBoolean("clans", _clans));
		});
	}
	
	public void loadAnnouncement(final int id, final Callback<SalesAnnouncementData> callback)
	{
		runAsync(() ->
		{
			executeQuery(GET_ANNOUNCEMENT, resultSet ->
			{
				if (resultSet.next())
				{
					int aId = resultSet.getInt("id");
					String rankString = resultSet.getString("ranks");
					List<PermissionGroup> ranks = new ArrayList<>();
					if (rankString.contains(",") && !rankString.startsWith(",") && !rankString.endsWith(","))
					{
						for (String rankStr : rankString.split(","))
						{
							PermissionGroup group = PermissionGroupHelper.getGroupFromLegacy(rankStr);
							ranks.add(group);
						}
					}
					else
					{
						PermissionGroup group = PermissionGroupHelper.getGroupFromLegacy(rankString);
						ranks.add(group);
					}
					PermissionGroup[] displayTo = ranks.toArray(new PermissionGroup[ranks.size()]);
					String message = resultSet.getString("message");
					boolean enabled = resultSet.getBoolean("enabled");
					
					final SalesAnnouncementData data = new SalesAnnouncementData(aId, displayTo, message, enabled);
					runSync(() ->
					{
						callback.run(data);
					});
				}
			}, new ColumnInt("id", id));
		});
	}
	
	public void createAnnouncement(final PermissionGroup[] displayTo, final String message, Callback<SalesAnnouncementData> callback)
	{
		runAsync(() ->
		{
			StringBuilder rankStr = new StringBuilder(displayTo[0].name());
			for (int i = 1; i < displayTo.length; i++)
			{
				rankStr.append(",").append(displayTo[i].name());
			}
			executeInsert(INSERT_ANNOUNCEMENT, resultSet ->
			{
				if (resultSet.next())
				{
					int id = resultSet.getInt(1);
					final SalesAnnouncementData data = new SalesAnnouncementData(id, displayTo, message, true);
					if (callback != null)
					{
						runSync(() -> callback.run(data));
					}
				}
			}, new ColumnVarChar("ranks", 250, rankStr.toString()), new ColumnVarChar("message", 256, message), new ColumnBoolean("enabled", true), new ColumnBoolean("clans", _clans));
		});
	}
	
	public void updateAnnouncementStatus(SalesAnnouncementData data, Runnable after)
	{
		runAsync(() ->
		{
			executeUpdate(UPDATE_ANNOUNCEMENT_STATUS, new ColumnBoolean("enabled", data.isEnabled()), new ColumnInt("id", data.getId()));
			if (after != null)
			{
				runSync(after);
			}
		});
	}
	
	public void deleteAnnouncement(SalesAnnouncementData data, Runnable after)
	{
		runAsync(() ->
		{
			executeUpdate(DELETE_ANNOUNCEMENT, new ColumnInt("id", data.getId()));
			if (after != null)
			{
				runSync(after);
			}
		});
	}

	@Override
	protected void initialize() {}

	@Override
	protected void update() {}
}