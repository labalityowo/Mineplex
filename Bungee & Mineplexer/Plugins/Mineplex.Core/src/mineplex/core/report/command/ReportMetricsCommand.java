package mineplex.core.report.command;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.BukkitFuture;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.report.ReportManager;
import mineplex.core.report.ReportPlugin;
import mineplex.core.report.data.metrics.ReportMetrics;

/**
 * Displays various report-related metrics on a player.
 */
public class ReportMetricsCommand extends CommandBase<ReportPlugin>
{
	private static final String PREFIX = "Report Metrics";
	private static final int MAX_DAYS = 30;

	public ReportMetricsCommand(ReportPlugin plugin)
	{
		super(plugin, ReportManager.Perm.REPORT_METRICS_COMMAND, "reportmetrics");
	}

	@Override
	public void Execute(Player player, String[] args)
	{
		if (args.length > 0 && args.length <= 2)
		{
			Integer days = parseDaysArgument(player, args[0]);

			if (days != null)
			{
				if (args.length >= 2) // has target argument
				{
					String targetName = args[1];

					Plugin.getManager().getRepository().getAccountId(targetName).thenAccept(targetIdOptional ->
					{
						if (targetIdOptional.isPresent())
						{
							int targetId = targetIdOptional.get();
							displayUserMetrics(player, targetName, targetId, days);
						}
						else
						{
							UtilPlayer.message(player, F.main(Plugin.getName(), C.cRed + "Player not found."));
						}
					});
				}
				else // display global metrics
				{
					UtilPlayer.message(player, F.main("Report Metrics", F.elem("Global Metrics") + " (" + F.elem(days + " days") + ")"));
					displayGlobalMetrics(player, days);
				}
			}
		}
		else
		{
			UtilPlayer.message(player,
					F.main(Plugin.getName(), C.cRed + "Invalid Usage: "
							+ F.elem("/" + _aliasUsed + " <days> [player]")));
		}
	}

	public Integer parseDaysArgument(Player sender, String daysString)
	{
		Integer days;

		try
		{
			days = Integer.parseInt(daysString);
		}
		catch (NumberFormatException e)
		{
			UtilPlayer.message(sender, F.main(PREFIX, F.elem(daysString) + C.cRed + " is not a valid integer."));
			return null;
		}

		if (days > MAX_DAYS)
		{
			UtilPlayer.message(sender, F.main(PREFIX,
					C.cRed + "Cannot view metrics for longer than " + F.elem(MAX_DAYS + " days") + C.cRed + "."));

			return null;
		}

		return days;
	}

	public void displayGlobalMetrics(Player player, int days)
	{
		Plugin.getManager().getMetricsRepository().getGlobalMetrics(days).thenCompose(
				BukkitFuture.accept(globalMetrics ->
				{
					UtilPlayer.message(player, F.main("Report Metrics", "Submitted: " + F.elem(globalMetrics.getSubmitted())));
					UtilPlayer.message(player, F.main("Report Metrics", "Expired: " + F.elem(globalMetrics.getExpired())));
					displayMetrics(player, globalMetrics);
				}));
	}

	public void displayUserMetrics(Player player, String targetName, int targetId, int days)
	{
		Plugin.getManager().getMetricsRepository().getUserMetrics(targetId, days).thenCompose(
				BukkitFuture.accept(userMetrics ->
				{
					UtilPlayer.message(player,
							F.main("Report Metrics",
									F.elem(targetName) + " (" + F.elem(days + " days") + ")"));

					displayMetrics(player, userMetrics);
				}));
	}

	public void displayMetrics(Player player, ReportMetrics reportMetrics)
	{
		UtilPlayer.message(player, F.main("Report Metrics", "Accepted: " + F.elem(reportMetrics.getAccepted())));
		UtilPlayer.message(player, F.main("Report Metrics", "Denied: " + F.elem(reportMetrics.getDenied())));
		UtilPlayer.message(player, F.main("Report Metrics", "Flagged Abusive: " + F.elem(reportMetrics.getFlagged())));
	}
}