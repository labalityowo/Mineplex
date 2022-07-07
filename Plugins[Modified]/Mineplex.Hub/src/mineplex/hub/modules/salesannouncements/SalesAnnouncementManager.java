package mineplex.hub.modules.salesannouncements;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import mineplex.core.Managers;
import mineplex.core.MiniPlugin;
import mineplex.core.account.CoreClientManager;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.serverdata.commands.ServerCommandManager;

public class SalesAnnouncementManager extends MiniPlugin
{
	public enum Perm implements Permission
	{
		SALES_COMMAND,
	}

	private static final String LINE = C.cDGreenB + C.Strike + "=============================================";
	private final Map<Integer, SalesAnnouncementData> _data = new HashMap<>();
	private final SalesAnnouncementRepository _repo;
	public final boolean CLANS = false;
	
	public SalesAnnouncementManager(JavaPlugin plugin)
	{
		super("Sales", plugin);
		_repo = new SalesAnnouncementRepository(plugin, CLANS);
		_repo.loadAnnouncements(_data);
		
		addCommand(new SalesAnnouncementCommand(this));
		
		ServerCommandManager.getInstance().registerCommandType(SalesAnnouncementUpdateCommand.class, new SalesAnnouncementUpdateHandler(this));
		ServerCommandManager.getInstance().registerCommandType(SalesAnnouncementDeleteCommand.class, new SalesAnnouncementDeleteHandler(this));
		
		generatePermissions();
	}
	
	private void generatePermissions()
	{

		PermissionGroup.ADMIN.setPermission(Perm.SALES_COMMAND, true, true);
	}
	
	public Map<Integer, SalesAnnouncementData> getLoadedAnnouncements()
	{
		return _data;
	}
	
	public String getServer()
	{
		return getPlugin().getConfig().getString("serverstatus.name");
	}
	
	public void createAnnouncement(Player creator, PermissionGroup[] displayTo, String message)
	{
		if (_data.size() >= 9 * 6)
		{
			UtilPlayer.message(creator, F.main(getName(), "There are too many existing Sales Announcements to create a new one! Try deleting some!"));
			return;
		}
		_repo.createAnnouncement(displayTo, message, data ->
		{
			UtilPlayer.message(creator, F.main(getName(), "Announcement successfully created!"));
			_data.put(data.getId(), data);
			new SalesAnnouncementUpdateCommand(data.getId(), getServer(), CLANS).publish();
		});
	}
	
	public void deleteAnnouncement(Player deletor, SalesAnnouncementData data, boolean forceRemoveFromList)
	{
		if (forceRemoveFromList)
		{
			_data.remove(data.getId());
		}
		_repo.deleteAnnouncement(data, () ->
		{
			UtilPlayer.message(deletor, F.main(getName(), "Successfully deleted announcement!"));
			if (!forceRemoveFromList)
			{
				_data.remove(data.getId());
			}
			new SalesAnnouncementDeleteCommand(data.getId(), getServer(), CLANS).publish();
		});
	}
	
	public void toggleAnnouncement(Player toggler, SalesAnnouncementData data)
	{
		data.setEnabled(!data.isEnabled());
		_repo.updateAnnouncementStatus(data, () ->
		{
			UtilPlayer.message(toggler, F.main(getName(), "Successfully toggled announcement!"));
			new SalesAnnouncementUpdateCommand(data.getId(), getServer(), CLANS).publish();
		});
	}
	
	public void handleRemoteDeletion(int id)
	{
		runSync(() ->
		{
			_data.remove(id);
			UtilServer.CallEvent(new SalesAnnouncementRemoteListUpdateEvent());
		});
	}
	
	public void handleRemoteUpdate(int id)
	{
		runSync(() ->
		{
			if (_data.containsKey(id))
			{
				_repo.loadAnnouncement(id, data ->
				{
					_data.get(data.getId()).setEnabled(data.isEnabled());
					UtilServer.CallEvent(new SalesAnnouncementRemoteListUpdateEvent());
				});
			}
			else
			{
				_repo.loadAnnouncement(id, data ->
				{
					_data.put(data.getId(), data);
					UtilServer.CallEvent(new SalesAnnouncementRemoteListUpdateEvent());
				});
			}
		});
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent event)
	{
		if (_data.isEmpty() || !_data.values().stream().filter(data -> data.isEnabled()).findAny().isPresent())
		{
			return;
		}
		Player player = event.getPlayer();
		PermissionGroup rank = Managers.get(CoreClientManager.class).Get(player).getPrimaryGroup();
		
		runSyncLater(() ->
		{
			_data.values().stream().filter(data -> data.isEnabled() && data.shouldDisplayTo(rank)).forEach(data ->
			{
				player.sendMessage(" ");
				player.sendMessage(LINE);
				player.sendMessage(" ");
				player.sendMessage(data.getMessage(false));
				player.sendMessage(" ");
				player.sendMessage(LINE);
				player.sendMessage(" ");
			});
		}, 5L);
	}
}