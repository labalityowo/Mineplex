package nautilus.game.arcade.game.games.micro.mission;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import mineplex.core.mission.MissionTrackerType;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.missions.GameMissionTracker;

public class Last2Tracker extends GameMissionTracker<Game>
{

	private boolean _awarded;

	public Last2Tracker(Game game)
	{
		super(MissionTrackerType.MICRO_LAST_TWO, game);
	}

	@EventHandler
	public void update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC || _awarded)
		{
			return;
		}

		long aliveTeams = _game.GetTeamList().stream()
				.filter(GameTeam::IsTeamAlive)
				.count();

		if (aliveTeams == 2)
		{
			_awarded = true;

			for (Player player : _game.GetPlayers(true))
			{
				_manager.incrementProgress(player, 1,  _trackerType, getGameType(), null);
			}
		}
	}
}
