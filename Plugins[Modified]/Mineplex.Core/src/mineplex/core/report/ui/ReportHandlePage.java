package mineplex.core.report.ui;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import mineplex.core.common.util.BukkitFuture;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.gui.SimpleGui;
import mineplex.core.report.ReportCategory;
import mineplex.core.report.ReportManager;
import mineplex.core.report.ReportPlugin;
import mineplex.core.report.data.Report;
import mineplex.core.report.data.ReportRepository;

/**
 * An interface which allows the user to select the type of report they'd like to handle.
 */
public class ReportHandlePage extends SimpleGui implements ReportCategoryCallback
{
	private final ReportPlugin _plugin;
	private final Player _handler;
	private final int _handlerId;

	public ReportHandlePage(ReportPlugin plugin, Player handler, int handlerId)
	{
		super(plugin.getPlugin(), handler, "Report Type Selection", 9 * 3);

		_plugin = plugin;
		_handler = handler;
		_handlerId = handlerId;

		buildPage();
	}

	private void buildPage()
	{
		setItem(11, new ReportCategoryButton(this, ReportCategory.HACKING));
		setItem(13, new ReportCategoryButton(this, ReportCategory.CHAT_ABUSE));
		setItem(15, new ReportCategoryButton(this, ReportCategory.GAMEPLAY));
	}

	@Override
	public void click(ReportCategoryButton button)
	{
		_handler.closeInventory();
		handleReport(button.getCategory());
	}

	public void handleReport(ReportCategory category)
	{
		ReportManager reportManager = _plugin.getManager();
		ReportRepository reportRepository = reportManager.getRepository();

		reportManager.isHandlingReport(_handler).whenComplete((handlingReport, throwable) ->
		{
			if (throwable == null)
			{
				if (!handlingReport)
				{
					Map<Report, Double> reportPriorities = Collections.synchronizedMap(new HashMap<>());
					boolean devMode = reportManager.isDevMode(_handler.getUniqueId());

					// the below fetches the ids of all unhandled reports and gets a Report object for each of these ids
					// the priority of the report is then calculated and the results placed in a map
					reportRepository.getUnhandledReports(_handlerId, category, devMode).thenCompose(reportRepository::getReports).thenAccept(reports ->
							CompletableFuture.allOf(reports.stream().map(report ->
									reportManager.calculatePriority(report).thenAccept(priority ->
											{
												if (priority > 0)
												{
													reportPriorities.put(report, priority);
												}
												else
												{
													// mark the report as expired to keep the database clean
													// and reduce future query time
													reportManager.expireReport(report);
												}
											}
									)
							).toArray(CompletableFuture[]::new)).join()
					).thenApply(aVoid ->
					{
						Map.Entry<Report, Double> mostImportant = null;

						for (Map.Entry<Report, Double> entry : reportPriorities.entrySet())
						{
							if (mostImportant == null || (double) entry.getValue() > mostImportant.getValue())
							{
								mostImportant = entry;
							}
						}

						return mostImportant == null ? null : mostImportant.getKey();
					}).thenCompose(BukkitFuture.accept(report ->
					{
						if (report != null)
						{
							reportManager.handleReport(report, _handler);
						}
						else
						{
							UtilPlayer.message(_handler, F.main(_plugin.getName(), "No open " + F.elem(category.getName()) + " report(s) found."));
						}
					}));
				}
				else
				{
					Bukkit.getScheduler().runTask(_plugin.getPlugin(), () ->
							UtilPlayer.message(_handler, F.main(_plugin.getName(), C.cRed + "You are already handling a report.")));

				}
			}
			else
			{
				UtilPlayer.message(_handler, F.main(_plugin.getName(), C.cRed + "An error occurred, please try again later."));
				_plugin.getPlugin().getLogger().log(Level.SEVERE, "Error whilst checking for reports being handled by " + _handler.getName(), throwable);
			}
		});
	}
}
