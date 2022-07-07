package mineplex.game.clans.clans.worldevent.boss.skeletonking.minion;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.clans.clans.worldevent.api.EventCreature;
import mineplex.game.clans.clans.worldevent.api.WorldEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

public class WraithCreature extends EventCreature<Zombie>
{
	private static final int LIFETIME = -1;
	
	public WraithCreature(WorldEvent event, Location spawnLocation)
	{
		super(event, spawnLocation, "Wraith", true, 200, 30, true, Zombie.class);
		spawnEntity();
	}

	@Override
	protected void spawnCustom()
	{
		Zombie entity = getEntity();
		EntityEquipment eq = entity.getEquipment();
		eq.setHelmet(new ItemStack(Material.SKULL_ITEM, 1, (short)1));
		eq.setChestplate(new ItemBuilder(Material.LEATHER_CHESTPLATE).setColor(Color.BLACK).build());
		eq.setLeggings(new ItemBuilder(Material.LEATHER_LEGGINGS).setColor(Color.BLACK).build());
		eq.setBoots(new ItemBuilder(Material.LEATHER_BOOTS).setColor(Color.BLACK).build());
		eq.setItemInHand(new ItemStack(Material.IRON_SWORD));
		eq.setHelmetDropChance(0.f);
		eq.setChestplateDropChance(0.f);
		eq.setLeggingsDropChance(0.f);
		eq.setBootsDropChance(0.f);
		eq.setItemInHandDropChance(0.f);
		entity.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 999999, 0));
		entity.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 999999, 0));
		entity.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, 999999, 0));
		entity.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 999999, 1));
	}

	@Override
	public void dieCustom()
	{
		if (LIFETIME >= 0 && getEntity().getTicksLived() >= (20 * LIFETIME))
		{
			return;
		}
		
		if (Math.random() > 0.97)
			getEntity().getWorld().dropItem(getEntity().getLocation(), new ItemStack(Material.DIAMOND_HELMET));

		if (Math.random() > 0.97)
			getEntity().getWorld().dropItem(getEntity().getLocation(), new ItemStack(Material.DIAMOND_CHESTPLATE));

		if (Math.random() > 0.97)
			getEntity().getWorld().dropItem(getEntity().getLocation(), new ItemStack(Material.DIAMOND_LEGGINGS));

		if (Math.random() > 0.97)
			getEntity().getWorld().dropItem(getEntity().getLocation(), new ItemStack(Material.DIAMOND_BOOTS));
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void damage(CustomDamageEvent event)
	{
		if (event.IsCancelled())
		{
			return;
		}
		
		LivingEntity damagee = event.GetDamageeEntity();
		LivingEntity damager = event.GetDamagerEntity(false);
		
		if (damagee == null)
		{
			return;
		}
		
		if (damager == null)
		{
			return;
		}
		
		if (getEntity().equals(damager))
		{
			event.AddMult(damager.getName(), "Mystical Darkness", 2, false);
		}
	}

	@EventHandler
	public void blink(UpdateEvent event)
	{
		if (getEntity() == null)
			return;

		if (event.getType() != UpdateType.FAST)
			return;
		
		if (LIFETIME >= 0 && getEntity().getTicksLived() >= (20 * LIFETIME))
		{
			remove();
			return;
		}

		if (Math.random() < 0.6)
			return;

		Zombie zombie = getEntity();

		if (zombie.getTarget() == null)
			return;

		double dist = UtilMath.offset(zombie.getTarget(), zombie);

		if (dist <= 10 || dist > 25)
			return;
		
		Location teleport = zombie.getTarget().getLocation().add(Math.random() + 1, 0, Math.random() + 1);
		if (UtilMath.offset(getSpawnLocation(), teleport) > 30)
		{
			return;
		}
		UtilParticle.PlayParticleToAll(ParticleType.SMOKE, zombie.getLocation(), 0, 0, 0, 0, 5, ViewDist.MAX);
		zombie.getWorld().playSound(zombie.getLocation(), Sound.ENDERMAN_TELEPORT, 1f, 2f);
		zombie.teleport(teleport);
		UtilParticle.PlayParticleToAll(ParticleType.SMOKE, zombie.getLocation(), 0, 0, 0, 0, 5, ViewDist.MAX);
		zombie.getWorld().playSound(zombie.getLocation(), Sound.ENDERMAN_TELEPORT, 1f, 2f);
	}
	
	@EventHandler
	public void onTarget(EntityTargetLivingEntityEvent event)
	{
		if (getEntity().equals(event.getEntity()))
		{
			if (!(event.getTarget() instanceof Player))
			{
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void protect(CustomDamageEvent event)
	{
		if (event.IsCancelled())
		{
			return;
		}
		
		LivingEntity damagee = event.GetDamageeEntity();
		LivingEntity damager = event.GetDamagerEntity(event.GetCause() == DamageCause.PROJECTILE);
		
		if (damagee == null)
		{
			return;
		}
		
		if (damager == null)
		{
			return;
		}
		
		if (getEntity().equals(damagee))
		{
			if (!(damager instanceof Player))
			{
				event.SetCancelled("Allied Attacker");
			}
		}
	}
}