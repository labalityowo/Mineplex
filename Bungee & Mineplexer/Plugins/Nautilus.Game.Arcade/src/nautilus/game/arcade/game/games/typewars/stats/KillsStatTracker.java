package nautilus.game.arcade.game.games.typewars.stats;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.Game.GameState;
import nautilus.game.arcade.game.games.typewars.TypeWars;
import nautilus.game.arcade.stats.StatTracker;

public class KillsStatTracker extends StatTracker<TypeWars>
{

	private TypeWars _typeWars;
	
	public KillsStatTracker(TypeWars game)
	{
		super(game);
		_typeWars = game;
	}
	
	@EventHandler
	public void end(GameStateChangeEvent event)
	{
		if(event.GetState() != GameState.End)
			return;
		
		for(Player player : _typeWars.GetPlayers(true))
		{
			addStat(player, "MinionKills", _typeWars.getPlayerKills(player), false, false);
		}
		
	}

}
