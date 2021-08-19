package nautilus.game.arcade.command;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import nautilus.game.arcade.ArcadeManager;

/**
 *
 */
public class CancelNextGameCommand extends CommandBase<ArcadeManager>
{
	public CancelNextGameCommand(ArcadeManager plugin)
	{
		super(plugin, ArcadeManager.Perm.NEXT_BEST_GAME, "cancelsendingtonextbestgame");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		Plugin.getNextBestGameManager().cancel(caller, Plugin.getPartyManager().getPartyByPlayer(caller));
	}
}