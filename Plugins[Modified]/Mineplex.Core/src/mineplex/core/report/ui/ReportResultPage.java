package mineplex.core.report.ui;

import java.util.logging.Level;

import org.bukkit.entity.Player;

import mineplex.core.common.util.BukkitFuture;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.gui.SimpleGui;
import mineplex.core.report.ReportManager;
import mineplex.core.report.ReportPlugin;
import mineplex.core.report.ReportResult;
import mineplex.core.report.ReportResultType;
import mineplex.core.report.ReportTeam;
import mineplex.core.report.data.Report;

/**
 * User interface shown to a moderator when closing a report to determine the result of the report.
 */
public class ReportResultPage extends SimpleGui
{
	private final ReportPlugin _plugin;
	private final ReportManager _reportManager;
	private final Report _report;
	private final String _suspectName;
	private final String _reason;

	public ReportResultPage(ReportPlugin plugin, Report report, Player reportCloser, String suspectName, String reason)
	{
		super(plugin.getPlugin(), reportCloser, "Close Report", 9 * 4);
		_plugin = plugin;
		_reportManager = plugin.getManager();
		_report = report;
		_suspectName = suspectName;
		_reason = reason;

		buildPage();
	}

	public String getSuspectName()
	{
		return _suspectName;
	}

	private void buildPage()
	{
		setItem(11, new ReportResultButton(this, ReportResultType.ACCEPTED));
		setItem(13, new ReportResultButton(this, ReportResultType.DENIED));
		setItem(15, new ReportResultButton(this, ReportResultType.ABUSIVE));
		setItem(27, new ReportAbortButton(this));
		setItem(35, new ReportAssignTeamButton(this, ReportTeam.RC));
	}

	public void closeReport(ReportResultType result)
	{
		getPlayer().closeInventory();
		ReportResult reportResult = new ReportResult(result, _reason);

		_reportManager.closeReport(_report, getPlayer(), reportResult);
	}

	public void assignTeam(ReportTeam team)
	{
		getPlayer().closeInventory();

		_reportManager.assignTeam(_report, team).thenAccept(aVoid ->
				UtilPlayer.message(getPlayer(),
						F.main(ReportManager.getReportPrefix(_report),
								"Report forwarded to " + F.elem(team.name()) + " team")));
	}

	public void abortReport()
	{
		getPlayer().closeInventory();

		_reportManager.getReportHandling(getPlayer()).whenComplete(BukkitFuture.complete((reportOptional, throwable) ->
		{
			if (throwable == null)
			{
				if (reportOptional.isPresent())
				{
					Report report = reportOptional.get();

					_reportManager.abortReport(report).thenApply(BukkitFuture.accept(voidValue ->
							UtilPlayer.message(getPlayer(), F.main(ReportManager.getReportPrefix(report),
									"Report has been aborted and may be handled by another staff member."))));
				}
				else
				{
					UtilPlayer.message(getPlayer(), F.main(_plugin.getName(), "You aren't currently handling a report."));
				}
			}
			else
			{
				UtilPlayer.message(getPlayer(), F.main(_plugin.getName(), C.cRed + "An error occurred, please try again later."));
				_plugin.getPlugin().getLogger().log(Level.SEVERE, "Error whilst aborting report for player " + getPlayer().getName(), throwable);
			}
		}));
	}
}
