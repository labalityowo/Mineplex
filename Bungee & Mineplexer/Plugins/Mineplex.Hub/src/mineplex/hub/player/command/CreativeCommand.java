package mineplex.hub.player.command;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.hub.HubManager;
import mineplex.hub.player.CreativeManager;

public class CreativeCommand extends CommandBase<CreativeManager>
{

	public CreativeCommand(CreativeManager plugin)
	{
		super(plugin, HubManager.Perm.GAMEMODE_COMMAND, "gm", "gamemode");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		Player target = caller;

		if (args != null && args.length >= 1)
		{
			target = UtilPlayer.searchOnline(caller, args[0], true);

			if (target == null)
			{
				return;
			}
		}

		boolean enabled = target.getGameMode() == GameMode.CREATIVE;

		target.setGameMode(enabled ? GameMode.ADVENTURE : GameMode.CREATIVE);
		Plugin.addGameMode(caller, target);

		if (!target.equals(caller))
		{
			UtilPlayer.message(target, F.main(Plugin.getName(), F.name(caller.getName()) + " toggled your Creative Mode: " + F.tf(!enabled)));
		}

		UtilPlayer.message(caller, F.main(Plugin.getName(), F.name(target.getName()) + " Creative Mode: " + F.tf(!enabled)));
	}
}