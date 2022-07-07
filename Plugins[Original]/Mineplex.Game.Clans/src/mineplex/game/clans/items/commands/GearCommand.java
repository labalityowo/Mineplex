package mineplex.game.clans.items.commands;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.game.clans.items.GearManager;

public class GearCommand extends CommandBase<GearManager>
{
	public GearCommand(GearManager plugin)
	{
		super(plugin, GearManager.Perm.GEAR_COMMAND, "gear", "custom-gear");
	}
	
	@Override
	public void Execute(Player caller, String[] args)
	{
		if (args == null || args.length == 0)
		{
			Plugin.openShop(caller);
		}
		else
		{
			caller.updateInventory();
		}
	}	
}