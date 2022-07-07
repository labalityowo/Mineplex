package nautilus.game.arcade.game.games.typewars.stats;

import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.game.Game.GameState;
import nautilus.game.arcade.stats.StatTracker;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

public class TimeInGameTracker extends StatTracker<Game>
{

	public TimeInGameTracker(Game game)
	{
		super(game);
	}
	
	@EventHandler
	public void end(GameStateChangeEvent event)
	{
		if(event.GetState() != GameState.End)
			return;
		
		for(Player player : getGame().GetPlayers(true))
		{
			addStat(player, "TimeInGame", (int) (((System.currentTimeMillis() - getGame().getGameLiveTime())/1000)/60), false, false);
		}
		
	}

}
