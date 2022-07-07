package mineplex.game.clans.clans.freeze.commands;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.game.clans.clans.freeze.ClansFreezeManager;

/**
 * Command to freeze players
 */
public class FreezeCommand extends CommandBase<ClansFreezeManager>
{
	public FreezeCommand(ClansFreezeManager plugin)
	{
		super(plugin, ClansFreezeManager.Perm.FREEZE_COMMAND, "freeze");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{		
		if (args == null || args.length < 1)
		{
			UtilPlayer.message(caller, C.cBlue + "/freeze <username>" + C.cGray + " - " + C.cYellow + "Freezes a player, restricting their movement and ability to interact with the game.");
		}
		else if (args.length > 0)
		{
			Player target = UtilPlayer.searchOnline(caller, args[0], true);
			if (target == null)
			{
				return;
			}
			if (Plugin.isFrozen(target))
			{
				UtilPlayer.message(caller, F.main(Plugin.getName(), F.elem(args[0]) + " is already frozen!"));
				return;
			}
			Plugin.freeze(target, caller);
		}
	}
}