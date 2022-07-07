package mineplex.core.portal.commands;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.portal.Intent;
import mineplex.core.portal.Portal;

public class ServerCommand extends CommandBase<Portal>
{
	public ServerCommand(Portal plugin)
	{
		super(plugin, Portal.Perm.SERVER_COMMAND, "server");
	}

	@Override
	public void Execute(final Player player, final String[] args)
	{
		final String serverName = Plugin.getPlugin().getConfig().getString("serverstatus.name");

		if (args == null || args.length == 0)
		{
			UtilPlayer.message(player,
					F.main(Plugin.getName(), C.cGray + "You are currently on server: " + C.cGold + serverName));
		}
		else if (args.length == 1)
		{
			if (serverName.equalsIgnoreCase(args[0]))
			{
				UtilPlayer.message(
						player,
						F.main(Plugin.getName(), "You are already on " + C.cGold + serverName + C.cGray
								+ "!"));
			}
			else
			{
				Plugin.doesServerExist(args[0], serverExists ->
				{
					if (!serverExists)
					{
						UtilPlayer.message(
								player,
								F.main(Plugin.getName(), "Server " + C.cGold + args[0]
										+ C.cGray + " does not exist!"));
						return;
					}

					boolean deniedAccess = false;
					String servUp = args[0].toUpperCase();

					if (servUp.contains("STAFF"))
					{
						if (!_commandCenter.GetClientManager().Get(player).hasPermission(Portal.Perm.JOIN_STAFF))
						{
							deniedAccess = true;
						}
					}
					else if (servUp.startsWith("CLANS-"))
					{
						UtilPlayer.message(player, F.main(Plugin.getName(), "Clans servers can only be joined via the Clans Hub!"));
						return;
					}

					if (deniedAccess)
					{
						UtilPlayer.message(
								player,
								F.main(Plugin.getName(), C.cRed + "You don't have permission to join " + C.cGold + args[0]));
					}
					else
					{
						Plugin.sendPlayerToServer(player, args[0], Intent.PLAYER_REQUEST);
					}
				});
			}
		}
		else
		{
			UtilPlayer.message(player,
					F.main(Plugin.getName(), C.cRed + "Your arguments are inappropriate for this command!"));
		}
	}
}
