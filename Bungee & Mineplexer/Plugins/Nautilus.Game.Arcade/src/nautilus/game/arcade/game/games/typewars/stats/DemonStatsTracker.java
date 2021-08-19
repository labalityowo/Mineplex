package nautilus.game.arcade.game.games.typewars.stats;

import java.util.HashMap;

import nautilus.game.arcade.game.games.typewars.MinionKillEvent;
import nautilus.game.arcade.game.games.typewars.TypeWars;
import nautilus.game.arcade.game.games.typewars.TypeWars.KillType;
import nautilus.game.arcade.stats.StatTracker;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

public class DemonStatsTracker extends StatTracker<TypeWars>
{
	
	private HashMap<Player, Long> _players;
	private HashMap<Player, Integer> _kills;

	public DemonStatsTracker(TypeWars game)
	{
		super(game);
		_players = new HashMap<>();
		_kills = new HashMap<>();
	}
	
	@EventHandler
	public void minonKill(MinionKillEvent event)
	{
		if(event.getType() != KillType.TYPED)
			return;
		
		if(!_players.containsKey(event.getPlayer()))
		{
			_players.put(event.getPlayer(), System.currentTimeMillis());
			_kills.put(event.getPlayer(), 1);
			return;
		}
		if(_players.get(event.getPlayer()) + 8000 > System.currentTimeMillis())
		{
			int kills = _kills.get(event.getPlayer());
			_kills.put(event.getPlayer(), kills + 1);
		}
		else
		{
			_players.put(event.getPlayer(), System.currentTimeMillis());
			_kills.put(event.getPlayer(), 1);
		}
		if(_kills.get(event.getPlayer()) >= 5)
		{
			addStat(event.getPlayer(), "Demon", 1, true, false);
		}
	}

}
