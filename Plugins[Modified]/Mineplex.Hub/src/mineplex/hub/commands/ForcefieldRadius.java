package mineplex.hub.commands;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.hub.modules.ForcefieldManager;

public class ForcefieldRadius extends CommandBase<ForcefieldManager>
{
	public ForcefieldRadius(ForcefieldManager plugin)
	{
		super(plugin, ForcefieldManager.Perm.FORCEFIELD_RADIUS_COMMAND, "radius");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		Plugin.ForcefieldRadius(caller, args);
	}
}