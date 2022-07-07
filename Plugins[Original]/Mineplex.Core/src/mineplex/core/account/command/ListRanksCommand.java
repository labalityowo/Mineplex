package mineplex.core.account.command;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.entity.Player;

import mineplex.core.account.CoreClientManager;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.command.CommandBase;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;

public class ListRanksCommand extends CommandBase<CoreClientManager>
{
	public ListRanksCommand(CoreClientManager plugin)
	{
		super(plugin, CoreClientManager.Perm.LIST_RANKS_COMMAND, "list");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		UtilPlayer.message(caller, F.main(Plugin.getName(), "Available Ranks: " + Stream.of(PermissionGroup.values()).map(group -> C.cYellow + group.name().toLowerCase()).sorted().collect(Collectors.joining(C.cGray + ", "))));
	}
}