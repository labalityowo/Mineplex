package nautilus.game.arcade.kit.perks;

import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ProjectileHitEvent;

import mineplex.core.common.util.UtilPlayer;
import nautilus.game.arcade.kit.Perk;

public class PerkFireArrows extends Perk
{

	private double _distance;
	private int _fireTicks;
	
	public PerkFireArrows(double distance, int fireTicks)
	{
		super("Fire Arrows", new String[] {});

		_distance = distance;
		_fireTicks = fireTicks;
	}
	
	@EventHandler
	public void onProjectileHit(ProjectileHitEvent event)
	{
		if (event.getEntity().getShooter() instanceof Player)
		{
			Player player = (Player) event.getEntity().getShooter();
			Projectile projectile = event.getEntity();
			
			if (!hasPerk(player))
			{
				return;
			}
			
			for (Player other : UtilPlayer.getNearby(projectile.getLocation(), _distance))
			{
				if (!Manager.IsAlive(other) || player.equals(other) || Manager.GetGame().GetTeam(player).equals(Manager.GetGame().GetTeam(other)))
				{
					continue;
				}
				
				other.setFireTicks(_fireTicks);
			}
		}
	}
}
