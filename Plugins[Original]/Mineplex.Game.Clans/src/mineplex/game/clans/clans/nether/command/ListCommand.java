package mineplex.game.clans.clans.nether.command;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.game.clans.clans.nether.NetherManager;

/**
 * Command to list all nether portals 
 */
public class ListCommand extends CommandBase<NetherManager>
{
	public ListCommand(NetherManager plugin)
	{
		super(plugin, NetherManager.Perm.PORTAL_LIST_COMMAND, "list");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		Plugin.showPortalList(caller);
	}
}