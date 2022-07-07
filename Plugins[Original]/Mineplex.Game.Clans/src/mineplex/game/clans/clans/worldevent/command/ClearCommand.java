package mineplex.game.clans.clans.worldevent.command;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.game.clans.clans.worldevent.WorldEventManager;

public class ClearCommand extends CommandBase<WorldEventManager>
{
	public ClearCommand(WorldEventManager plugin)
	{
		super(plugin, WorldEventManager.Perm.STOP_EVENT_COMMAND, "clear");
	}
	
	@Override
	public void Execute(Player caller, String[] args)
	{
		Plugin.clearEvents();
		UtilPlayer.message(caller, F.main(Plugin.getName(), "All world events cleared!"));
	}
}