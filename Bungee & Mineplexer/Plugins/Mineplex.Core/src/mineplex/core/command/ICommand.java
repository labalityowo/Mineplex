package mineplex.core.command;

import java.util.Collection;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import mineplex.core.account.permissions.Permission;
import mineplex.core.lifetimes.Component;

public interface ICommand extends Component
{
	void SetCommandCenter(CommandCenter commandCenter);
	void Execute(Player caller, String[] args);

	Collection<String> Aliases();

	void SetAliasUsed(String name);
	
	Permission getPermission();

	@Override
	default void activate()
	{
		CommandCenter.Instance.addCommand(this);
	}

	@Override
	default void deactivate()
	{
		CommandCenter.Instance.removeCommand(this);
	}

	List<String> onTabComplete(CommandSender sender, String commandLabel, String[] args);
}