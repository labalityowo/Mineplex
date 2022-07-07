package mineplex.hub.server.command;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import mineplex.core.command.MultiCommandBase;
import mineplex.core.common.util.F;
import mineplex.hub.server.ServerManager;
import mineplex.hub.server.ServerManager.Perm;

public class NetStatCommand extends MultiCommandBase<ServerManager>
{

	public NetStatCommand(ServerManager plugin)
	{
		super(plugin, Perm.NET_STAT_COMMAND, "netstat");

		AddCommand(new NetInfoCommand(plugin));
		AddCommand(new NetGroupCommand(plugin));
	}

	@Override
	protected void Help(Player caller, String[] args)
	{
		caller.sendMessage(F.main(Plugin.getName(), "Command List:"));
		caller.sendMessage(F.help("/" + _aliasUsed + " info <server>", "Gets the info of a particular server as JSON.", ChatColor.DARK_RED));
		caller.sendMessage(F.help("/" + _aliasUsed + " groups", "Lists all server groups with their player and server counts.", ChatColor.DARK_RED));
	}
}
