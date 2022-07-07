package mineplex.game.clans.clans.nether.command;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.game.clans.clans.nether.NetherManager;

/**
 * Command to create a nether portal
 */
public class CreateCommand extends CommandBase<NetherManager>
{
	public CreateCommand(NetherManager plugin)
	{
		super(plugin, NetherManager.Perm.PORTAL_CREATE_COMMAND, "create");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		Boolean returnPortal = null;
		try
		{
			returnPortal = Boolean.parseBoolean(args[0]);
		}
		catch (Exception e) {}
		
		if (returnPortal == null)
		{
			UtilPlayer.message(caller, F.main(Plugin.getName(), "Usage: " + F.elem("/portal " + _aliasUsed + " <ReturnPortal>")));
			return;
		}
		
		Plugin.createPortal(caller, returnPortal);
	}
}