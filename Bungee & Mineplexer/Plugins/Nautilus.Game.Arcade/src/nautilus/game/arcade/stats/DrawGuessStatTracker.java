package nautilus.game.arcade.stats;

import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import nautilus.game.arcade.game.Game;

/**
 * Created by luke1 on 28/01/2016.
 */
public class DrawGuessStatTracker extends StatTracker<Game>
{
	public DrawGuessStatTracker(Game game)
	{
		super(game);
	}

	@EventHandler
	public void guess(AsyncPlayerChatEvent event)
	{
		if(getGame().GetState() != Game.GameState.Live)
			return;

		int i = 0;
		if(event.getMessage().contains(" "))
		{
			for(char c : event.getMessage().toCharArray())
			{
				System.out.println(c);
				if(c != ' ')
					continue;

				i++;
			}
		}

		if(i <= 1)
			addStat(event.getPlayer(), "TotalGuess", 1, false, false);
	}
}
