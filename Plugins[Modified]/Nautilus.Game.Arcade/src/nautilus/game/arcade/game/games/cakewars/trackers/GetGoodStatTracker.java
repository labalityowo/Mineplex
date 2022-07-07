package nautilus.game.arcade.game.games.cakewars.trackers;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;

import mineplex.core.common.util.UtilPlayer;

import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.stats.StatTracker;

public class GetGoodStatTracker extends StatTracker<Game>
{

	private static final int MIN_FALL_DISTANCE = 15;

	public GetGoodStatTracker(Game game)
	{
		super(game);
	}

	@EventHandler
	public void playerDeath(PlayerDeathEvent event)
	{
		Player player = event.getEntity();
		Player killer = player.getKiller();

		if (killer == null || killer.getFallDistance() < MIN_FALL_DISTANCE)
		{
			return;
		}

		getGame().getArcadeManager().runSyncLater(() ->
		{
			if (!killer.isOnline() || UtilPlayer.isSpectator(killer))
			{
				return;
			}

			addStat(killer, "GetGood", 1, true, false);
		}, 60);
	}
}
