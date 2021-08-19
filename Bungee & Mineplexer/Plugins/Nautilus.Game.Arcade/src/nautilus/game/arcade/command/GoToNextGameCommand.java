package nautilus.game.arcade.command;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import nautilus.game.arcade.ArcadeManager;

/**
 *
 */
public class GoToNextGameCommand extends CommandBase<ArcadeManager>
{
	public GoToNextGameCommand(ArcadeManager plugin)
	{
		super(plugin, ArcadeManager.Perm.NEXT_BEST_GAME, "gotonextbestgame", "nextgame", "nbg");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		Plugin.getNextBestGameManager().onCommand(caller);
	}
}