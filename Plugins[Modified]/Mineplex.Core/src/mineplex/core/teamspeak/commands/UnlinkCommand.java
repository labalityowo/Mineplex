package mineplex.core.teamspeak.commands;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.teamspeak.TeamspeakManager;

public class UnlinkCommand extends CommandBase<TeamspeakManager>
{
	public UnlinkCommand(TeamspeakManager plugin)
	{
		super(plugin, TeamspeakManager.Perm.UNLINK_COMMAND, "unlink");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		if (args.length == 0)
		{
			UtilPlayer.message(caller, F.main("Teamspeak", "No account specified!"));
			return;
		}

		Plugin.unlink(caller, args[0]);
	}
}