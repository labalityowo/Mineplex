package mineplex.core.punish.clans.command;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.punish.clans.ClansBanManager;
import mineplex.core.punish.clans.ui.ClansBanShop;

public class ClansBanCommand extends CommandBase<ClansBanManager>
{
	public ClansBanCommand(ClansBanManager plugin)
	{
		super(plugin, ClansBanManager.Perm.PUNISHMENT_COMMAND, "cb");
	}

	@Override
	public void Execute(final Player caller, String[] args)
	{		
		if (args == null || args.length < 1)
		{
			UtilPlayer.message(caller, C.cBlue + "/cb <username> <reason>" + C.cGray + " - " + C.cYellow + "Displays the \"Clans Punish\" GUI, allowing you to ban the player, and view their past bans.");
		}
		else if (args.length > 1)
		{
			final String playerName = args[0];
			
			StringBuilder reasonBuilder = new StringBuilder(args[1]);
			
			for (int i = 2; i < args.length; i++)
			{
				reasonBuilder.append(' ');
				reasonBuilder.append(args[i]);
			}
			
			final String finalReason = reasonBuilder.toString();
			
			Plugin.loadClient(playerName, client ->
			{
				if (client.isPresent())
				{
					new ClansBanShop(Plugin, playerName, client.get(), finalReason).attemptShopOpen(caller);
				}
				else
				{
					UtilPlayer.message(caller, C.cRed + "Could not find player with name " + C.cYellow + playerName);
				}
			});
		}
		else
		{
			UtilPlayer.message(caller, C.cBlue + "/cb <username> <reason>" + C.cGray + " - " + C.cYellow + "Displays the \"Clans Punish\" GUI, allowing you to ban the player, and view their past bans.");
		}
	}
}