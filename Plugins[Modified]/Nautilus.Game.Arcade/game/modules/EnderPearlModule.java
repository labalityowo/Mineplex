package nautilus.game.arcade.game.modules;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class EnderPearlModule extends Module
{

	private static final int DEFAULT_COOLDOWN = 500;

	private final Map<Projectile, Player> _pearls;

	private int _maxTicks = Integer.MAX_VALUE;
	private long _cooldown = DEFAULT_COOLDOWN;

	public EnderPearlModule()
	{
		_pearls = new HashMap<>();
	}

	public EnderPearlModule setMaxTicks(int maxTicks)
	{
		_maxTicks = maxTicks;
		return this;
	}

	public EnderPearlModule setCooldown(long cooldown)
	{
		_cooldown = cooldown;
		return this;
	}

	@Override
	public void cleanup()
	{
		_pearls.clear();
	}

	@EventHandler
	public void interactEnderPearl(PlayerInteractEvent event)
	{
		if (!UtilEvent.isAction(event, ActionType.R) || !getGame().IsLive())
		{
			return;
		}

		Player player = event.getPlayer();
		ItemStack itemStack = player.getItemInHand();
		boolean inform = _cooldown > DEFAULT_COOLDOWN;

		if (itemStack != null && itemStack.getType() == Material.ENDER_PEARL && !Recharge.Instance.use(player, "Ender Pearl", _cooldown, inform, inform))
		{
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void projectileLaunch(ProjectileLaunchEvent event)
	{
		Projectile entity = event.getEntity();

		if (!getGame().IsLive() || !(entity instanceof EnderPearl) || !(entity.getShooter() instanceof Player))
		{
			return;
		}

		Player shooter = (Player) event.getEntity().getShooter();

		_pearls.put(entity, shooter);
		entity.setShooter(null);
	}

	@EventHandler
	public void update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		_pearls.entrySet().removeIf(entry ->
		{
			Projectile entity = entry.getKey();
			Player shooter = entry.getValue();

			if (UtilPlayer.isSpectator(shooter))
			{
				entity.remove();
				return true;
			}
			else if (entity.getTicksLived() > _maxTicks)
			{
				teleport(shooter, entity);
				entity.remove();
				return true;
			}

			UtilParticle.PlayParticleToAll(ParticleType.WITCH_MAGIC, entity.getLocation(), 0, 0, 0, 0.1F, 3, ViewDist.LONG);
			return false;
		});
	}

	@EventHandler
	public void projectileHit(ProjectileHitEvent event)
	{
		if (!(event.getEntity() instanceof EnderPearl))
		{
			return;
		}

		Projectile projectile = event.getEntity();
		Player shooter = _pearls.remove(projectile);

		if (shooter != null && getGame().isInsideMap(projectile.getLocation()))
		{
			teleport(shooter, projectile);
		}
	}

	private void teleport(Player shooter, Projectile entity)
	{
		Location toTeleport = entity.getLocation();
		Location playerLocation = shooter.getLocation();
		toTeleport.setYaw(playerLocation.getYaw());
		toTeleport.setPitch(playerLocation.getPitch());

		shooter.teleport(toTeleport);
		shooter.setFallDistance(0);
	}
}
