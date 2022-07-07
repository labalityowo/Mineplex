package mineplex.core.report.command;

import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;

import org.bukkit.entity.Player;

import mineplex.core.chatsnap.SnapshotMetadata;
import mineplex.core.chatsnap.SnapshotRepository;
import mineplex.core.command.CommandBase;
import mineplex.core.common.jsonchat.ClickEvent;
import mineplex.core.common.jsonchat.HoverEvent;
import mineplex.core.common.jsonchat.JsonMessage;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.report.ReportManager;
import mineplex.core.report.ReportPlugin;
import mineplex.core.report.ReportResult;
import mineplex.core.report.data.Report;
import mineplex.core.report.data.ReportMessage;
import mineplex.core.report.data.ReportRepository;

/**
 * Provides the sender of the command with information of the supplied report.
 */
public class ReportInfoCommand extends CommandBase<ReportPlugin>
{
	public ReportInfoCommand(ReportPlugin plugin)
	{
		super(plugin, ReportManager.Perm.REPORT_INFO_COMMAND, "reportinfo");
	}

	@Override
	public void Execute(Player player, String[] args)
	{
		if (args != null && args.length == 1)
		{
			try
			{
				long reportId = Long.parseLong(args[0]);

				ReportManager reportManager = Plugin.getManager();
				ReportRepository repository = reportManager.getRepository();

				repository.getReport(reportId).thenAccept(reportOptional ->
				{
					if (reportOptional.isPresent())
					{
						Report report = reportOptional.get();
						String prefix = ReportManager.getReportPrefix(report);
						String suspect = repository.getAccountName(report.getSuspectId()).join();
						String handler = report.getHandlerId().map(handlerId -> repository.getAccountName(handlerId).join()).orElse("None");
						Optional<SnapshotMetadata> snapshotMetadataOptional = report.getSnapshotMetadata();

						UtilPlayer.message(player, F.main(prefix, "Type: " + F.elem(report.getCategory().getName())));
						UtilPlayer.message(player, F.main(prefix, "Suspect: " + F.elem(suspect)));
						UtilPlayer.message(player, F.main(prefix, "Handler: " + F.elem(handler)));

						UtilPlayer.message(player, F.main(prefix, ""));
						UtilPlayer.message(player, F.main(prefix, F.elem("Reporters")));

						for (Map.Entry<Integer, ReportMessage> entry : report.getReportMessages().entrySet())
						{
							String reporter = repository.getAccountName(entry.getKey()).join();
							ReportMessage message = entry.getValue();
							UtilPlayer.message(player, F.main(prefix, reporter + ": " + F.elem(message.getMessage())));
						}

						UtilPlayer.message(player, F.main(prefix, ""));

						Optional<ReportResult> resultOptional = report.getResult();
						String status = resultOptional.map(reportResult -> reportResult.getType().getName()).orElse("Unresolved");

						UtilPlayer.message(player, F.main(prefix, "Status: " + F.elem(status)));

						if (resultOptional.isPresent())
						{
							ReportResult result = resultOptional.get();
							String handlerMessage = result.getReason().orElse("None specified.");
							UtilPlayer.message(player, F.main(prefix, "Handler Message: " + F.elem(handlerMessage)));
						}

						if (snapshotMetadataOptional.isPresent())
						{
							SnapshotMetadata snapshotMetadata = snapshotMetadataOptional.get();
							Optional<String> tokenOptional = snapshotMetadata.getToken();

							if (tokenOptional.isPresent())
							{
								String token = tokenOptional.get();
								UtilPlayer.message(player, F.main(prefix, ""));

								new JsonMessage(F.main(prefix, F.elem("View chat log")))
										.hover(HoverEvent.SHOW_TEXT, C.cGray + "Opens the chat log in your default browser")
										.click(ClickEvent.OPEN_URL, SnapshotRepository.getURL(token))
										.sendToPlayer(player);
							}
						}
					}
					else
					{
						UtilPlayer.message(player, F.main(Plugin.getName(), C.cRed + "Couldn't find a report with that id, please try again"));
					}
				}).exceptionally(throwable ->
				{
					Plugin.getPlugin().getLogger().log(Level.SEVERE, "Error whilst getting report info.", throwable);
					return null;
				});
			}
			catch (NumberFormatException e)
			{
				UtilPlayer.message(player, F.main(Plugin.getName(), C.cRed + "Invalid report id"));
			}
		}
		else
		{
			UtilPlayer.message(player, F.main(Plugin.getName(), C.cRed + "Invalid Usage: " + F.elem("/" + _aliasUsed + " <report-id>")));
		}
	}
}