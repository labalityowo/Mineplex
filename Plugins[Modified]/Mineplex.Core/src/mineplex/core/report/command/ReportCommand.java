package mineplex.core.report.command;

import java.util.logging.Level;

import org.bukkit.entity.Player;

import mineplex.core.account.CoreClient;
import mineplex.core.account.CoreClientManager;
import mineplex.core.command.CommandBase;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.report.ReportManager;
import mineplex.core.report.ReportPlugin;
import mineplex.core.report.ui.ReportCreatePage;

/**
 * The command used by players to create a report.
 * When executing this command the user will be prompted to select the type of report.
 */
public class ReportCommand extends CommandBase<ReportPlugin>
{
	public ReportCommand(ReportPlugin plugin)
	{
		super(plugin, ReportManager.Perm.REPORT_COMMAND, "report");
	}

	@Override
	public void Execute(final Player reporter, final String[] args)
	{
		CoreClientManager clientManager = _commandCenter.GetClientManager();

		ReportManager reportManager = Plugin.getManager();
		boolean canReport = reportManager.canReport(reporter);

		if (canReport)
		{
			if (args == null || args.length < 2)
			{
				UtilPlayer.message(reporter, F.main(Plugin.getName(), C.cRed + "Invalid Usage: " + F.elem("/" + _aliasUsed + " <player> <reason>")));
			}
			else
			{
				int reporterId = clientManager.getAccountId(reporter);
				String playerName = args[0];
				Player suspect = UtilPlayer.searchOnline(reporter, playerName, false);
				String reason = F.combine(args, 1, null, false);

				reportManager.getOpenReports(reporterId).whenComplete((reports, throwable) ->
				{
					if (throwable == null)
					{
						if (reports.size() < ReportManager.MAXIMUM_REPORTS)
						{
							if (suspect != null)
							{
								// allow developer (iKeirNez) to report himself (for easy testing reasons)
								if (suspect == reporter && !reportManager.isDevMode(reporter.getUniqueId()))
								{
									UtilPlayer.message(reporter, F.main(Plugin.getName(),
											C.cRed + "You cannot report yourself."));
								}
								else
								{
									CoreClient suspectClient = clientManager.Get(suspect);
									new ReportCreatePage(Plugin, reporter, reporterId, suspectClient, reason).openInventory();
								}
							}
							else
							{
								clientManager.loadClientByName(playerName, suspectClient ->
								{
									if (suspectClient != null)
									{
										new ReportCreatePage(Plugin, reporter, reporterId, suspectClient, reason).openInventory();
									}
									else
									{
										UtilPlayer.message(reporter, F.main(Plugin.getName(),
												C.cRed + "Unable to find player '" + playerName + "'!"));
									}
								});
							}
						}
						else
						{
							UtilPlayer.message(reporter, F.main(Plugin.getName(),
									C.cRed + "Cannot create report, you have reached the limit."));
						}
					}
					else
					{
						UtilPlayer.message(reporter, F.main(Plugin.getName(),
								C.cRed + "An error occurred, please try again."));
						Plugin.getPlugin().getLogger().log(Level.SEVERE, "Error whilst fetching open reports.", throwable);
					}
				});
			}
		}
		else
		{
			UtilPlayer.message(reporter, C.cRed + "You are banned from using the report feature.");
		}
	}
}