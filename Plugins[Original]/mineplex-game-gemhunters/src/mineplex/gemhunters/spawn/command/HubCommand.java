package mineplex.gemhunters.spawn.command;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.portal.GenericServer;
import mineplex.core.portal.Intent;
import mineplex.core.portal.Portal;
import mineplex.gemhunters.spawn.SpawnModule;

public class HubCommand extends CommandBase<SpawnModule>
{
	public HubCommand(SpawnModule plugin)
	{
		super(plugin, SpawnModule.Perm.HUB_COMMAND, "hub", "lobby");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		Portal.getInstance().sendPlayerToGenericServer(caller, GenericServer.HUB, Intent.PLAYER_REQUEST);
	}
}