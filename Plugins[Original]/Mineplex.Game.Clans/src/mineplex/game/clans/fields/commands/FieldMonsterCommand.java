package mineplex.game.clans.fields.commands;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilPlayer;
import mineplex.game.clans.fields.FieldMonster;
import mineplex.game.clans.fields.FieldMonsterInput;
import mineplex.game.clans.fields.monsters.FieldMonsterBase;

public class FieldMonsterCommand extends CommandBase<FieldMonster>
{
	public FieldMonsterCommand(FieldMonster plugin)
	{
		super(plugin, FieldMonster.Perm.FIELD_MONSTER_COMMAND, "fm");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		if (!Plugin.getInput().containsKey(caller))
			Plugin.getInput().put(caller, new FieldMonsterInput());

		FieldMonsterInput input = Plugin.getInput().get(caller);

		if (args == null || args.length == 0)
		{
			Plugin.getInput().get(caller).Display(caller);
			UtilPlayer.message(caller, F.main(Plugin.getName(), "Type " + F.elem("/fm help") + " for commands."));
		}

		else if (args[0].equalsIgnoreCase("help"))
		{
			Plugin.Help(caller);
		}

		else if (args[0].equalsIgnoreCase("type"))
		{			
			try
			{
				input.type = UtilEnt.searchEntity(caller, args[1], true);
				if (input.type != null)
					input.Display(caller);
			}
			catch (Exception e)
			{
				UtilPlayer.message(caller, F.main(Plugin.getName(), "Invalid Monster Type."));
			}
		}

		else if (args[0].equalsIgnoreCase("max"))
		{
			try
			{
				int value = Integer.parseInt(args[1]);
				if (value < 1)	value = 1;
				input.mobMax = value;
				input.Display(caller);
			}
			catch (Exception e)
			{
				UtilPlayer.message(caller, F.main(Plugin.getName(), "Invalid Monster Max."));
			}
		}

		else if (args[0].equalsIgnoreCase("rate"))
		{
			try
			{
				double value = Double.parseDouble(args[1]);
				if (value < 0)	value = 0;
				input.mobRate = value;
				input.Display(caller);
			}
			catch (Exception e)
			{
				UtilPlayer.message(caller, F.main(Plugin.getName(), "Invalid Monster Rate."));
			}
		}

		else if (args[0].equalsIgnoreCase("radius"))
		{
			try
			{
				int integer = Integer.parseInt(args[1]);
				if (integer < 1)	integer = 1;
				input.radius = integer;
				input.Display(caller);
			}
			catch (Exception e)
			{
				UtilPlayer.message(caller, F.main(Plugin.getName(), "Invalid Area Radius."));
			}
		}

		else if (args[0].equalsIgnoreCase("height"))
		{
			try
			{
				int integer = Integer.parseInt(args[1]);
				if (integer < 1)	integer = 1;
				input.height = integer;
				input.Display(caller);
			}
			catch (Exception e)
			{
				UtilPlayer.message(caller, F.main(Plugin.getName(), "Invalid Area Height."));
			}
		}

		else if (args[0].equalsIgnoreCase("create"))
		{
			if (args.length < 2)
			{
				UtilPlayer.message(caller, F.main(Plugin.getName(), "Missing Monster Field Name."));
			}
			else
			{
				Plugin.Create(caller, args[1]);
			}
		}

		else if (args[0].equalsIgnoreCase("delete"))
		{
			if (args.length < 2)
			{
				UtilPlayer.message(caller, F.main(Plugin.getName(), "Missing Monster Field Name."));
			}
			else
			{
				Plugin.Delete(caller, args[1]);
			}
		}

		else if (args[0].equalsIgnoreCase("list"))
		{
			UtilPlayer.message(caller, F.main(Plugin.getName(), "Listing Monster Fields;"));

			for (FieldMonsterBase pit : Plugin.getPits())
				pit.Display(caller);
		}

		else if (args[0].equalsIgnoreCase("info"))
		{
			UtilPlayer.message(caller, F.main(Plugin.getName(), "Listing Monster Fields;"));

			for (FieldMonsterBase pit : Plugin.getPits())
				pit.Display(caller);
		}

		else if (args[0].equalsIgnoreCase("wipe"))
		{
			Plugin.Wipe(caller, true);
		}

		else if (args[0].equalsIgnoreCase("kill"))
		{
			for (FieldMonsterBase pit : Plugin.getPits())
				pit.RemoveMonsters();
		}

		else
		{
			UtilPlayer.message(caller, F.main(Plugin.getName(), "Invalid Command."));
		}
	}
}