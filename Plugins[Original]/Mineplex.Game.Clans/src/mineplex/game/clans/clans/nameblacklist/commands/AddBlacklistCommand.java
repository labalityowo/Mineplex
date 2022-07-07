package mineplex.game.clans.clans.nameblacklist.commands;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.slack.SlackAPI;
import mineplex.core.slack.SlackMessage;
import mineplex.core.slack.SlackTeam;
import mineplex.game.clans.clans.nameblacklist.ClansBlacklist;

public class AddBlacklistCommand extends CommandBase<ClansBlacklist>
{
	public AddBlacklistCommand(ClansBlacklist plugin)
	{
		super(plugin, ClansBlacklist.Perm.BLACKLIST_COMMAND, "blacklistname");
	}

	@Override
	public void Execute(final Player caller, String[] args)
	{		
		if (args == null || args.length < 1)
		{
			UtilPlayer.message(caller, C.cGold + "/blacklistname <clanName> - Blacklists a clan name.");
		}
		else if (args.length >= 1)
		{
			final String blacklist = args[0];
			
			Plugin.runAsync(() ->
			{
				Plugin.getRepository().add(blacklist, caller.getName());
				UtilPlayer.message(caller, F.main("Clans", "Successfully added " + F.elem(blacklist) + " to the clan name blacklist."));
				if (!UtilServer.isTestServer())
				{
					SlackAPI.getInstance().sendMessage(SlackTeam.DEVELOPER, "#clans-commandspy",
							new SlackMessage("Clans Command Logger", "crossed_swords", caller.getName() + " has blacklisted the clan name " + blacklist + "."),
							true);
				}
			});
		}
		else
		{
			UtilPlayer.message(caller, C.cGold + "/blacklistname <clanName> - Blacklists a clan name.");
		}
	}
}