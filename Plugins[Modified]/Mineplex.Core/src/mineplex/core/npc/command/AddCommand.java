package mineplex.core.npc.command;

import java.sql.SQLException;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.npc.NpcManager;

public class AddCommand extends CommandBase<NpcManager>
{
	public AddCommand(NpcManager plugin)
	{
		super(plugin, NpcManager.Perm.ADD_NPC_COMMAND, "add");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		if (args == null || args.length == 0)
		{
			Plugin.help(caller);
		}
		else
		{
			EntityType type;
			try
			{
				type = EntityType.valueOf(args[0].toUpperCase());
			}
			catch (IllegalArgumentException e)
			{
				Plugin.help(caller, "Invalid entity.");

				return;
			}

			double radius = 0;
			if (args.length >= 2)
			{
				try
				{
					radius = Double.parseDouble(args[1]);
				}
				catch (NumberFormatException e)
				{
					Plugin.help(caller, "Invalid radius.");

					return;
				}
			}

			boolean adult = true;
			if (args.length >= 3)
			{
				adult = Boolean.parseBoolean(args[2]);
			}

			String name = null;
			if (args.length >= 4)
			{
				name = args[3];
				for (int i = 4; i < args.length; i++)
				{
					name += " " + args[i];
				}
			}

			try
			{
				Plugin.addNpc(caller, type, radius, adult, name, null);
			}
			catch (SQLException e)
			{
				Plugin.help(caller, "Database error.");
			}
		}
	}
}