package nautilus.game.minekart.gp.command.gp;

import org.bukkit.entity.Player;

import nautilus.game.minekart.gp.GPManager;
import mineplex.core.command.CommandBase;
import mineplex.core.common.Rank;

public class StartCommand extends CommandBase<GPManager>
{
	public StartCommand(GPManager plugin)
	{
		super(plugin, Rank.MODERATOR, "start");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		Plugin.StartGP(true);
	}
}
