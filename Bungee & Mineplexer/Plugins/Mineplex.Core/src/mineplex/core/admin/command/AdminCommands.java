package mineplex.core.admin.command;

import mineplex.core.MiniPlugin;
import mineplex.core.account.CoreClientManager;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;

public class AdminCommands extends MiniPlugin
{
	private CoreClientManager _coreClientManager;

	public enum Perm implements Permission
	{
		SEEN_COMMAND
	}

	public AdminCommands()
	{
		super("Staff");

		_coreClientManager = require(CoreClientManager.class);

		generatePermissions();
	}

	public CoreClientManager getCoreClientManager()
	{
		return _coreClientManager;
	}

	public void generatePermissions()
	{
		PermissionGroup.TRAINEE.setPermission(Perm.SEEN_COMMAND, true, true);
	}

	@Override
	public void addCommands()
	{
		addCommand(new SeenCommand(this));
	}
}
