package mineplex.core.report.command;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.report.ReportManager;
import mineplex.core.report.ReportPlugin;
import mineplex.core.report.ui.ReportHandlePage;

/**
 * When executed, the user is appointed handler of the most important report in the report queue (if any).
 * A user may only handle 1 report at a time.
 */
public class ReportHandleCommand extends CommandBase<ReportPlugin>
{
	public ReportHandleCommand(ReportPlugin plugin)
	{
		super(plugin, ReportManager.Perm.REPORT_HANDLE_COMMAND, "reporthandle", "rh");
	}

	@Override
	public void Execute(final Player player, final String[] args)
	{
		if (args == null || args.length == 0)
		{
			int accountId = _commandCenter.GetClientManager().getAccountId(player);
			ReportHandlePage reportHandlePage = new ReportHandlePage(Plugin, player, accountId);
			reportHandlePage.openInventory();
		}
		else
		{
			UtilPlayer.message(player, F.main(Plugin.getName(), C.cRed + "Invalid Usage: " + F.elem("/" + _aliasUsed)));
		}
	}
}