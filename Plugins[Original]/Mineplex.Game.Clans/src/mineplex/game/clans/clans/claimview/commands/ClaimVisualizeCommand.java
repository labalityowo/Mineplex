package mineplex.game.clans.clans.claimview.commands;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.game.clans.clans.claimview.ClaimVisualizer;

public class ClaimVisualizeCommand extends CommandBase<ClaimVisualizer>
{
	public ClaimVisualizeCommand(ClaimVisualizer plugin)
	{
		super(plugin, ClaimVisualizer.Perm.VISUALIZE_COMMAND, "showclaims");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		Plugin.toggleVisualizer(caller);
	}
}