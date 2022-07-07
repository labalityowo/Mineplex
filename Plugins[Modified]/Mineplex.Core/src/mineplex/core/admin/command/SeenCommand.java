package mineplex.core.admin.command;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTime;

public class SeenCommand extends CommandBase<AdminCommands>
{
	public SeenCommand(AdminCommands plugin)
	{
		super(plugin, AdminCommands.Perm.SEEN_COMMAND, "seen", "lastlogin");
	}

	private void help(Player caller)
	{
		reply(caller, "Usage: " + F.elem("/seen <player>"));
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		if (args.length != 1)
		{
			help(caller);
			return;
		}

		String target = args[0];

		if (!UtilPlayer.isValidName(target))
		{
			reply(caller, "That name isn't valid! Try again?");
		}

		if (target.equalsIgnoreCase(caller.getName()))
		{
			reply(caller, "I see you right now! Is this a trick?");
			return;
		}

		if (Bukkit.getPlayerExact(target) != null)
		{
			reply(caller, "They're on this server, right now! Have you no eyes?");
			return;
		}

		Plugin.getCoreClientManager().loadLastLogin(target, (lastLogin) ->
		{
			if (lastLogin == null)
			{
				reply(caller, "The player " + F.name(target) + " was not found.");
				return;
			}

			reply(caller, "The player " + F.name(target) + " last logged in at " + F.elem(UtilTime.when(lastLogin)));
		});
	}
}
