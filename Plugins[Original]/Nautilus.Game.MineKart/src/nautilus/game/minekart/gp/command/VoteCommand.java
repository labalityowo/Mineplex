package nautilus.game.minekart.gp.command;

import org.bukkit.entity.Player;

import nautilus.game.minekart.gp.GPManager;
import mineplex.core.command.CommandBase;
import mineplex.core.common.Rank;

public class VoteCommand extends CommandBase<GPManager>
{
	public VoteCommand(GPManager plugin)
	{
		super(plugin, Rank.ALL, "vote");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		Plugin.Vote(caller);
	}
}
