package mineplex.game.clans.restart;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.slack.SlackAPI;
import mineplex.core.slack.SlackMessage;
import mineplex.core.slack.SlackTeam;

public class RestartCommand extends CommandBase<RestartManager>
{
	public RestartCommand(RestartManager plugin)
	{
		super(plugin, RestartManager.Perm.RESTART_COMMAND, "forceRestart");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		UtilPlayer.message(caller, F.main("Clans", "Initiating server restart!"));
		Plugin.restart();
		if (!UtilServer.isTestServer())
		{
			SlackAPI.getInstance().sendMessage(SlackTeam.DEVELOPER, "#clans-commandspy",
					new SlackMessage("Clans Command Logger", "crossed_swords", caller.getName() + " has initiated a restart of " + UtilServer.getServerName() + "."),
					true);
		}
	}
}