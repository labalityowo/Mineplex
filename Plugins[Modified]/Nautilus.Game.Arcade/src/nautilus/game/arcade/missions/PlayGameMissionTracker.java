package nautilus.game.arcade.missions;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import mineplex.core.game.GameDisplay;
import mineplex.core.mission.MissionTrackerType;

import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.game.Game.GameState;

public class PlayGameMissionTracker extends GameMissionTracker<Game>
{

	public PlayGameMissionTracker(Game game)
	{
		super(MissionTrackerType.GAME_PLAY, game);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void gameStart(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Live)
		{
			return;
		}

		// Run after 30 seconds so that players don't quit immediately
		_manager.runSyncLater(() ->
		{
			for (Player player : _game.GetPlayers(false))
			{
				if (!player.isOnline())
				{
					continue;
				}

				_manager.incrementProgress(player, 1, _trackerType, getGameType(), null);
			}
		}, 600);
	}
}
