package mineplex.game.clans.clans.playtime.command;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.stats.StatsManager;
import mineplex.game.clans.clans.ClansPlayerStats;
import mineplex.game.clans.clans.playtime.Playtime;

public class PlayTimeCommand extends CommandBase<StatsManager>
{
	private Playtime _playTracker;

	public PlayTimeCommand(StatsManager plugin, Playtime tracker)
	{
		super(plugin, Playtime.Perm.CLANS_TIME_COMMAND, "clanstime");
		
		_playTracker = tracker;
	}
	
	@Override
	public void Execute(final Player caller, final String[] args)
	{
		if (args == null || args.length == 0)
		{
			UtilPlayer.message(caller, F.main("Clans", "Usage: /clanstime <playerName>"));
		}
		else
		{
			final Player target = UtilPlayer.searchOnline(caller, args[0], false);

			if (target == null)
			{
				Plugin.getOfflinePlayerStats(args[0], stats ->
				{
					if (stats == null)
					{
						UtilPlayer.message(caller, F.main("Clans", "Player " + F.elem(args[0]) + " not found!"));
					}
					else
					{
						long time = stats.getStat(ClansPlayerStats.PLAY_TIME.id());
						UtilPlayer.message(caller, F.main("Clans", F.name(args[0]) + " has spent " + F.elem(UtilTime.convertString(time * 1000L, 1, UtilTime.TimeUnit.FIT)) + " playing Clans."));
					}
				});
			}
			else
			{
				long time = Plugin.Get(target).getStat(ClansPlayerStats.PLAY_TIME.id());
				UtilPlayer.message(caller, F.main("Clans", F.name(target.getName()) + " has spent " + F.elem(UtilTime.convertString(time * 1000L, 1, UtilTime.TimeUnit.FIT) + " (+" + UtilTime.MakeStr(_playTracker.getUnsavedPlaytime(target) * 1000) + ")") + " playing Clans."));
			}
		}
	}
}