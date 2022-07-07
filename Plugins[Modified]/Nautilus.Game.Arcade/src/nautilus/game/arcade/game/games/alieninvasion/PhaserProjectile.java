package nautilus.game.arcade.game.games.alieninvasion;

import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilServer;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.ArcadeManager;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.util.Vector;

public class PhaserProjectile implements Listener
{

	private static final int SHARDS_PER_HIT = 5;

	private ArcadeManager _manager;
	private Projectile _projectile;

	public PhaserProjectile(ArcadeManager manager, LivingEntity player)
	{
		this(manager, player, player.getLocation().getDirection());
	}

	public PhaserProjectile(ArcadeManager manager, LivingEntity player, Vector direction)
	{
		_manager = manager;

		_projectile = player.launchProjectile(Snowball.class);
		_projectile.setVelocity(direction.multiply(2));

		UtilServer.RegisterEvents(this);
	}

	@EventHandler
	public void damage(CustomDamageEvent event)
	{
		if (_projectile == null || event.GetProjectile() == null || !event.GetProjectile().equals(_projectile) || event.GetReason() != null && event.GetReason().contains("Blaster"))
		{
			return;
		}

		Projectile projectile = event.GetProjectile();
		LivingEntity damagee = event.GetDamageeEntity();
		Player damager = event.GetDamagerPlayer(true);

		if (event.GetDamagerEntity(true) instanceof Player)
		{
			if (damagee instanceof Player)
			{
				projectile.remove();
				return;
			}

			_manager.GetGame().AddGems(damager, SHARDS_PER_HIT, "Aliens Hit", false, true);
			damager.playSound(damager.getLocation(), Sound.CHICKEN_EGG_POP, 1, 0.7F);
		}

		event.SetCancelled("Blaster Snowball");

		UtilParticle.PlayParticle(ParticleType.CLOUD, projectile.getLocation(), 0.5F, 0.5F, 0.5F, 0.05F, 5, ViewDist.NORMAL);
		_manager.GetDamage().NewDamageEvent(damagee, damager, projectile, DamageCause.CUSTOM, 3, false, true, true, UtilEnt.getName(damager), "Blaster");
		UtilServer.Unregister(this);
	}

	@EventHandler
	public void cleanup(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
		{
			return;
		}

		if (_projectile == null || _projectile.isDead() || !_projectile.isValid())
		{
			UtilServer.Unregister(this);
		}
	}
}
