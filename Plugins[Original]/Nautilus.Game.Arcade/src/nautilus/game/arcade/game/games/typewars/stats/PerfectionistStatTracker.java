package nautilus.game.arcade.game.games.typewars.stats;

import java.util.ArrayList;
import java.util.HashMap;

import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.Game.GameState;
import nautilus.game.arcade.game.games.typewars.TypeAttemptEvent;
import nautilus.game.arcade.game.games.typewars.TypeWars;
import nautilus.game.arcade.stats.StatTracker;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

public class PerfectionistStatTracker extends StatTracker<TypeWars>
{

	private HashMap<String, Long> _wordsRecently;
	private ArrayList<Player> _players;
	private HashMap<Player, Integer> _playerWords;
	
	public PerfectionistStatTracker(TypeWars game)
	{
		super(game);
		_wordsRecently = new HashMap<>();
		_players = new ArrayList<>();
		_playerWords = new HashMap<>();
	}
	
	@EventHandler
	public void attempt(TypeAttemptEvent event)
	{
		if(event.isSuccessful())
		{
			_wordsRecently.put(event.getAttempt().toUpperCase(), System.currentTimeMillis());
			
			if(!_playerWords.containsKey(event.getPlayer()))
				_playerWords.put(event.getPlayer(), 1);
			
			int words = _playerWords.get(event.getPlayer());
			_playerWords.put(event.getPlayer(), words + 1);
		}
		else
		{
			if(_wordsRecently.containsKey(event.getAttempt().toUpperCase()))
			{
				if(_wordsRecently.get(event.getAttempt().toUpperCase()) + 2000 > System.currentTimeMillis())
				{
					return;
				}
			}
			_players.remove(event.getPlayer());
		}
	}
	
	@EventHandler
	public void gameState(GameStateChangeEvent event)
	{
		if(event.GetState() == GameState.Live)
		{
			for(Player player : event.GetGame().GetPlayers(true))
			{
				_players.add(player);
			}
		}
		if(event.GetState() == GameState.End)
		{
			for(Player player : _players)
			{
				if(player.isOnline())
				{
					if(_playerWords.containsKey(player))
					{
						if(_playerWords.get(player) >= 5)
							addStat(player, "Perfectionist", 1, true, false);
					}
				}
			}
		}
	}

}
