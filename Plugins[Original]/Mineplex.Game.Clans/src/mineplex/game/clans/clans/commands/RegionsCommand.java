package mineplex.game.clans.clans.commands;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.game.clans.clans.ClansManager;

public class RegionsCommand extends CommandBase<ClansManager>
{
	private ClansManager _manager;
	
	public RegionsCommand(ClansManager plugin)
	{
		super(plugin, ClansManager.Perm.REGION_CLEAR_COMMAND, "region-reset");
		
		_manager = plugin;
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		UtilPlayer.message(caller, F.main("Regions", "Resetting clans regions!"));
		_manager.getClanRegions().resetRegions();
	}
}