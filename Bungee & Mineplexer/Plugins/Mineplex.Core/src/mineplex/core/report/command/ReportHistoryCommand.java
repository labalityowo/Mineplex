package mineplex.core.report.command;

import org.bukkit.entity.Player;

import com.google.common.primitives.Longs;

import mineplex.core.command.CommandBase;
import mineplex.core.common.jsonchat.ChildJsonMessage;
import mineplex.core.common.jsonchat.ClickEvent;
import mineplex.core.common.jsonchat.JsonMessage;
import mineplex.core.common.util.BukkitFuture;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.report.ReportManager;
import mineplex.core.report.ReportPlugin;
import mineplex.core.report.ReportRole;

/**
 * A staff command for viewing report related statistics of a player.
 */
public class ReportHistoryCommand extends CommandBase<ReportPlugin>
{
	public ReportHistoryCommand(ReportPlugin reportPlugin)
	{
		super(reportPlugin, ReportManager.Perm.REPORT_HISTORY_COMMAND, "reporthistory", "rhis");
	}

	@Override
	public void Execute(Player player, String[] args)
	{
		if (args != null && args.length == 1)
		{
			String playerName = args[0];

			Plugin.getManager().getRepository().getAccountId(playerName).thenAccept(accountIdOptional ->
			{
				if (accountIdOptional.isPresent())
				{
					int accountId = accountIdOptional.get();

					Plugin.getManager().getRepository().getAccountStatistics(accountId).thenCompose(BukkitFuture.accept(stats ->
							{
								UtilPlayer.message(player, F.main(Plugin.getName(), "Report History for " + F.elem(playerName)));

								for (ReportRole role : ReportRole.values())
								{
									long[] idArray = stats.get(role).stream()
											.sorted((l1, l2) -> Longs.compare(l2, l1))
											.mapToLong(l -> l)
											.toArray();
									int reportCount = idArray.length;

									// create clickable report ids
									ChildJsonMessage jsonMessage = new JsonMessage(F.main(Plugin.getName(), ""))
											.extra(C.mElem);

									int displayAmount = Math.min(idArray.length, 5);

									if (displayAmount > 0)
									{
										for (int i = 0; i < displayAmount; i++)
										{
											long reportId = idArray[i];

											jsonMessage = jsonMessage.add(String.valueOf(reportId))
													.click(ClickEvent.RUN_COMMAND, "/reportinfo " + reportId);

											if (i != displayAmount - 1)
											{
												jsonMessage = jsonMessage.add(", ");
											}
										}
									}
									else
									{
										jsonMessage = jsonMessage.add("N/A");
									}

									UtilPlayer.message(player, F.main(Plugin.getName(), F.elem(role.getHumanName()) + " (" + reportCount + ")"));
									jsonMessage.sendToPlayer(player);
								}
							}
					));
				}
				else
				{
					UtilPlayer.message(player, F.main(Plugin.getName(), C.cRed + "Player not found."));
				}
			});
		}
		else
		{
			UtilPlayer.message(player, F.main(Plugin.getName(), C.cRed + "Invalid Usage: " + F.elem("/" + _aliasUsed + " <player>")));
		}
	}
}