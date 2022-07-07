package mineplex.game.clans.clans.nether.command;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.game.clans.clans.nether.NetherManager;

/**
 * Command to close all nether portals
 */
public class CloseCommand extends CommandBase<NetherManager>
{
	public CloseCommand(NetherManager plugin)
	{
		super(plugin, NetherManager.Perm.PORTAL_CLOSE_COMMAND, "close");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		UtilPlayer.message(caller, F.main(Plugin.getName(), "Closing all " + F.clansNether("Nether Portals") + "!"));
		Plugin.closePortals();
	}
}