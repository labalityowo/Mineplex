package nautilus.game.arcade.missions;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import mineplex.core.mission.MissionTrackerType;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

import nautilus.game.arcade.game.Game;

public class DamageMissionTracker extends GameMissionTracker<Game>
{

	public DamageMissionTracker(Game game)
	{
		super(null, game);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void damage(CustomDamageEvent event)
	{
		Player damager = event.GetDamagerPlayer(true);

		if (event.IsCancelled() || damager == null)
		{
			return;
		}

		_manager.incrementProgress(damager, 1, MissionTrackerType.GAME_DAMAGE_CAUSE, getGameType(), event.GetCause());

		String reason = event.GetReason();

		if (reason != null)
		{
			_manager.incrementProgress(damager, 1, MissionTrackerType.GAME_DAMAGE_REASON, getGameType(), event.GetReason());
		}
	}
}
