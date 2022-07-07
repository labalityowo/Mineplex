package mineplex.game.clans.fields.commands;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.game.clans.fields.FieldOre;

public class FieldOreCommand extends CommandBase<FieldOre>
{
	public FieldOreCommand(FieldOre plugin)
	{
		super(plugin, FieldOre.Perm.FIELD_ORE_COMMAND, "fo");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		if (args == null || args.length == 0)
		{
			Plugin.help(caller);
			return;
		}
		
		if (args[0].equalsIgnoreCase("toggle"))
		{
			if (!Plugin.getActivePlayers().remove(caller.getName()))
				Plugin.getActivePlayers().add(caller.getName());
			
			UtilPlayer.message(caller, F.main(Plugin.getName(), "Interact Active: " + F.tf(Plugin.getActivePlayers().contains(caller.getName()))));
		}
		
		else if (args[0].equalsIgnoreCase("help"))
		{
			Plugin.help(caller);
		}
		
		else if (args[0].equalsIgnoreCase("reset"))
		{
			Plugin.reset(caller);
		}
		
		else if (args[0].equalsIgnoreCase("fill"))
		{
			Plugin.fill(caller);
		}
		
		else if (args[0].equalsIgnoreCase("list"))
		{
			Plugin.list(caller);
		}
		
		else if (args[0].equalsIgnoreCase("wipe"))
		{
			Plugin.wipe(caller);
		}
	}
}