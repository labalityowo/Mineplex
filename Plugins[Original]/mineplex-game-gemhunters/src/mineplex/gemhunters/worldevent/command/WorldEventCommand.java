package mineplex.gemhunters.worldevent.command;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import mineplex.core.command.MultiCommandBase;
import mineplex.core.common.util.F;
import mineplex.gemhunters.worldevent.WorldEventModule;

public class WorldEventCommand extends MultiCommandBase<WorldEventModule>
{
	public WorldEventCommand(WorldEventModule plugin)
	{
		super(plugin, WorldEventModule.Perm.WORLD_EVENT_COMMAND, "worldevent", "we");
		
		AddCommand(new StartCommand(plugin));
		AddCommand(new StopCommand(plugin));
	}

	@Override
	protected void Help(Player caller, String[] args)
	{
		caller.sendMessage(F.main(Plugin.getName(), "Command List:"));
		caller.sendMessage(F.help("/" + _aliasUsed + " start [name]", "Starts a world event. Leaving [name] blank picks a random one.", ChatColor.DARK_RED));
		caller.sendMessage(F.help("/" + _aliasUsed + " stop [name]", "Stops a world event. Leaving [name] blank stops all events.", ChatColor.DARK_RED));
	}
}