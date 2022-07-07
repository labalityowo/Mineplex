package nautilus.game.arcade.game.games.smash.perks.skeleton;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.util.Vector;

import mineplex.core.common.util.UtilPlayer;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import nautilus.game.arcade.game.games.smash.perks.SmashUltimate;

public class SmashSkeleton extends SmashUltimate
{

	private Set<Projectile> _arrows = new HashSet<>();

	public SmashSkeleton()
	{
		super("Arrow Storm", new String[] {}, Sound.SKELETON_HURT, 0);
	}

	@EventHandler
	public void fireArrows(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}
		
		for (UUID uuid : getLastUltimate().keySet())
		{
			Player cur = UtilPlayer.searchExact(uuid);
			
			if (cur == null)
			{
				continue;
			}
			
			Vector random = new Vector((Math.random() - 0.5) / 5, (Math.random() - 0.5) / 5, (Math.random() - 0.5) / 5);
			Projectile arrow = cur.launchProjectile(Arrow.class);
			arrow.setVelocity(cur.getLocation().getDirection().add(random).multiply(3));
			_arrows.add(arrow);
			cur.getWorld().playSound(cur.getLocation(), Sound.SHOOT_ARROW, 1f, 1f);
		}
	}

	@EventHandler
	public void projectileHit(ProjectileHitEvent event)
	{
		if (_arrows.remove(event.getEntity()))
		{
			event.getEntity().remove();
		}
	}

	@EventHandler
	public void clean(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
		{
			return;
		}

		_arrows.removeIf(arrow -> arrow.isDead() || !arrow.isValid());
	}
}
