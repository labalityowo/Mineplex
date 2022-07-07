package mineplex.game.clans.clans.worldevent.command;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import mineplex.core.command.MultiCommandBase;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.game.clans.clans.worldevent.WorldEventManager;

public class WorldEventCommand extends MultiCommandBase<WorldEventManager>
{
	public WorldEventCommand(WorldEventManager plugin)
	{
		super(plugin, WorldEventManager.Perm.WORLD_EVENT_COMMAND, "worldevent", "we", "event");
		
		AddCommand(new StartCommand(Plugin));
		AddCommand(new ClearCommand(Plugin));
		AddCommand(new RandomCommand(Plugin));
	}
	
	@Override
	protected void Help(Player caller, String[] args)
	{
		UtilPlayer.message(caller, F.help("/" + _aliasUsed + " start <type>", "Start a World Event", ChatColor.DARK_RED));
		UtilPlayer.message(caller, F.help("/" + _aliasUsed + " clear", "Clears all World Events", ChatColor.DARK_RED));
		UtilPlayer.message(caller, F.help("/" + _aliasUsed + " random", "Starts a random World Event", ChatColor.DARK_RED));
	}
}