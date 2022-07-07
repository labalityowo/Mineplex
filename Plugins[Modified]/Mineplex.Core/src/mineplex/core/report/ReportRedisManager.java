package mineplex.core.report;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;

import mineplex.core.common.util.UtilServer;
import mineplex.core.report.redis.FindPlayer;
import mineplex.core.report.redis.FindPlayerResponse;
import mineplex.core.report.redis.HandlerNotification;
import mineplex.core.report.redis.ReportersNotification;
import mineplex.serverdata.commands.CommandCallback;
import mineplex.serverdata.commands.ServerCommand;

/**
 * Handles receiving of report notifications.
 */
public class ReportRedisManager implements CommandCallback
{
	private final ReportManager _reportManager;
	private final String _serverName;

	public ReportRedisManager(ReportManager reportManager, String serverName)
	{
		_reportManager = reportManager;
		_serverName = serverName;
	}

	@Override
	public void run(ServerCommand command)
	{
		if (command instanceof HandlerNotification)
		{
			HandlerNotification reportNotification = (HandlerNotification) command;

			_reportManager.getRepository().getReport(reportNotification.getReportId()).thenAccept(report ->
					{
						if (report != null)
						{
							int handlerId = reportNotification.getHandlerId();

							_reportManager.getRepository().getAccountUUID(handlerId).thenAccept(handlerUUID ->
							{
								if (handlerUUID != null)
								{
									Player handler = Bukkit.getPlayer(handlerUUID);

									if (handler != null)
									{
										sendRawMessage(handler, reportNotification.getJson());
									}
								}
							});
						}
					}
			);
		}
		else if (command instanceof ReportersNotification)
		{
			ReportersNotification reportersNotification = (ReportersNotification) command;
			reportersNotification.getReporterUUIDs().stream()
								 .map(Bukkit::getPlayer)
								 .filter(player -> player != null)
								 .forEach(reporter -> sendRawMessage(reporter, reportersNotification.getJson()));
		}
		else if (command instanceof FindPlayer)
		{
			FindPlayer findPlayer = (FindPlayer) command;

			if (Bukkit.getPlayer(findPlayer.getId()) != null)
			{
				new FindPlayerResponse(findPlayer, _serverName).publish();
			}
		}
		else if (command instanceof FindPlayerResponse)
		{
			FindPlayerResponse foundPlayer = (FindPlayerResponse) command;
			_reportManager.onSuspectLocated(foundPlayer.getReportId(), foundPlayer.getServerName());
		}
	}

	private void sendRawMessage(Player player, String rawMessage)
	{
		Server server = UtilServer.getServer();
		server.dispatchCommand(server.getConsoleSender(), "tellraw " + player.getName() + " " + rawMessage);
	}
}
