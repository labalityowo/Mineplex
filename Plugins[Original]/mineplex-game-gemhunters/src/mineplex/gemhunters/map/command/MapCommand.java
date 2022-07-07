package mineplex.gemhunters.map.command;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;
import mineplex.gemhunters.map.ItemMapModule;

public class MapCommand extends CommandBase<ItemMapModule>
{
	public MapCommand(ItemMapModule plugin)
	{
		super(plugin, ItemMapModule.Perm.MAP_COMMAND, "map", "getmap");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		caller.sendMessage(F.main(Plugin.getName(), "Giving you a new map."));
		Plugin.setMap(caller);
	}
}