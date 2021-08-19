package nautilus.game.arcade.game.games.survivalgames.misison;

import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;

import mineplex.core.mission.MissionTrackerType;

import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.missions.GameMissionTracker;

public class BowHorseKillTracker extends GameMissionTracker<Game>
{

	public BowHorseKillTracker(Game game)
	{
		super(MissionTrackerType.SG_BOW_HORSE_KILL, game);
	}

	@EventHandler
	public void playerDeath(PlayerDeathEvent event)
	{
		Player player = event.getEntity();
		Player killer = player.getKiller();

		if (killer == null || killer.equals(player) || killer.getVehicle() == null || !(killer.getVehicle() instanceof Horse))
		{
			return;
		}

		_manager.incrementProgress(killer, 1, _trackerType, getGameType(), null);
	}
}
