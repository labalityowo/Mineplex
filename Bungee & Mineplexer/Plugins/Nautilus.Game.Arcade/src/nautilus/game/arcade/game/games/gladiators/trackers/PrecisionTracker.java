package nautilus.game.arcade.game.games.gladiators.trackers;

import java.util.List;
import java.util.UUID;
import mineplex.core.common.util.NautHashMap;
import mineplex.core.common.util.UtilPlayer;
import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.game.games.gladiators.Gladiators;
import nautilus.game.arcade.stats.StatTracker;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;

/**
 * Created by William (WilliamTiger).
 * 08/12/15
 */
public class PrecisionTracker extends StatTracker<Gladiators>
{
	private NautHashMap<Projectile, UUID> _current = new NautHashMap<Projectile, UUID>();
	private NautHashMap<UUID, Integer> _shot = new NautHashMap<UUID, Integer>();
	private NautHashMap<UUID, Integer> _hit = new NautHashMap<UUID, Integer>();

	public PrecisionTracker(Gladiators game)
	{
		super(game);
	}

	@EventHandler
	public void end(GameStateChangeEvent e)
	{
		if (e.GetState() != Game.GameState.End)
			return;

		List<Player> winners = getGame().getWinners();
		if (winners == null)
			return;

		for (Player p : winners)
		{
			if (getShot(p) == getHit(p) && getShot(p) >= 3)
			{
				addStat(p, "Precision", 1, true, false);
			}
		}
	}

	@EventHandler
	public void shoot(EntityShootBowEvent e)
	{
		if (!(e.getEntity() instanceof Player))
			return;

		Player p = (Player) e.getEntity();

		_shot.put(p.getUniqueId(), getShot(p) + 1);
		_current.put((Projectile) e.getProjectile(), p.getUniqueId());
	}

	@EventHandler
	public void hit(ProjectileHitEvent e)
	{
		if (!_current.containsKey(e.getEntity()))
			return;

		Player player = UtilPlayer.searchExact(_current.remove(e.getEntity()));
		if (player == null)
			return;

		_hit.put(player.getUniqueId(), getHit(player) + 1);
	}

	private int getShot(Player player)
	{
		return _shot.containsKey(player.getUniqueId()) ? _shot.get(player.getUniqueId()) : 0;
	}

	private int getHit(Player player)
	{
		return _hit.containsKey(player.getUniqueId()) ? _hit.get(player.getUniqueId()) : 0;
	}
}