package nautilus.game.arcade.game.games.cakewars.trackers;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import mineplex.core.common.util.UtilTime;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.games.cakewars.CakeWars;
import nautilus.game.arcade.game.games.cakewars.event.CakeWarsEatCakeEvent;
import nautilus.game.arcade.stats.StatTracker;

public class Survive10Tracker extends StatTracker<CakeWars>
{

	private static final long TIME = TimeUnit.MINUTES.toMillis(10);

	private final Map<GameTeam, Long> _cakeDestroyTime;

	public Survive10Tracker(CakeWars game)
	{
		super(game);

		_cakeDestroyTime = new HashMap<>(8);
	}

	@EventHandler
	public void cakeEat(CakeWarsEatCakeEvent event)
	{
		_cakeDestroyTime.put(event.getGameTeam(), System.currentTimeMillis());
	}

	@EventHandler
	public void updateAchievement(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
		{
			return;
		}

		_cakeDestroyTime.keySet().removeIf(gameTeam ->
		{
			long time = _cakeDestroyTime.get(gameTeam);

			if (UtilTime.elapsed(time, TIME))
			{
				for (Player player : gameTeam.GetPlayers(true))
				{
					addStat(player, "Survive10", 1, true, false);
				}

				return true;
			}

			return false;
		});
	}
}
