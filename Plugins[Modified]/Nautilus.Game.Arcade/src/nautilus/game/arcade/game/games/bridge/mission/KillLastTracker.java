package nautilus.game.arcade.game.games.bridge.mission;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;

import mineplex.core.mission.MissionTrackerType;

import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.missions.GameMissionTracker;

public class KillLastTracker extends GameMissionTracker<Game>
{

	public KillLastTracker(Game game)
	{
		super(MissionTrackerType.BRIDGES_KILL_LAST, game);
	}

	@EventHandler(priority = EventPriority.LOW)
	public void playerDeath(PlayerDeathEvent event)
	{
		Player player = event.getEntity();
		Player killer = player.getKiller();

		if (player.equals(killer))
		{
			return;
		}

		GameTeam team = _game.GetTeam(player);

		if (team == null || team.GetPlayers(true).size() > 1)
		{
			return;
		}

		_manager.incrementProgress(killer, 1, _trackerType, getGameType(), null);
	}
}
