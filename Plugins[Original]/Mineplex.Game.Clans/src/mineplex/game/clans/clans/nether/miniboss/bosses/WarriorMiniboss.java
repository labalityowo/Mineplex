package mineplex.game.clans.clans.nether.miniboss.bosses;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
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

import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilMath;
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.clans.amplifiers.AmplifierManager;
import mineplex.game.clans.clans.nether.miniboss.NetherMiniBoss;
import mineplex.game.clans.items.runes.RuneManager.RuneAttribute;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

/**
 * Class for running an individual Warrior miniboss
 */
public class WarriorMiniboss extends NetherMiniBoss<Zombie>
{
	private static final double RUNE_DROP_CHANCE = .02;
	private static final int MAX_VALUABLE_DROPS = 5;
	private static final Material[] VALUABLE_DROP_TYPES = new Material[] {Material.DIAMOND, Material.GOLD_INGOT, Material.IRON_INGOT, Material.LEATHER};
	private static final Material[] SET_DROP_TYPES = new Material[] {Material.DIAMOND_HELMET, Material.DIAMOND_CHESTPLATE, Material.DIAMOND_LEGGINGS, Material.DIAMOND_BOOTS, Material.GOLD_HELMET, Material.GOLD_CHESTPLATE, Material.GOLD_LEGGINGS, Material.GOLD_BOOTS, Material.IRON_HELMET, Material.IRON_CHESTPLATE, Material.IRON_LEGGINGS, Material.IRON_BOOTS, Material.CHAINMAIL_HELMET, Material.CHAINMAIL_CHESTPLATE, Material.CHAINMAIL_LEGGINGS, Material.CHAINMAIL_BOOTS, Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS};
	private static final double SET_DROP_CHANCE = .02;
	private static final double LEAP_CHANCE = .9;
	private static final double LEAP_MIN_DIST = 3;
	private static final double LEAP_MAX_DIST = 16;
	private static final float SOUND_VOLUME = 1f;
	private static final float SOUND_PITCH = 2f;
	
	public WarriorMiniboss(String displayName, Double maxHealth, Location spawn, EntityType type)
	{
		super(displayName, maxHealth, spawn, type);
	}
	
	@Override
	public void customSpawn()
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
	
	@Override
	public void update()
	{
		if (Math.random() < LEAP_CHANCE)
			return;

		Zombie zombie = getEntity();

		if (zombie.getTarget() == null)
			return;

		double dist = UtilMath.offset(zombie.getTarget(), zombie);

		if (dist <= LEAP_MIN_DIST || dist > LEAP_MAX_DIST)
			return;


		double power = 0.8 + (1.2 * ((dist-3)/13d)); //Im sorry connor but i have no idea what those numbers are for <3

		//Leap
		UtilAction.velocity(zombie, UtilAlg.getTrajectory(zombie, zombie.getTarget()),
				power, false, 0, 0.2, 1, true);

		//Effect
		zombie.getWorld().playSound(zombie.getLocation(), Sound.ZOMBIE_HURT, SOUND_VOLUME, SOUND_PITCH);
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