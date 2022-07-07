package mineplex.core.antihack.commands;

import org.bukkit.entity.Player;

import mineplex.core.antihack.AntiHack;
import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;

public class DetailedMessagesCommand extends CommandBase<AntiHack>
{
	public DetailedMessagesCommand(AntiHack plugin)
	{
		super(plugin, AntiHack.Perm.DETAILED_MESSAGES_COMMAND, "detailedmessages");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		if (Plugin.toggleDetailedMessage(caller))
		{
			UtilPlayer.message(caller, F.main(Plugin.getName(), "Detailed messages enabled"));
		}
		else
		{
			UtilPlayer.message(caller, F.main(Plugin.getName(), "Detailed messages disabled"));
		}
	}
}