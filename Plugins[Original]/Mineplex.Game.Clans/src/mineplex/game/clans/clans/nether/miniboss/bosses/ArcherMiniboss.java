package mineplex.game.clans.clans.nether.miniboss.bosses;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
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

import mineplex.core.common.util.UtilMath;
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.clans.amplifiers.AmplifierManager;
import mineplex.game.clans.clans.nether.miniboss.NetherMiniBoss;
import mineplex.game.clans.items.runes.RuneManager.RuneAttribute;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

/**
 * Class for running an individual Archer miniboss
 */
public class ArcherMiniboss extends NetherMiniBoss<Skeleton>
{
	private static final int BARBED_LEVEL = 1;
	private static final double RUNE_DROP_CHANCE = .02;
	private static final int MAX_VALUABLE_DROPS = 5;
	private static final Material[] VALUABLE_DROP_TYPES = new Material[] {Material.DIAMOND, Material.GOLD_INGOT, Material.IRON_INGOT, Material.LEATHER};
	private static final Material[] SET_DROP_TYPES = new Material[] {Material.DIAMOND_HELMET, Material.DIAMOND_CHESTPLATE, Material.DIAMOND_LEGGINGS, Material.DIAMOND_BOOTS, Material.GOLD_HELMET, Material.GOLD_CHESTPLATE, Material.GOLD_LEGGINGS, Material.GOLD_BOOTS, Material.IRON_HELMET, Material.IRON_CHESTPLATE, Material.IRON_LEGGINGS, Material.IRON_BOOTS, Material.CHAINMAIL_HELMET, Material.CHAINMAIL_CHESTPLATE, Material.CHAINMAIL_LEGGINGS, Material.CHAINMAIL_BOOTS, Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS};
	private static final double SET_DROP_CHANCE = .02;
	
	public ArcherMiniboss(String displayName, Double maxHealth, Location spawn, EntityType type)
	{
		super(displayName, maxHealth, spawn, type);
	}
	
	@Override
	public void customSpawn()
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
	public void customDeath(Location deathLocation)
	{
		deathLocation.getWorld().dropItemNaturally(deathLocation, new ItemStack(VALUABLE_DROP_TYPES[UtilMath.r(VALUABLE_DROP_TYPES.length)], UtilMath.r(MAX_VALUABLE_DROPS) + 1));
		double runeDropChance = RUNE_DROP_CHANCE;
		if (ClansManager.getInstance().getAmplifierManager().hasActiveAmplifier())
		{
			runeDropChance *= AmplifierManager.AMPLIFIER_RUNE_DROP_MULTIPLIER;
		}
		if (new Random().nextDouble() <= runeDropChance)
		{
			RuneAttribute runeType = RuneAttribute.values()[UtilMath.r(RuneAttribute.values().length)];
			deathLocation.getWorld().dropItemNaturally(deathLocation, ClansManager.getInstance().getGearManager().getRuneManager().getRune(runeType));
		}
		if (new Random().nextDouble() <= SET_DROP_CHANCE)
		{
			deathLocation.getWorld().dropItemNaturally(deathLocation, new ItemStack(SET_DROP_TYPES[UtilMath.r(SET_DROP_TYPES.length)], 1));
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
		
		event.getProjectile().setMetadata("BARBED_ARROW", new FixedMetadataValue(ClansManager.getInstance().getPlugin(), 2));
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
		ClansManager.getInstance().getCondition().Factory().Slow("Barbed Arrows", damagee, damager, (projectile.getVelocity().length() / 3) * (2 + BARBED_LEVEL), 0, false, true, true, true);
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