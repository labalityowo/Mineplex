package mineplex.core.teamspeak.commands;

import org.bukkit.entity.Player;

import mineplex.core.command.MultiCommandBase;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.teamspeak.TeamspeakManager;

public class TeamspeakCommand extends MultiCommandBase<TeamspeakManager>
{
	public TeamspeakCommand(TeamspeakManager plugin)
	{
		super(plugin, TeamspeakManager.Perm.TEAMSPEAK_COMMAND, "teamspeak", "ts");

		AddCommand(new LinkCommand(plugin));
		AddCommand(new ListCommand(plugin));
		AddCommand(new UnlinkCommand(plugin));
	}

	@Override
	protected void Help(Player caller, String[] args)
	{
		UtilPlayer.message(caller, F.main("Teamspeak", "To link a new Teamspeak account, run " + F.elem("/teamspeak link")));
		UtilPlayer.message(caller, F.main("Teamspeak", "To list all linked Teamspeak accounts, run " + F.elem("/teamspeak list")));
	}
}