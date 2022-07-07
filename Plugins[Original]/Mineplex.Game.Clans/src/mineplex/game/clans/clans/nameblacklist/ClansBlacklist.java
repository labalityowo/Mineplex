package mineplex.game.clans.clans.nameblacklist;

import java.util.List;

import org.bukkit.event.EventHandler;
import org.bukkit.plugin.java.JavaPlugin;

import mineplex.core.MiniPlugin;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.clans.clans.nameblacklist.commands.AddBlacklistCommand;
import mineplex.game.clans.clans.nameblacklist.repository.ClanNameBlacklistRepository;

public class ClansBlacklist extends MiniPlugin
{
	public enum Perm implements Permission
	{
		BLACKLIST_COMMAND,
	}

	private List<String> _blacklist;
	private ClanNameBlacklistRepository _repository;
	
	public ClansBlacklist(JavaPlugin plugin)
	{
		super("Clan Name Blacklist", plugin);

		_repository = new ClanNameBlacklistRepository(plugin, this);
		
		runAsync(() -> _repository.loadNames(this::setBlacklist));
		
		generatePermissions();
	}
	
	private void generatePermissions()
	{

		PermissionGroup.CMOD.setPermission(Perm.BLACKLIST_COMMAND, false, true);
		PermissionGroup.ADMIN.setPermission(Perm.BLACKLIST_COMMAND, true, true);
	}
	
	// Fetch new blacklisted clans every 16 seconds (in case someone blacklists a clan name on a different server)
	@EventHandler
	public void update(UpdateEvent event)
	{
		if (event.getType() == UpdateType.SLOWER)
		{
			runAsync(() -> _repository.loadNames(this::setBlacklist));
		}
	}
	
	private void setBlacklist(List<String> blacklist)
	{
		_blacklist = blacklist;
	}
	
	public boolean allowed(String name)
	{
		return !_blacklist.contains(name.toLowerCase());
	}
	
	public void add(String name)
	{
		_blacklist.add(name);
	}
	
	public void addCommands()
	{
		addCommand(new AddBlacklistCommand(this));
	}

	public ClanNameBlacklistRepository getRepository()
	{
		return _repository;
	}
}