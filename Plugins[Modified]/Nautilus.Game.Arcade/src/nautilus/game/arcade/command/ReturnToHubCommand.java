package nautilus.game.arcade.command;

import org.bukkit.entity.Player;

import mineplex.core.account.permissions.Permission;
import mineplex.core.command.CommandBase;
import mineplex.core.portal.GenericServer;
import mineplex.core.portal.Intent;

import nautilus.game.arcade.ArcadeManager;

public class ReturnToHubCommand extends CommandBase<ArcadeManager>
{
	public ReturnToHubCommand(ArcadeManager plugin)
	{
		super(plugin, ArcadeManager.Perm.RETURN_TO_HUB_COMMAND, "hub", "lobby", "leave", "takemebacktoparadisecity");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		Plugin.GetPortal().sendPlayerToGenericServer(caller, GenericServer.HUB, Intent.PLAYER_REQUEST);
	}
}
