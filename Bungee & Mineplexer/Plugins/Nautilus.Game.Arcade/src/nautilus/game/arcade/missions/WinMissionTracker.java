package nautilus.game.arcade.missions;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import mineplex.core.mission.MissionTrackerType;

import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.game.Game.GameState;

public class WinMissionTracker extends GameMissionTracker<Game>
{

	public WinMissionTracker(Game game)
	{
		super(MissionTrackerType.GAME_WIN, game);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void gameEnd(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.End)
		{
			return;
		}

		List<Player> winners = event.GetGame().getWinners();

		if (winners == null)
		{
			return;
		}

		winners.forEach(player -> _manager.incrementProgress(player, 1, _trackerType, getGameType(), null));
	}
}
