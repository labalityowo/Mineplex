package mineplex.core.status;

import mineplex.core.common.util.F;
import mineplex.core.portal.GenericServer;
import mineplex.core.portal.Intent;
import mineplex.core.portal.Portal;
import mineplex.serverdata.Region;
import mineplex.serverdata.commands.CommandCallback;
import mineplex.serverdata.commands.ServerCommand;
import mineplex.serverdata.commands.SuicideCommand;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class SuicideHandler implements CommandCallback
{
	private ServerStatusManager _statusManager;
	private String _serverName;
	private Region _region;
	
	public SuicideHandler(ServerStatusManager statusManager, String serverName, Region region)
	{
		_statusManager = statusManager;
		_serverName = serverName;
		_region = region;
	}
	
	public void run(ServerCommand command)
	{
		if (command instanceof SuicideCommand)
		{
			String serverName = ((SuicideCommand)command).getServerName();
			Region region = ((SuicideCommand)command).getRegion();
			
			if (!serverName.equalsIgnoreCase(_serverName) || _region != region)
				return;
			
			for (Player player : Bukkit.getOnlinePlayers())
			{
				player.sendMessage(F.main("Cleanup", "Server is being cleaned up, you're being sent to a lobby."));
			}
			
			Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Bukkit.getPluginManager().getPlugins()[0], new Runnable()
			{
				public void run()
				{
					Portal.getInstance().sendAllPlayersToGenericServer(GenericServer.HUB, Intent.KICK);
				}
			}, 60L);
			
			_statusManager.disableStatus();
		}
	}
}
