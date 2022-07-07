package nautilus.game.arcade.game.games.typewars.stats;

import java.util.ArrayList;

import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.Game.GameState;
import nautilus.game.arcade.game.games.typewars.ActivateSpellEvent;
import nautilus.game.arcade.game.games.typewars.TypeWars;
import nautilus.game.arcade.stats.StatTracker;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

public class DumbledontStatTracker extends StatTracker<TypeWars>
{

	private ArrayList<Player> _players;
	
	public DumbledontStatTracker(TypeWars game)
	{
		super(game);
		_players = new ArrayList<>();
	}
	
	@EventHandler
	public void spell(ActivateSpellEvent event)
	{
		_players.remove(event.getPlayer());
	}
	
	@EventHandler
	public void end(GameStateChangeEvent event)
	{
		if(event.GetState() == GameState.Live)
		{
			for(Player player : event.GetGame().GetPlayers(true))
				_players.add(player);
		}
		if(event.GetState() == GameState.End)
		{
			for(Player player : _players)
			{
				if(player.isOnline())
				{
					if(event.GetGame().GetTeam(player) == event.GetGame().WinnerTeam)
						addStat(player, "Dumbledont", 1, true, false);
				}
			}
		}
	}

}
