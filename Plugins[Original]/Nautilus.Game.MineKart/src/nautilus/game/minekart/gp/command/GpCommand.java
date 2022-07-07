package nautilus.game.minekart.gp.command;

import org.bukkit.entity.Player;

import nautilus.game.minekart.gp.GPManager;
import nautilus.game.minekart.gp.command.gp.FinishCommand;
import nautilus.game.minekart.gp.command.gp.StartCommand;
import mineplex.core.command.MultiCommandBase;
import mineplex.core.common.Rank;

public class GpCommand extends MultiCommandBase<GPManager>
{
	public GpCommand(GPManager plugin)
	{
		super(plugin, Rank.MODERATOR, "gp");
		
		AddCommand(new StartCommand(plugin));
		AddCommand(new FinishCommand(plugin));
	}

	@Override
	protected void Help(Player caller, String[] args)
	{

	}
}
