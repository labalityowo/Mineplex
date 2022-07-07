package mineplex.game.clans.clans.worldevent.command;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.game.clans.clans.worldevent.WorldEventManager;
import mineplex.game.clans.clans.worldevent.WorldEventType;

/**
 * Command for spawning a random world event in the world.
 */
public class RandomCommand extends CommandBase<WorldEventManager>
{
	public RandomCommand(WorldEventManager plugin)
	{
		super(plugin, WorldEventManager.Perm.START_EVENT_COMMAND, "random", "rand");
	}
	
	@Override
	public void Execute(Player caller, String[] args)
	{
		WorldEventType typeStarted = Plugin.randomEvent();
		if (typeStarted != null)
		{
			UtilPlayer.message(caller, F.main(Plugin.getName(), "Started " + F.name(typeStarted.getName())));
		}
		else
		{
			UtilPlayer.message(caller, F.main(Plugin.getName(), "Could not start World Event"));
		}
	}
}