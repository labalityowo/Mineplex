package mineplex.game.clans.clans.observer.command;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.game.clans.clans.observer.ObserverManager;

public class ObserverCommand extends CommandBase<ObserverManager>
{
	public ObserverCommand(ObserverManager plugin)
	{
		super(plugin, ObserverManager.Perm.OBSERVE_COMMAND, "observer", "o");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		if (args == null || args.length == 0)
		{
			// Toggle Observer Mode
			if (Plugin.isObserver(caller))
			{
				Plugin.removeObserver(caller);
				UtilPlayer.message(caller, F.main("Observer", "You are no longer in " + F.elem("Observer Mode")));
			}
			else
			{
				if (!Plugin.canEnterObserverMode(caller, true))
				{
					return;
				}

				Plugin.setObserver(caller);
				UtilPlayer.message(caller, F.main("Observer", "You have entered " + F.elem("Observer Mode")));
			}
		}
		else
		{
			String playername = args[0];
			Player player = UtilPlayer.searchOnline(caller, playername, true);

			if (player != null)
			{
				// Observe Player

				if (Plugin.isObserver(caller))
				{
					caller.teleport(player);
					UtilPlayer.message(caller, F.main("Observer", "Teleported to " + F.elem(player.getName())));
				}
				else
				{
					UtilPlayer.message(caller, F.main("Observer", "You must enter " + F.elem("Observer Mode") + " to teleport"));
				}
			}
		}
	}
}