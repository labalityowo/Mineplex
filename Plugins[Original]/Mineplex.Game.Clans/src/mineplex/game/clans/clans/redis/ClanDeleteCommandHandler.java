package mineplex.game.clans.clans.redis;

import mineplex.core.common.util.UtilPlayer;
import mineplex.game.clans.clans.ClanInfo;
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.core.ClanDeleteCommand;
import mineplex.serverdata.commands.CommandCallback;
import mineplex.serverdata.commands.ServerCommand;

public class ClanDeleteCommandHandler implements CommandCallback
{

	public void run(ServerCommand command)
	{
		if (command instanceof ClanDeleteCommand)
		{
			ClanDeleteCommand serverCommand = (ClanDeleteCommand) command;
			String clanName = serverCommand.getClanName();
			ClanInfo clanInfo = ClansManager.getInstance().getClan(clanName);
			
			if (clanInfo != null)
			{
				// Kick all online players from clan and delete clan info locally
				UtilPlayer.kick(clanInfo.getOnlinePlayers(), "Clans", "Your clan leader has moved your clan to another server!", true);
				ClansManager.getInstance().getClanDataAccess().deleteLocally(clanInfo);	
			}
		}
	}
}
