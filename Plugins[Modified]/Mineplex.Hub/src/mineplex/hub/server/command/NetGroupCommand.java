package mineplex.hub.server.command;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.hub.server.GameServer;
import mineplex.hub.server.ServerManager;
import mineplex.hub.server.ServerManager.Perm;

public class NetGroupCommand extends CommandBase<ServerManager>
{

	NetGroupCommand(ServerManager plugin)
	{
		super(plugin, Perm.NET_STAT_COMMAND, "groups");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		Plugin.getServers().forEach((group, map) ->
		{
			int players = map.values().stream()
					.mapToInt(gameServer -> gameServer.getServer().getPlayerCount())
					.sum();
			int servers = map.values().size();

			caller.sendMessage(F.main(Plugin.getName(), F.name(group) + " - " + F.count(players) + " Players on " + F.count(servers) + " Servers."));
		});
	}
}
