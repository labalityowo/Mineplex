package mineplex.core.updater.command;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.portal.Portal;
import mineplex.core.updater.FileUpdater;
import mineplex.serverdata.commands.RestartCommand;

public class RestartServerCommand extends CommandBase<FileUpdater>
{

	public RestartServerCommand(FileUpdater plugin)
	{
		super(plugin, FileUpdater.Perm.RESTART_COMMAND, "restart");
	}

	@Override
	public void Execute(final Player caller, final String[] args)
	{
		if (args.length == 0)
		{
			UtilPlayer.message(caller, F.main(Plugin.getName(), "/" + _aliasUsed + " <server> - Restarts a single server."));
			UtilPlayer.message(caller, F.main(Plugin.getName(), "/" + _aliasUsed + " <prefix> group - Restarts a group of servers that match the prefix."));
			return;
		}

		String serverName = args[0];
		boolean groupRestart = args.length > 1 && args[1].equalsIgnoreCase("group");

		if (groupRestart)
		{
			new RestartCommand(serverName, Plugin.getRegion(), true).publish();
			UtilPlayer.message(caller, F.main(Plugin.getName(), "Sent restart command to all " + F.color(serverName, C.cGold) + " servers."));
		}
		else
		{
			Portal.getInstance().doesServerExist(serverName, serverExists ->
			{
				if (serverExists)
				{
					new RestartCommand(serverName, Plugin.getRegion(), false).publish();
					UtilPlayer.message(caller, F.main(Plugin.getName(), "Sent restart command to " + F.color(serverName, C.cGold) + "."));
				}
				else
				{
					UtilPlayer.message(caller, F.main(Plugin.getName(), F.color(serverName, C.cGold) + " doesn't exist."));
				}
			});
		}
	}
}