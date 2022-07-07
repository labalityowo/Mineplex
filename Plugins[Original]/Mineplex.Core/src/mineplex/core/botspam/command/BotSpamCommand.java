package mineplex.core.botspam.command;

import org.bukkit.entity.Player;

import mineplex.core.botspam.BotSpamManager;
import mineplex.core.command.MultiCommandBase;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;

public class BotSpamCommand extends MultiCommandBase<BotSpamManager>
{
	public BotSpamCommand(BotSpamManager plugin)
	{
		super(plugin, BotSpamManager.Perm.BOTSPAM_COMMAND, "botban", "botspam");

		AddCommand(new BotSpamAddCommand(Plugin));
		AddCommand(new BotSpamEnableCommand(Plugin));
		AddCommand(new BotSpamDisableCommand(Plugin));
		AddCommand(new BotSpamListCommand(Plugin));
		AddCommand(new BotSpamGlobalUpdateCommand(Plugin));
	}

	@Override
	protected void Help(Player caller, String[] args)
	{
		UtilPlayer.message(caller, F.main("BotSpam", "/botspam list"));
		UtilPlayer.message(caller, F.main("BotSpam", "/botspam add <text>"));
		UtilPlayer.message(caller, F.main("BotSpam", "/botspam enable <text id>"));
		UtilPlayer.message(caller, F.main("BotSpam", "/botspam disable <text id>"));
		UtilPlayer.message(caller, F.main("BotSpam", "/botspam globalupdate"));
	}
}