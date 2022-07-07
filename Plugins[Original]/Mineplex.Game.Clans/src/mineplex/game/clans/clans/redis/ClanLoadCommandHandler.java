package mineplex.game.clans.clans.redis;

import mineplex.core.common.util.Callback;
import mineplex.core.common.util.UtilPlayer;
import mineplex.game.clans.clans.ClanInfo;
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.core.ClanDeleteCommand;
import mineplex.game.clans.core.ClanLoadCommand;
import mineplex.game.clans.core.repository.tokens.ClanToken;
import mineplex.serverdata.commands.CommandCallback;
import mineplex.serverdata.commands.ServerCommand;

public class ClanLoadCommandHandler implements CommandCallback
{

	public void run(ServerCommand command)
	{
		if (command instanceof ClanLoadCommand)
		{
			ClanLoadCommand serverCommand = (ClanLoadCommand) command;
			final String clanName = serverCommand.getClanName();
			
			ClansManager.getInstance().getClanDataAccess().retrieveClan(clanName, new Callback<ClanToken>()
			{
				@Override
				public void run(ClanToken clan)
				{
					if (clan != null)
					{
						ClansManager.getInstance().loadClan(clan);	// Load the clan data locally
						System.out.println("Successfully finished loading and transferring clan '" + clanName + "'!");
					}
					else
					{
						System.out.println("ERROR: UNABLE TO LOAD CLAN " + clanName + " DURING REMOTE CLAN LOAD COMMAND!");
					}
				}
			});
		}
	}
}
