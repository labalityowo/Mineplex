package nautilus.game.arcade.stats;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;

import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.game.Game;

public class SharpShooterStatTracker extends StatTracker<Game>
{
	private final Map<UUID, Integer> _arrowsShot = new HashMap<>();
	private final Map<UUID, Integer> _arrowsHit = new HashMap<>();

	public SharpShooterStatTracker(Game game)
	{
		super(game);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onProjectileLaunch(ProjectileLaunchEvent event)
	{
		if (getGame().GetState() != Game.GameState.Live)
			return;

		if (event.getEntity().getShooter() instanceof Player && event.getEntity() instanceof Arrow)
		{
			Player player = (Player) event.getEntity().getShooter();

			Integer count = _arrowsShot.get(player.getUniqueId());
			count = (count == null ? 0 : count) + 1;
			_arrowsShot.put(player.getUniqueId(), count);
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onArrowHit(CustomDamageEvent event)
	{
		if (getGame().GetState() != Game.GameState.Live)
			return;

		if (event.GetProjectile() instanceof Arrow && event.GetDamageePlayer() != null)
		{
			if (event.GetProjectile().getShooter() instanceof Player && event.GetProjectile().getShooter() != event.GetDamageePlayer())
			{
				Player player = (Player) event.GetProjectile().getShooter();

				Integer count = _arrowsHit.get(player.getUniqueId());
				count = (count == null ? 0 : count) + 1;
				_arrowsHit.put(player.getUniqueId(), count);

				if (count == 8)
				{
					Integer arrowsShot = _arrowsShot.get(player.getUniqueId());

					if (arrowsShot != null && arrowsShot == 8)
						addStat(player, "Sharpshooter", 1, true, false);
				}
			}
		}
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event)
	{
		_arrowsShot.remove(event.getEntity().getUniqueId());
		_arrowsHit.remove(event.getEntity().getUniqueId());
	}
}
