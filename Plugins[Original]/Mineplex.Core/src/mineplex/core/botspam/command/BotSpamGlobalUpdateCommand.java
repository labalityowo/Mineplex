package mineplex.core.botspam.command;

import org.bukkit.entity.Player;

import mineplex.core.botspam.BotSpamManager;
import mineplex.core.botspam.ForceUpdateCommand;
import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.serverdata.commands.ServerCommandManager;

public class BotSpamGlobalUpdateCommand extends CommandBase<BotSpamManager>
{
	public BotSpamGlobalUpdateCommand(BotSpamManager plugin)
	{
		super(plugin, BotSpamManager.Perm.UPDATE_BOTSPAM_COMMAND, "globalupdate");
	}

	@Override
	public void Execute(final Player caller, String[] args)
	{
		ServerCommandManager.getInstance().publishCommand(new ForceUpdateCommand());
		UtilPlayer.message(caller, F.main("Botspam", "Forced an update across all servers!"));
	}
}