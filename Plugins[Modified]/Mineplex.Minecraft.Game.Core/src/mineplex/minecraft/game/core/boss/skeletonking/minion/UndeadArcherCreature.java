package mineplex.minecraft.game.core.boss.skeletonking.minion;

import mineplex.core.common.util.UtilMath;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.boss.EventCreature;
import mineplex.minecraft.game.core.boss.WorldEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class UndeadArcherCreature extends EventCreature<Skeleton>
{
	private static final int BARBED_LEVEL = 1;
	private static final int LIFETIME = -1;
	
	public UndeadArcherCreature(WorldEvent event, Location spawnLocation)
	{
		super(event, spawnLocation, "Undead Archer", true, 25, Skeleton.class);
		
		spawnEntity();
	}
	
	@Override
	protected void spawnCustom()
	{
		Skeleton entity = getEntity();
		EntityEquipment eq = entity.getEquipment();
		eq.setItemInHand(new ItemStack(Material.BOW));
		eq.setHelmet(new ItemStack(Material.CHAINMAIL_HELMET));
		eq.setChestplate(new ItemStack(Material.CHAINMAIL_CHESTPLATE));
		eq.setLeggings(new ItemStack(Material.CHAINMAIL_LEGGINGS));
		eq.setBoots(new ItemStack(Material.CHAINMAIL_BOOTS));
		eq.setItemInHandDropChance(0.f);
		eq.setHelmetDropChance(0.f);
		eq.setChestplateDropChance(0.f);
		eq.setLeggingsDropChance(0.f);
		eq.setBootsDropChance(0.f);
		entity.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 999999, 0));
	}
	
	@Override
	public void dieCustom()
	{
		if (LIFETIME >= 0 && getEntity().getTicksLived() >= (20 * LIFETIME))
		{
			return;
		}
		
		if (Math.random() > 0.97)
		{
			getEntity().getWorld().dropItem(getEntity().getLocation(), new org.bukkit.inventory.ItemStack(Material.CHAINMAIL_HELMET));
		}
		
		if (Math.random() > 0.97)
		{
			getEntity().getWorld().dropItem(getEntity().getLocation(), new org.bukkit.inventory.ItemStack(Material.CHAINMAIL_CHESTPLATE));
		}
		
		if (Math.random() > 0.97)
		{
			getEntity().getWorld().dropItem(getEntity().getLocation(), new org.bukkit.inventory.ItemStack(Material.CHAINMAIL_LEGGINGS));
		}
		
		if (Math.random() > 0.97)
		{
			getEntity().getWorld().dropItem(getEntity().getLocation(), new org.bukkit.inventory.ItemStack(Material.CHAINMAIL_BOOTS));
		}
		
		if (Math.random() > 0.90)
		{
			getEntity().getWorld().dropItem(getEntity().getLocation(), new org.bukkit.inventory.ItemStack(Material.BOW));
		}
		
		getEntity().getWorld().dropItem(getEntity().getLocation(), new org.bukkit.inventory.ItemStack(Material.ARROW, UtilMath.r(12) + 1));
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void bowShoot(EntityShootBowEvent event)
	{
		if (BARBED_LEVEL == 0)
		{
			return;
		}
		
		if (!(event.getProjectile() instanceof Arrow))
		{
			return;
		}
		
		if (event.getEntity().getEntityId() != getEntity().getEntityId())
		{
			return;
		}
		
		event.getProjectile().setMetadata("BARBED_ARROW", new FixedMetadataValue(getEvent().getPlugin(), 2));
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void damage(CustomDamageEvent event)
	{
		if (event.IsCancelled())
		{
			return;
		}
		
		if (event.GetCause() != DamageCause.PROJECTILE)
		{
			return;
		}
		
		Projectile projectile = event.GetProjectile();
		LivingEntity damagee = event.GetDamageeEntity();
		LivingEntity damager = event.GetDamagerEntity(true);
		
		if (projectile == null)
		{
			return;
		}
		
		if (!projectile.hasMetadata("BARBED_ARROW"))
		{
			return;
		}
		
		if (damagee == null)
		{
			return;
		}
		
		if (damager == null)
		{
			return;
		}
		
		if (!getEntity().equals(damager))
		{
			return;
		}
		
		// Level
		if (BARBED_LEVEL == 0)
		{
			return;
		}
		
		Player damageePlayer = event.GetDamageePlayer();
		
		if (damageePlayer != null)
		{
			damageePlayer.setSprinting(false);
		}
		
		// Damage
		event.AddMod(damager.getName(), "Barbed Arrows", projectile.getMetadata("BARBED_ARROW").get(0).asDouble(), false);
		
		// Condition
		getEvent().getCondition().Factory().Slow("Barbed Arrows", damagee, damager, (projectile.getVelocity().length() / 3) * (2 + BARBED_LEVEL), 0, false, true, true, true);
	}
	
	@EventHandler
	public void clean(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
		{
			return;
		}
		
		if (LIFETIME >= 0 && getEntity().getTicksLived() >= (20 * LIFETIME))
		{
			remove();
			return;
		}
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