package mineplex.minecraft.game.core.boss.skeletonking.minion;

import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilMath;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.boss.EventCreature;
import mineplex.minecraft.game.core.boss.WorldEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

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

public class UndeadWarriorCreature extends EventCreature<Zombie>
{
	private static final int LIFETIME = -1;
	
	public UndeadWarriorCreature(WorldEvent event, Location spawnLocation)
	{
		super(event, spawnLocation, "Undead Warrior", true, 30, Zombie.class);
		spawnEntity();
	}

	@Override
	protected void spawnCustom()
	{
		Zombie entity = getEntity();
		EntityEquipment eq = entity.getEquipment();
		eq.setHelmet(new ItemStack(Material.IRON_HELMET));
		eq.setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
		eq.setLeggings(new ItemStack(Material.IRON_LEGGINGS));
		eq.setBoots(new ItemStack(Material.IRON_BOOTS));
		eq.setItemInHand(new ItemStack(Material.STONE_SWORD));
		eq.setHelmetDropChance(0.f);
		eq.setChestplateDropChance(0.f);
		eq.setLeggingsDropChance(0.f);
		eq.setBootsDropChance(0.f);
		eq.setItemInHandDropChance(0.f);
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
			getEntity().getWorld().dropItem(getEntity().getLocation(), new ItemStack(Material.IRON_HELMET));

		if (Math.random() > 0.97)
			getEntity().getWorld().dropItem(getEntity().getLocation(), new ItemStack(Material.IRON_CHESTPLATE));

		if (Math.random() > 0.97)
			getEntity().getWorld().dropItem(getEntity().getLocation(), new ItemStack(Material.IRON_LEGGINGS));

		if (Math.random() > 0.97)
			getEntity().getWorld().dropItem(getEntity().getLocation(), new ItemStack(Material.IRON_BOOTS));
	}

	@EventHandler
	public void leap(UpdateEvent event)
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

		if (Math.random() < 0.9)
			return;

		Zombie zombie = getEntity();

		if (zombie.getTarget() == null)
			return;

		double dist = UtilMath.offset(zombie.getTarget(), zombie);

		if (dist <= 3 || dist > 16)
			return;


		double power = 0.8 + (1.2 * ((dist-3)/13d));

		//Leap
		UtilAction.velocity(zombie, UtilAlg.getTrajectory(zombie, zombie.getTarget()),
				power, false, 0, 0.2, 1, true);

		//Effect
		zombie.getWorld().playSound(zombie.getLocation(), Sound.ZOMBIE_HURT, 1f, 2f);
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
	public void attack(CustomDamageEvent event)
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
			if (damagee instanceof Player)
			{
				((Player)damagee).setFoodLevel(((Player)damagee).getFoodLevel() - 1);
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