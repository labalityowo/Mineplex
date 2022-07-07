package mineplex.core.teamspeak.commands;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.teamspeak.TeamspeakManager;

public class LinkCommand extends CommandBase<TeamspeakManager>
{
	public LinkCommand(TeamspeakManager plugin)
	{
		super(plugin, TeamspeakManager.Perm.LINK_COMMAND, "link");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		if (args.length == 0)
		{
			UtilPlayer.message(caller, F.main("Teamspeak", "No token specified. Join the " + F.elem(TeamspeakManager.TEAMSPEAK_CHANNEL_NAME) + " on Teamspeak and type " + F.elem("!link") + " to link an account"));
			return;
		}

		Plugin.link(caller, args[0]);
	}
}