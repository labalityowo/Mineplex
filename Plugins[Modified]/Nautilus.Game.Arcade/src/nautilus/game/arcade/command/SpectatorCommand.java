package nautilus.game.arcade.command;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import nautilus.game.arcade.ArcadeManager;

public class SpectatorCommand extends CommandBase<ArcadeManager>
{
	public SpectatorCommand(ArcadeManager plugin)
	{
		super(plugin, ArcadeManager.Perm.SPECTATOR_COMMAND, "spectator", "spec");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		Plugin.toggleSpectator(caller);
	}
}
