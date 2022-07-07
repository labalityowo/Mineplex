package mineplex.core.portal.commands;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.portal.Portal;

public class SendCommand extends CommandBase<Portal>
{
	public SendCommand(Portal plugin)
	{
		super(plugin, Portal.Perm.SEND_COMMAND, "send");
	}
	
	@Override
	public void Execute(final Player player, final String[] args)
	{		
		if (args == null || args.length == 0)
		{
			UtilPlayer.message(player, F.main(Plugin.getName(), C.cRed + "Your arguments are inappropriate for this command!"));
			return;
		}
		else if (args.length == 2)
		{
			final String playerTarget = args[0];
			final String serverTarget = args[1];
			
			_commandCenter.GetClientManager().checkPlayerName(player, playerTarget, playerName ->
			{
				if (playerName == null)
				{
					UtilPlayer.message(player, F.main(Plugin.getName(), C.cGray + "Player " + C.cGold + playerTarget + C.cGray + " does not exist!"));
					return;
				}

				Plugin.doesServerExist(serverTarget, serverExists ->
				{
					if (!serverExists)
					{
						UtilPlayer.message(player, F.main(Plugin.getName(), C.cGray + "Server " + C.cGold + serverTarget + C.cGray + " does not exist!"));
						return;
					}

					Portal.transferPlayer(playerName, serverTarget);

					UtilPlayer.message(player, F.main(Plugin.getName(), C.cGray + "You have sent player: " + C.cGold + playerName + C.cGray + " to server: " + C.cGold + serverTarget + C.cGray + "!"));
					return;
				});
			});
		}
		else
		{
			UtilPlayer.message(player, F.main(Plugin.getName(), C.cRed + "Your arguments are inappropriate for this command!"));
			return;
		}
	}
}
