package nautilus.game.arcade.game.games.smash.mission;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.mission.MissionTrackerType;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.missions.GameMissionTracker;

public class AirborneTracker extends GameMissionTracker<Game>
{

	public AirborneTracker(Game game)
	{
		super(MissionTrackerType.SSM_AIRBORNE, game);
	}

	@EventHandler
	public void update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
		{
			return;
		}

		for (Player player : _game.GetPlayers(true))
		{
			if (UtilPlayer.isSpectator(player) || UtilEnt.onBlock(player))
			{
				continue;
			}

			_manager.incrementProgress(player, 1, _trackerType, getGameType(), null);
		}
	}

}
