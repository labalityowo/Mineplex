package nautilus.game.arcade.game.games.typewars.stats;

import java.util.HashMap;

import nautilus.game.arcade.game.games.typewars.SummonMinionEvent;
import nautilus.game.arcade.game.games.typewars.TypeWars;
import nautilus.game.arcade.stats.StatTracker;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

public class HoarderStatTracker extends StatTracker<TypeWars>
{

	private HashMap<Player, Integer> _players;
	
	public HoarderStatTracker(TypeWars game)
	{
		super(game);
		_players = new HashMap<>();
	}

	@EventHandler
	public void summonMinion(SummonMinionEvent event)
	{
		if(!_players.containsKey(event.getPlayer()))
		{
			_players.put(event.getPlayer(), 1);
			return;
		}
		int summons = _players.get(event.getPlayer());
		_players.put(event.getPlayer(), summons + 1);
		if(_players.get(event.getPlayer()) >= 50)
			addStat(event.getPlayer(), "Hoarder", 1, true, false);
	}
	
}
