package nautilus.game.arcade.game.games.moba.kit.common;

import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilServer;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.ArcadeManager;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class LeashedEntity implements Listener
{

	private final LivingEntity _host;
	private final Zombie _fakeLeash;

	public LeashedEntity(ArcadeManager manager, LivingEntity host, LivingEntity leasher)
	{
		manager.GetGame().CreatureAllowOverride = true;

		_host = host;
		_fakeLeash = host.getWorld().spawn(host.getLocation(), Zombie.class);
		UtilEnt.vegetate(_fakeLeash);
		UtilEnt.silence(_fakeLeash, true);
		_fakeLeash.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, false, false));
		_fakeLeash.setLeashHolder(leasher);

		manager.GetGame().CreatureAllowOverride = false;

		UtilServer.RegisterEvents(this);
	}

	public void remove()
	{
		_fakeLeash.setLeashHolder(null);
		_fakeLeash.remove();
		UtilServer.Unregister(this);
	}

	@EventHandler
	public void update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		Location location = _host.getLocation();
		((CraftLivingEntity) _fakeLeash).getHandle().setPosition(location.getX(), location.getY(), location.getZ());
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void fakeLeashDamage(CustomDamageEvent event)
	{
		if (event.GetDamageeEntity().equals(_fakeLeash) && event.GetCause() == DamageCause.ENTITY_ATTACK)
		{
			if (event.GetDamagerEntity(true).equals(_host))
			{
				event.SetCancelled("Self Leash Damage");
			}

			event.setDamagee(_host);
			event.SetIgnoreRate(false);
		}
	}

	@EventHandler
	public void fakeLeashFire(EntityCombustEvent event)
	{
		if (event.getEntity().equals(_fakeLeash))
		{
			event.setCancelled(true);
		}
	}

	public LivingEntity getHost()
	{
		return _host;
	}
}
