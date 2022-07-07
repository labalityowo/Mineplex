package mineplex.game.clans.clans.commands;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.game.clans.clans.ClansManager;

public class MapCommand extends CommandBase<ClansManager>
{
	public MapCommand(ClansManager plugin)
	{
		super(plugin, ClansManager.Perm.MAP_COMMAND, "map", "clansmap");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		Plugin.getItemMapManager().setMap(caller);
	}
}