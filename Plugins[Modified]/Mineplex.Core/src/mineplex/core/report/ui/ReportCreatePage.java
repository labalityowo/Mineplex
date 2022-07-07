package mineplex.core.report.ui;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;

import mineplex.core.account.CoreClient;
import mineplex.core.chatsnap.SnapshotManager;
import mineplex.core.chatsnap.SnapshotMessage;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.gui.SimpleGui;
import mineplex.core.report.ReportCategory;
import mineplex.core.report.ReportManager;
import mineplex.core.report.ReportPlugin;

/**
 * User interface shown to a player when reporting another player.
 */
public class ReportCreatePage extends SimpleGui implements  ReportCategoryCallback
{
	private final ReportPlugin _plugin;
	private final Player _reporter;
	private final int _reporterId;
	private final CoreClient _suspect;
	private final String _reason;

	public ReportCreatePage(ReportPlugin plugin, Player reporter, int reporterId, CoreClient suspect, String reason)
	{
		super(plugin.getPlugin(), reporter, "Report " + suspect.getName(), 9 * 3);

		_plugin = plugin;
		_reporter = reporter;
		_reporterId = reporterId;
		_suspect = suspect;
		_reason = reason;

		buildPage();
	}

	private void buildPage()
	{
		setItem(11, new ReportCategoryButton(this, ReportCategory.HACKING));
		setItem(13, new ReportCategoryButton(this, ReportCategory.CHAT_ABUSE));
		setItem(15, new ReportCategoryButton(this, ReportCategory.GAMEPLAY));
	}

	public void addReport(ReportCategory category)
	{
		if (category == ReportCategory.CHAT_ABUSE)
		{
			if (hasSentMessage(_suspect.getAccountId()))
			{
				createReport(category);
			}
			else
			{
				UtilPlayer.message(_reporter, F.main(_plugin.getName(), C.cRed + "You have not received a message from that player"));
			}
		}
		else
		{
			createReport(category);
		}
	}

	private boolean hasSentMessage(int accountId)
	{
		SnapshotManager snapshotManager = _plugin.getManager().getSnapshotManager();
		int suspectId = _suspect.getAccountId();
		List<SnapshotMessage> suspectMessages = snapshotManager.getMessagesFrom(accountId).stream()
				.filter(message -> message.getSenderId() == suspectId || message.getRecipientIds().contains(suspectId))
				.collect(Collectors.toList());

		return suspectMessages.size() > 0;
	}

	private void createReport(ReportCategory category)
	{
		_plugin.getManager().createReport(_reporterId, _suspect.getAccountId(), category, _reason)
				.thenAccept(report -> {
					boolean error = true;

					if (report != null)
					{
						Optional<Long> reportIdOptional = report.getId();

						if (reportIdOptional.isPresent())
						{
							_reporter.sendMessage(F.main(ReportManager.getReportPrefix(reportIdOptional.get()), C.cGreen + "Successfully created report."));
							error = false;
						}
					}

					if (error)
					{
						_reporter.sendMessage(C.cRed + "An error occurred whilst reporting that player, please try again.");
					}
				});
	}

	@Override
	public void click(ReportCategoryButton button)
	{
		_reporter.closeInventory();
		addReport(button.getCategory());
	}
}