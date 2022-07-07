package mineplex.game.clans.clans.freeze.commands;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.game.clans.clans.freeze.ClansFreezeManager;

/**
 * Command to unfreeze players
 */
public class UnfreezeCommand extends CommandBase<ClansFreezeManager>
{
	public UnfreezeCommand(ClansFreezeManager plugin)
	{
		super(plugin, ClansFreezeManager.Perm.UNFREEZE_COMMAND, "unfreeze");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{		
		if (args == null || args.length < 1)
		{
			UtilPlayer.message(caller, C.cBlue + "/unfreeze <username>" + C.cGray + " - " + C.cYellow + "Unfreezes a player, restoring their movement and ability to interact with the game.");
		}
		else if (args.length > 0)
		{
			Player target = UtilPlayer.searchOnline(caller, args[0], true);
			if (target == null)
			{
				return;
			}
			if (!Plugin.isFrozen(target))
			{
				UtilPlayer.message(caller, F.main(Plugin.getName(), F.elem(args[0]) + " is not frozen!"));
				return;
			}
			Plugin.unfreeze(target, caller);
		}
	}
}