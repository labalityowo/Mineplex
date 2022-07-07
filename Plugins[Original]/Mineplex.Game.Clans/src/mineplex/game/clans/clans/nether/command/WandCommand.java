package mineplex.game.clans.clans.nether.command;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.game.clans.clans.nether.NetherManager;

/**
 * Command to give yourself a portal creation wand 
 */
public class WandCommand extends CommandBase<NetherManager>
{
	public WandCommand(NetherManager plugin)
	{
		super(plugin, NetherManager.Perm.PORTAL_CREATE_COMMAND, "wand");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		Plugin.giveWand(caller);
	}
}