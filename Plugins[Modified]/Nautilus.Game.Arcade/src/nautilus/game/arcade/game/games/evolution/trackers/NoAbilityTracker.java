package nautilus.game.arcade.game.games.evolution.trackers;

import java.util.ArrayList;
import java.util.List;

import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.Game.GameState;
import nautilus.game.arcade.game.games.evolution.Evolution;
import nautilus.game.arcade.game.games.evolution.events.EvolutionAbilityUseEvent;
import nautilus.game.arcade.stats.StatTracker;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

public class NoAbilityTracker extends StatTracker<Evolution>
{
	/**
	 * @author Mysticate
	 */
	
	private List<String> _out = new ArrayList<String>();
	
	public NoAbilityTracker(Evolution game)
	{
		super(game);
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onEvolutionAbility(EvolutionAbilityUseEvent event)
	{
		if (event.isCancelled())
			return;
		
		if (!getGame().IsLive())
			return;
		
		if (!getGame().IsAlive(event.getPlayer()))
			return;
		
		if (_out.contains(event.getPlayer().getUniqueId().toString()))
			return;
		
		_out.add(event.getPlayer().getUniqueId().toString());
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onGameStateChange(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.End)
			return;
		
		List<Player> winners = getGame().getWinners();
		if (winners == null)
			return;

		if (winners.size() < 1)
			return;

		Player winner = winners.get(0);

		if (_out.contains(winner.getUniqueId().toString()))
			return;

		addStat(winner, "MeleeOnly", 1, true, false);
	}
}
