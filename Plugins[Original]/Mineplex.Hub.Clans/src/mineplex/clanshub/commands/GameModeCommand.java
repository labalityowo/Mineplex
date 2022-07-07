package mineplex.clanshub.commands;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import mineplex.clanshub.HubManager;
import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;

/**
 * Command for setting gamemode
 */
public class GameModeCommand extends CommandBase<HubManager>
{
	public GameModeCommand(HubManager plugin)
	{
		super(plugin, HubManager.Perm.GAMEMODE_COMMAND, "gamemode", "gm");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		Player target = caller;
		
		if (args != null && args.length >= 1)
		{
			target = UtilPlayer.searchOnline(caller, args[0], true);
			
			if (target == null)
				return;
		}
		
		if (target.getGameMode() == GameMode.SURVIVAL)
		{
			target.setGameMode(GameMode.CREATIVE);
		}
		else
		{
			target.setGameMode(GameMode.SURVIVAL);
		}
		
		if (!target.equals(caller))
		{
			Plugin.addGameMode(caller, target);
			UtilPlayer.message(target, F.main("Game Mode", caller.getName() + " toggled your Creative Mode: " + F.tf(target.getGameMode() == GameMode.CREATIVE)));
		}
			
		UtilPlayer.message(caller, F.main("Game Mode", target.getName() + " Creative Mode: " + F.tf(target.getGameMode() == GameMode.CREATIVE)));
	}
}