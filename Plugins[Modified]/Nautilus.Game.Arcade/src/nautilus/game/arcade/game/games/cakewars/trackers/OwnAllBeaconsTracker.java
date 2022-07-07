package nautilus.game.arcade.game.games.cakewars.trackers;

import org.bukkit.event.EventHandler;

import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.games.cakewars.CakeWars;
import nautilus.game.arcade.game.modules.capturepoint.CapturePoint;
import nautilus.game.arcade.game.modules.capturepoint.CapturePointCaptureEvent;
import nautilus.game.arcade.stats.StatTracker;

public class OwnAllBeaconsTracker extends StatTracker<CakeWars>
{

	public OwnAllBeaconsTracker(CakeWars game)
	{
		super(game);
	}

	@EventHandler
	public void capture(CapturePointCaptureEvent event)
	{
		GameTeam team = event.getPoint().getOwner();

		for (CapturePoint point : getGame().getCapturePointModule().getCapturePoints())
		{
			if (point.getOwner() == null || !point.getOwner().equals(team))
			{
				return;
			}
		}

		team.GetPlayers(true).forEach(player -> addStat(player, "OwnAllBeacons", 1, true, false));
	}

}
