package mineplex.game.clans.clans.nether.command;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import mineplex.core.command.MultiCommandBase;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.game.clans.clans.nether.NetherManager;

/**
 * Base portal command 
 */
public class PortalCommand extends MultiCommandBase<NetherManager>
{
	public PortalCommand(NetherManager plugin)
	{
		super(plugin, NetherManager.Perm.PORTAL_COMMAND, "netherportal", "portal");
		
		AddCommand(new CreateCommand(plugin));
		AddCommand(new DeleteCommand(plugin));
		AddCommand(new ListCommand(plugin));
		AddCommand(new SpawnCommand(plugin));
		AddCommand(new CloseCommand(plugin));
		AddCommand(new WandCommand(plugin));
	}

	@Override
	protected void Help(Player caller, String[] args)
	{
		UtilPlayer.message(caller, F.help("/" + _aliasUsed + " spawn", "Forces a Nether Portal to spawn", ChatColor.DARK_RED));
		UtilPlayer.message(caller, F.help("/" + _aliasUsed + " close", "Closes all Nether Portals", ChatColor.DARK_RED));
		UtilPlayer.message(caller, F.help("/" + _aliasUsed + " list", "Lists all loaded Nether Portals", ChatColor.DARK_RED));
		UtilPlayer.message(caller, F.help("/" + _aliasUsed + " wand", "Gives you a Nether Portal claim wand", ChatColor.DARK_RED));
		UtilPlayer.message(caller, F.help("/" + _aliasUsed + " create", "Creates a Nether Portal with the corners you have selected", ChatColor.DARK_RED));
		UtilPlayer.message(caller, F.help("/" + _aliasUsed + " delete", "Deletes a loaded Nether Portal", ChatColor.DARK_RED));
	}
}