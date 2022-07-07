package mineplex.game.clans.clans.worldevent.undead.creature;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import mineplex.core.common.util.UtilMath;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.clans.clans.worldevent.api.EventCreature;
import mineplex.game.clans.clans.worldevent.api.WorldEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

public class UndeadArcher extends EventCreature<Skeleton>
{
	public static final int BARBED_LEVEL = 1;
	
	private Set<Projectile> _arrows = new HashSet<>();
	
	public UndeadArcher(WorldEvent event, Location spawnLocation)
	{
		super(event, spawnLocation, "Undead Archer", true, 30, 30, true, Skeleton.class);
		
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
		
		for (int i = 0; i < UtilMath.r(5) + 1; i++)
		{
			getEntity().getWorld().dropItem(getEntity().getLocation(), new org.bukkit.inventory.ItemStack(Material.EMERALD));
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void bowShoot(EntityShootBowEvent event)
	{
		if (BARBED_LEVEL == 0)
		{
			return;
		}
		
		if (!(event.getProjectile() instanceof Projectile))
		{
			return;
		}
		
		_arrows.add((Projectile) event.getProjectile());
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
		Player damager = event.GetDamagerPlayer(true);
		
		if (projectile == null)
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
		event.AddMod(damager.getName(), "Barbed Arrows", 0, false);
		
		// Condition
		getEvent().getCondition().Factory().Slow("Barbed Arrows", damagee, damager, (projectile.getVelocity().length() / 3) * (2 + BARBED_LEVEL), 0, false, true, true, true);
	}
	
	@EventHandler
	public void clean(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC) return;
		
		for (Iterator<Projectile> arrowIterator = _arrows.iterator(); arrowIterator.hasNext();)
		{
			Projectile arrow = arrowIterator.next();
			
			if (arrow.isDead() || !arrow.isValid()) arrowIterator.remove();
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
}