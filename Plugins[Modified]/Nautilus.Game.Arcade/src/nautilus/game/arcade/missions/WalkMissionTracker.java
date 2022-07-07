package nautilus.game.arcade.missions;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;

import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.mission.MissionTrackerType;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.game.Game.GameState;

public class WalkMissionTracker extends GameMissionTracker<Game>
{

	private final Map<Player, Location> _last;

	public WalkMissionTracker(Game game)
	{
		super(null, game);

		_last = new HashMap<>();
	}

	@Override
	public void cleanup()
	{
		_last.clear();
	}

	@EventHandler
	public void live(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Live)
		{
			return;
		}

		_game.GetPlayers(true).forEach(player -> _last.put(player, player.getLocation()));
	}

	@EventHandler
	public void update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC || !_game.IsLive())
		{
			return;
		}

		_last.entrySet().forEach(entry ->
		{
			Player player = entry.getKey();

			if (UtilPlayer.isSpectator(player))
			{
				return;
			}

			Location last = entry.getValue(), now = player.getLocation();
			double distance = UtilMath.offset(now, last);
			ChatColor teamColour = _game.GetTeam(player).GetColor();

			if (distance < 0.5)
			{
				_manager.incrementProgress(player, 1, MissionTrackerType.GAME_STAND_STILL, getGameType(), teamColour);
			}
			else
			{
				_manager.incrementProgress(player, (int) distance, MissionTrackerType.GAME_WALK, getGameType(), teamColour);
			}

			entry.setValue(now);
		});
	}

	@EventHandler
	public void playerQuit(PlayerQuitEvent event)
	{
		_last.remove(event.getPlayer());
	}
}
