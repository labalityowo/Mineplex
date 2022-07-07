package nautilus.game.arcade.game.games.evolution.trackers;

import java.util.ArrayList;
import java.util.List;

import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.Game.GameState;
import nautilus.game.arcade.game.games.evolution.Evolution;
import nautilus.game.arcade.stats.StatTracker;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

public class NoMeleeTracker extends StatTracker<Evolution>
{
	/**
	 * @author Mysticate
	 */
	
	private List<String> _out = new ArrayList<String>();
	
	public NoMeleeTracker(Evolution game)
	{
		super(game);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onDamage(CustomDamageEvent event)
	{
		if (!getGame().IsLive())
			return;
		
		Player player = event.GetDamagerPlayer(true);
		if (player == null)
			return;
		
		if (!getGame().IsAlive(player))
			return;
		
		if (event.GetReason() != null && new String(event.GetReason()).toLowerCase().contains("attack"))
		{
			if (_out.contains(player.getUniqueId().toString()))
				return;
			
			_out.add(player.getUniqueId().toString());
		}
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

		addStat(winner, "AbilityOnly", 1, true, false);
	}
}
