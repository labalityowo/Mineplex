package mineplex.clanshub.commands;

import org.bukkit.entity.Player;

import mineplex.clanshub.ForcefieldManager;
import mineplex.core.command.CommandBase;

/**
 * Command for controlling forcefield radius
 */
public class ForcefieldRadius extends CommandBase<ForcefieldManager>
{
	public ForcefieldRadius(ForcefieldManager plugin)
	{
		super(plugin, ForcefieldManager.Perm.FORCEFIELD_RADIUS_COMMAND, "radius", "forcefield");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		Plugin.ForcefieldRadius(caller, args);
	}
}