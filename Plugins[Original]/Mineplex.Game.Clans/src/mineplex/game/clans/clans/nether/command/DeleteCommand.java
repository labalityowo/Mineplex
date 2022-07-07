package mineplex.game.clans.clans.nether.command;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.game.clans.clans.nether.NetherManager;
import mineplex.game.clans.clans.nether.NetherPortal;

/**
 * Command to delete a nether portal
 */
public class DeleteCommand extends CommandBase<NetherManager>
{
	public DeleteCommand(NetherManager plugin)
	{
		super(plugin, NetherManager.Perm.PORTAL_DELETE_COMMAND, "delete", "remove");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		Integer id = null;
		try
		{
			id = Integer.parseInt(args[0]);
		}
		catch (Exception e) {}
		if (id == null || Plugin.getPortal(id) == null)
		{
			UtilPlayer.message(caller, F.main(Plugin.getName(), "Usage: " + F.elem("/portal " + _aliasUsed + " <ID>")));
			return;
		}
		NetherPortal portal = Plugin.getPortal(id);
		UtilPlayer.message(caller, F.main(Plugin.getName(), "Deleting the " + F.clansNether("Nether Portal") + " with ID " + id + "!"));
		Plugin.deletePortal(portal);
	}
}