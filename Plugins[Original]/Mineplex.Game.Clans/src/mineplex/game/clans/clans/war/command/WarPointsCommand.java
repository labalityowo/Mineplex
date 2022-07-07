package mineplex.game.clans.clans.war.command;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTime;
import mineplex.game.clans.Clans;
import mineplex.game.clans.clans.ClanInfo;
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.clans.war.WarManager;
import mineplex.game.clans.core.war.ClanWarData;

public class WarPointsCommand extends CommandBase<WarManager>
{
	public WarPointsCommand(WarManager plugin)
	{
		super(plugin, WarManager.Perm.WAR_POINT_COMMAND, "warpoints", "wp", "dom");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		if (!Clans.HARDCORE)
		{
			UtilPlayer.message(caller, F.main("Clans", "This is not a hardcore server!"));
			return;
		}
		ClansManager clansManager = Plugin.getClansManager();
		ClanInfo clan = clansManager.getClan(caller);

		if (clan == null)
		{
			UtilPlayer.message(caller, F.main("War", "You are not in a clan"));
			return;
		}

		if (args.length == 1)
		{
			ClanInfo search = clansManager.getClanUtility().searchClanPlayer(caller, args[0], true);
			if (search == null) return;

			String searchName = clansManager.getClanUtility().name(search, clan);
			String selfName = clansManager.getClanUtility().name(clan, clan);

			ClanWarData war = clan.getWarData(search);
			if (war == null)
			{
				UtilPlayer.message(caller, F.main("War", "War Status with " + searchName));
				UtilPlayer.message(caller, F.main("War", "War Points: " + F.elem("" + WarManager.WAR_START_POINTS)));
			}
			else
			{
				UtilPlayer.message(caller, F.main("War", "War Status with " + searchName));
				UtilPlayer.message(caller, F.main("War", "Initiated by: " + (war.getClanA().equals(clan.getName()) ? selfName : searchName)));
				UtilPlayer.message(caller, F.main("War", "War Points: " + clan.getFormattedWarPoints(search)));
				UtilPlayer.message(caller, F.main("War", "Age: " + F.elem(UtilTime.convertString(System.currentTimeMillis() - war.getTimeFormed().getTime(), 1, UtilTime.TimeUnit.FIT))));
			}
		}
	}
}