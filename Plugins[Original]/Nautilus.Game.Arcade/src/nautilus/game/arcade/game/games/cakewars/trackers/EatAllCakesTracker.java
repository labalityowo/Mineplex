package nautilus.game.arcade.game.games.cakewars.trackers;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.Game.GameState;
import nautilus.game.arcade.game.games.cakewars.CakeWars;
import nautilus.game.arcade.game.games.cakewars.event.CakeWarsEatCakeEvent;
import nautilus.game.arcade.stats.StatTracker;

public class EatAllCakesTracker extends StatTracker<CakeWars>
{

	private Player _lastPlayer;
	private boolean _award;

	public EatAllCakesTracker(CakeWars game)
	{
		super(game);

		_award = true;
	}

	@EventHandler
	public void cakeEat(CakeWarsEatCakeEvent event)
	{
		if (!_award)
		{
			return;
		}

		Player player = event.getPlayer();

		if (_lastPlayer == null)
		{
			_lastPlayer = player;
		}
		else if (!_lastPlayer.equals(player))
		{
			_award = false;
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onGameStateChange(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.End || _lastPlayer == null || !_award)
		{
			return;
		}

		addStat(_lastPlayer, "FinalBite", 1, true, false);
	}
}
