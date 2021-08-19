package nautilus.game.arcade.game;

import org.bukkit.entity.Player;

import mineplex.core.Managers;
import mineplex.core.account.permissions.Permission;
import mineplex.core.command.CommandBase;
import nautilus.game.arcade.ArcadeManager;

public class DebugCommand extends CommandBase<ArcadeManager>
{
	private final DebugCommandExecutor _executor;
	
	public DebugCommand(String commandName, Permission permission, DebugCommandExecutor executor)
	{
		super(Managers.get(ArcadeManager.class), permission, commandName);
		
		_executor = executor;
	}

	public void Execute(Player caller, String[] args)
	{
		_executor.execute(caller, args);
	}
}