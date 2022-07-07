package mineplex.game.clans.clans.nether.command;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.game.clans.clans.nether.NetherManager;

/**
 * Command to open a nether portal 
 */
public class SpawnCommand extends CommandBase<NetherManager>
{
	public SpawnCommand(NetherManager plugin)
	{
		super(plugin, NetherManager.Perm.PORTAL_OPEN_COMMAND, "spawn");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		UtilPlayer.message(caller, F.main(Plugin.getName(), "Spawning a " + F.clansNether("Nether Portal" + "!")));
		Plugin.spawnPortal();
	}
}