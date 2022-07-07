package mineplex.game.clans.fields.commands;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.game.clans.fields.FieldBlock;

public class FieldBlockCommand extends CommandBase<FieldBlock>
{
	public FieldBlockCommand(FieldBlock plugin)
	{
		super(plugin, FieldBlock.Perm.FIELD_BLOCK_COMMAND, "fb");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		if (args.length == 0 || args[0].equalsIgnoreCase("help"))
		{
			Plugin.help(caller);
		}
		else if (args[0].equalsIgnoreCase("toggle"))
		{
			if (!Plugin.getActive().remove(caller.getName()))
				Plugin.getActive().add(caller.getName());
			
			UtilPlayer.message(caller, F.main(Plugin.getName(), "Interact Active: " + F.tf(Plugin.getActive().contains(caller.getName()))));
		}
		else if (args[0].equalsIgnoreCase("load"))
		{
			Plugin.load();
			UtilPlayer.message(caller, F.main(Plugin.getName(), "Reloaded Field Blocks from Database."));
		}
		else if (args[0].equalsIgnoreCase("wipe"))
		{
			Plugin.wipe(caller);
		}
		else if (args.length <= 1)
		{
			Plugin.help(caller);
		}
		else if (args[0].equalsIgnoreCase("title"))
		{
			Plugin.getTitle().put(caller, args[1]);
			Plugin.showSettings(caller);
		}
		else if (args[0].equalsIgnoreCase("stock"))
		{
			try
			{
				int count = Integer.parseInt(args[1]);
				if (count < 1)	count = 1;
				Plugin.getStock().put(caller, count);
				Plugin.showSettings(caller);
			}
			catch (Exception e)
			{
				UtilPlayer.message(caller, F.main(Plugin.getName(), "Invalid Stock Max."));
			}
		}
		else if (args[0].equalsIgnoreCase("regen"))
		{
			try
			{
				double regen = Double.parseDouble(args[1]);
				if (regen < 0)	regen = 0;
				Plugin.getRegen().put(caller, UtilMath.trim(1, regen));
				Plugin.showSettings(caller);
			}
			catch (Exception e)
			{
				UtilPlayer.message(caller, F.main(Plugin.getName(), "Invalid Stock Regeneration Time."));
			}
		}
		else if (args[0].equalsIgnoreCase("empty"))
		{
			try
			{
				String[] toks = args[1].split(":");

				int id = Integer.parseInt(toks[0]);
				byte data = Byte.parseByte(toks[1]);

				Plugin.getEmptyId().put(caller, id);
				Plugin.getEmptyData().put(caller, data);
				Plugin.showSettings(caller);
			}
			catch (Exception e)
			{
				UtilPlayer.message(caller, F.main(Plugin.getName(), "Invalid Empty Block."));
			}
		}
		else if (args[0].equalsIgnoreCase("loot"))
		{
			boolean error = false;
			for (String cur : args[1].split(","))
			{
				String[] loot = cur.split(":");

				if (loot.length != 5)
				{
					error = true;
					break;
				}

				try
				{
					Integer.parseInt(loot[0]);
					Byte.parseByte(loot[1]);
					Integer.parseInt(loot[2]);
					Integer.parseInt(loot[3]);
					Integer.parseInt(loot[4]);	
				}
				catch (Exception e)
				{
					error = true;
					break;
				}
			}

			if (error)
			{
				UtilPlayer.message(caller, F.main(Plugin.getName(), "Invalid Loot String."));
				return;
			}

			Plugin.getLootString().put(caller, args[1]);
			Plugin.showSettings(caller);
		}
	}
}