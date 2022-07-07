package nautilus.game.arcade.stats;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerTeleportEvent;

import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.Game;

public class DistanceTraveledStatTracker extends StatTracker<Game>
{
	private final Map<Player, Location> _lastLocation = new HashMap<>();
	private final Map<Player, Double> _distanceTraveled = new HashMap<>();
	private final String _statName;

	public DistanceTraveledStatTracker(Game game, String statName)
	{
		super(game);

		_statName = statName;
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onGameStateChange(GameStateChangeEvent event)
	{
		if (event.GetState() == Game.GameState.Live)
		{
			for (Player player : getGame().GetPlayers(true))
				_lastLocation.put(player, player.getLocation());
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onUpdate(UpdateEvent event)
	{
		if (getGame().GetState() != Game.GameState.Live)
			return;

		if (event.getType() == UpdateType.SEC)
		{
			for (Player player : getGame().GetPlayers(true))
			{
				Location lastLocation = _lastLocation.put(player, player.getLocation());

				if (lastLocation != null && lastLocation.getWorld() == player.getLocation().getWorld())
				{
					Double distance = _distanceTraveled.get(player);
					if (distance == null)
						distance = 0.0;

					_distanceTraveled.put(player, distance + lastLocation.distance(player.getLocation()));
				}
			}
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onPlayerTeleport(PlayerTeleportEvent event)
	{
		_lastLocation.remove(event.getPlayer());
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onGameEnd(GameStateChangeEvent event)
	{
		if (event.GetState() == Game.GameState.End)
		{
			for (Map.Entry<Player, Double> entry : _distanceTraveled.entrySet())
				getGame().AddStat(entry.getKey(), getStatName(), (int) Math.round(entry.getValue()), false, false);
		}
	}

	public String getStatName()
	{
		return _statName;
	}
}
