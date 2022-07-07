package mineplex.game.clans.clans.invsee;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import mineplex.core.MiniPlugin;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.common.util.UtilServer;
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.clans.invsee.commands.InvseeCommand;
import mineplex.game.clans.clans.invsee.ui.InvseeInventory;

public class InvseeManager extends MiniPlugin
{
	public enum Perm implements Permission
	{
		INVSEE_COMMAND,
	}

	private final Map<UUID, InvseeInventory> _viewing = new HashMap<>();

	public InvseeManager(ClansManager manager)
	{
		super("Invsee Manager", manager.getPlugin());
		
		generatePermissions();
	}
	
	private void generatePermissions()
	{

		PermissionGroup.CMOD.setPermission(Perm.INVSEE_COMMAND, false, true);
		PermissionGroup.ADMIN.setPermission(Perm.INVSEE_COMMAND, true, true);
	}

	@Override
	public void addCommands()
	{
		addCommand(new InvseeCommand(this));
	}

	public void doInvsee(OfflinePlayer target, Player requester)
	{
		InvseeInventory invseeInventory = _viewing.computeIfAbsent(target.getUniqueId(), key -> new InvseeInventory(this, target));
		invseeInventory.addAndShowViewer(requester);
	}

	public boolean isBeingInvseen(OfflinePlayer player)
	{
		return _viewing.containsKey(player.getUniqueId());
	}

	public boolean isInvseeing(Player player)
	{
		for (InvseeInventory invseeInventory : _viewing.values())
		{
			if (invseeInventory.isViewer(player))
			{
				return true;
			}
		}
		return false;
	}

	public void close(UUID target)
	{
		InvseeInventory invseeInventory = _viewing.remove(target);
		if (invseeInventory == null)
		{
			log("Expected non-null inventory when closing " + target);
			return;
		}
		UtilServer.Unregister(invseeInventory);
	}
}