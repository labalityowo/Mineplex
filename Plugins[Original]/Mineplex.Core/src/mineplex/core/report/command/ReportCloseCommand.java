package mineplex.core.report.command;

import java.util.logging.Level;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.BukkitFuture;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.report.ReportManager;
import mineplex.core.report.ReportPlugin;
import mineplex.core.report.data.Report;
import mineplex.core.report.ui.ReportResultPage;

/**
 * The command used to close the report the user is currently handling.
 */
public class ReportCloseCommand extends CommandBase<ReportPlugin>
{
	public ReportCloseCommand(ReportPlugin plugin)
	{
		super(plugin, ReportManager.Perm.REPORT_CLOSE_COMMAND, "reportclose", "rc");
	}
	
	@Override
	public void Execute(final Player player, final String[] args)
	{		
		if (args == null || args.length < 1)
		{
			UtilPlayer.message(player, F.main(Plugin.getName(), C.cRed + "Invalid Usage: " + F.elem("/" + _aliasUsed + " <reason>")));
		}
		else
		{
			String reason = F.combine(args, 0, null, false);
			ReportManager reportManager = Plugin.getManager();

			reportManager.getReportHandling(player).whenComplete((reportOptional, throwable) ->
			{
				if (throwable == null)
				{
					if (reportOptional.isPresent())
					{
						Report report = reportOptional.get();

						reportManager.getRepository().getAccountName(report.getSuspectId()).thenCompose(BukkitFuture.accept(suspectName ->
						{
							ReportResultPage reportResultPage = new ReportResultPage(Plugin, report, player, suspectName, reason);
							reportResultPage.openInventory(); // report is closed when player selects the result
						}));
					}
					else
					{
						UtilPlayer.message(player, F.main(Plugin.getName(), C.cRed + "You aren't currently handling a report."));
					}
				}
				else
				{
					UtilPlayer.message(player, F.main(Plugin.getName(), C.cRed + "An error occurred, please try again later."));
					Plugin.getPlugin().getLogger().log(Level.SEVERE, "An error occurred whilst fetching the report being handled by " + player.getName(), throwable);
				}
			});
		}
	}
}