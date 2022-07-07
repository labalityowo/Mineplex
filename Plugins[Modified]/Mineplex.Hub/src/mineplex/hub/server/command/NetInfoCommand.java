package mineplex.hub.server.command;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.game.status.GameInfo;
import mineplex.hub.server.GameServer;
import mineplex.hub.server.ServerManager;
import mineplex.hub.server.ServerManager.Perm;

public class NetInfoCommand extends CommandBase<ServerManager>
{

	NetInfoCommand(ServerManager plugin)
	{
		super(plugin, Perm.NET_STAT_COMMAND, "info");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		if (args.length == 0)
		{
			caller.sendMessage(F.main(Plugin.getName(), "Enter a server name you muppet."));
			return;
		}

		GameServer server = Plugin.getServer(args[0]);

		if (server == null)
		{
			caller.sendMessage(F.main(Plugin.getName(), F.color(args[0], C.cGold) + " is not a valid server."));
			return;
		}

		GameInfo info = server.getInfo();

		if (info == null)
		{
			caller.sendMessage(F.main(Plugin.getName(), F.color(args[0], C.cGold) + " exists but does not have valid GameInfo."));
		}
		else
		{
			caller.sendMessage(F.main(Plugin.getName(), server.getInfo().toString()));
		}
	}
}
