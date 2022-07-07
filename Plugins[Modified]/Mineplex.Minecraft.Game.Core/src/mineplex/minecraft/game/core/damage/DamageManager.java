package mineplex.minecraft.game.core.damage;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import mineplex.core.MiniPlugin;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilGear;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.disguise.DisguiseManager;
import mineplex.core.npc.NpcManager;
import mineplex.minecraft.game.core.combat.CombatManager;
import mineplex.minecraft.game.core.condition.ConditionManager;
import mineplex.minecraft.game.core.damage.compatibility.NpcProtectListener;
import net.minecraft.server.v1_8_R3.DamageSource;
import net.minecraft.server.v1_8_R3.EntityHuman;
import net.minecraft.server.v1_8_R3.EntityLiving;

import org.bukkit.EntityEffect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Fish;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public class DamageManager extends MiniPlugin
{
	private CombatManager _combatManager;
	private DisguiseManager _disguiseManager;
	private ConditionManager _conditionManager;
	protected Field _lastDamageByPlayerTime;
	protected Method _k;

	public boolean UseSimpleWeaponDamage = false;
	public boolean DisableDamageChanges = false;
	/**
	 * The value of knockback that is applied when a player is attacked.
	 * If 0, the value is calculated from log10(damage).
	 */
	private double _constantKnockback;

	private boolean _enabled = true;
	
	private final HashMap<String, Integer> _protectionTypeModifiers = new HashMap<>();
	private final HashMap<String, DamageCause[]> _protectionCauses = new HashMap<>();

	public DamageManager(JavaPlugin plugin, CombatManager combatManager, NpcManager npcManager, DisguiseManager disguiseManager, ConditionManager conditionManager) 
	{
		super("Damage Manager", plugin);

		_combatManager = combatManager;
		_disguiseManager = disguiseManager;
		_conditionManager = conditionManager;

		try
		{
			_lastDamageByPlayerTime = EntityLiving.class.getDeclaredField("lastDamageByPlayerTime");
			_lastDamageByPlayerTime.setAccessible(true);
			_k = EntityLiving.class.getDeclaredMethod("damageArmor", float.class);
			_k.setAccessible(true);
		} 
		catch (final Exception e)
		{
			System.out.println("Problem getting access to EntityLiving: " + e.getMessage());
		}

		if (npcManager != null)
		{
			registerEvents(new NpcProtectListener(npcManager));
		}

		_protectionTypeModifiers.put(Enchantment.PROTECTION_ENVIRONMENTAL.getName(), 1);
		_protectionTypeModifiers.put(Enchantment.PROTECTION_FIRE.getName(), 2);
		_protectionTypeModifiers.put(Enchantment.PROTECTION_EXPLOSIONS.getName(), 2);
		_protectionTypeModifiers.put(Enchantment.PROTECTION_PROJECTILE.getName(), 2);
		_protectionTypeModifiers.put(Enchantment.PROTECTION_FALL.getName(), 3);
		
		_protectionCauses.put(Enchantment.PROTECTION_ENVIRONMENTAL.getName(), new DamageCause[] {DamageCause.BLOCK_EXPLOSION, DamageCause.CONTACT, DamageCause.CUSTOM, DamageCause.DROWNING, DamageCause.ENTITY_ATTACK, DamageCause.ENTITY_EXPLOSION, DamageCause.FALL, DamageCause.FALLING_BLOCK, DamageCause.FIRE, DamageCause.FIRE_TICK, DamageCause.LAVA, DamageCause.LIGHTNING, DamageCause.PROJECTILE, DamageCause.SUFFOCATION, DamageCause.THORNS});
		_protectionCauses.put(Enchantment.PROTECTION_FIRE.getName(), new DamageCause[] {DamageCause.FIRE, DamageCause.FIRE_TICK, DamageCause.LAVA});
		_protectionCauses.put(Enchantment.PROTECTION_EXPLOSIONS.getName(), new DamageCause[] {DamageCause.BLOCK_EXPLOSION, DamageCause.ENTITY_EXPLOSION});
		_protectionCauses.put(Enchantment.PROTECTION_PROJECTILE.getName(), new DamageCause[] {DamageCause.PROJECTILE});
		_protectionCauses.put(Enchantment.PROTECTION_FALL.getName(), new DamageCause[] {DamageCause.FALL});
	}
	
	private int getHighestLevel(Enchantment ench, ItemStack[] items)
	{
		int level = 0;
		
		for (ItemStack item : items)
		{
			if (item == null && item.getType() == Material.AIR)
			{
				continue;
			}
			if (!item.containsEnchantment(ench))
			{
				continue;
			}
			if (item.getEnchantmentLevel(ench) <= level)
			{
				continue;
			}
			level = item.getEnchantmentLevel(ench);
		}
		
		return level;
	}
	
	private int getTotalEPF(Enchantment ench, ItemStack[] items)
	{
		if (!_protectionTypeModifiers.containsKey(ench.getName()))
		{
			return 0;
		}
		if (!_protectionCauses.containsKey(ench.getName()))
		{
			return 0;
		}
		
		int epf = 0;
		
		for (ItemStack item : items)
		{
			if (item == null || item.getType() == Material.AIR)
			{
				continue;
			}
			if (!item.containsEnchantment(ench))
			{
				continue;
			}
			if (item.getEnchantmentLevel(ench) <= 0)
			{
				continue;
			}
			
			epf += (item.getEnchantmentLevel(ench) * _protectionTypeModifiers.get(ench.getName()));
		}
		
		return Math.min(20, epf);
	}
	
	private double getTotalEnchantReduction(ItemStack[] armor, DamageCause cause)
	{
		int epf = 0;
		
		for (Enchantment ench : Enchantment.values())
		{
			if (!_protectionTypeModifiers.containsKey(ench.getName()))
			{
				continue;
			}
			if (!_protectionCauses.containsKey(ench.getName()))
			{
				continue;
			}
			if (!Arrays.asList(_protectionCauses.get(ench.getName())).contains(cause))
			{
				continue;
			}
			
			epf += getTotalEPF(ench, armor);
		}
		
		epf = Math.max(0, Math.min(20, epf));
		return new BigDecimal(1).subtract(new BigDecimal(epf).divide(new BigDecimal(25))).doubleValue();
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void StartDamageEvent(EntityDamageEvent event)
	{
		if (!_enabled)
			return;

		boolean preCancel = false;
		if (event.isCancelled())
			preCancel = true;

		if (!(event.getEntity() instanceof LivingEntity))
			return;

		//Get Data
		LivingEntity damagee = GetDamageeEntity(event);
		LivingEntity damager = UtilEvent.GetDamagerEntity(event, true);
		Projectile projectile = GetProjectile(event);

		/*
		 * Fishing rods are disabled because of their custom properties
		 * we want them to behave like default MC.
		 */
		if (projectile instanceof Fish)
			return;

		//Pre-Event Modifications
		if (!DisableDamageChanges)
			WeaponDamage(event, damager);

		double damage = event.getDamage();

		//Consistent Arrow Damage
		if (projectile != null && projectile instanceof Arrow)
		{
			damage = projectile.getVelocity().length() * 3;
		}

		//New Event
		NewDamageEvent(damagee, damager, projectile, event.getCause(), damage, true, false, false, null, null, preCancel);

		//System.out.println(UtilEnt.getName(damagee) + " by " + event.getCause() + " at " + UtilWorld.locToStr(damagee.getLocation()));

		event.setCancelled(true);
	}

	@EventHandler
	public void onEntityCombust(EntityCombustByEntityEvent event)
	{
		if (!_enabled)
			return;

		if (!(event.getCombuster() instanceof Player || event.getCombuster() instanceof Arrow))
			return;

		event.setCancelled(true);
	}

	/**
	 * Removes arrows after hit, especially in cases where arrows may bounce, like if the damage was cancelled.
	 */
	@EventHandler(priority = EventPriority.MONITOR)
	public void removeArrows(EntityDamageEvent event)
	{
		Projectile projectile = GetProjectile(event);

		if (projectile instanceof Arrow)
		{
			projectile.teleport(new Location(projectile.getWorld(), 0, 0, 0));
			projectile.remove();
		}
	}
	
	/**
	 * Removes arrows after hit, especially in cases where arrows may bounce, like if the damage was cancelled.
	 */
	@EventHandler(priority = EventPriority.MONITOR)
	public void removeArrows(CustomDamageEvent event)
	{
		Projectile projectile = event.GetProjectile();

		if (projectile instanceof Arrow)
		{
			projectile.teleport(new Location(projectile.getWorld(), 0, 0, 0));
			projectile.remove();
		}
	}

	public CustomDamageEvent NewDamageEvent(LivingEntity damagee, LivingEntity damager, Projectile proj,
			DamageCause cause, double damage, boolean knockback, boolean ignoreRate, boolean ignoreArmor,
			String source, String reason)
	{
		return NewDamageEvent(damagee, damager, proj,
				cause, damage, knockback, ignoreRate, ignoreArmor,
				source, reason, false);
	}

	public CustomDamageEvent NewDamageEvent(LivingEntity damagee, LivingEntity damager, Projectile proj,
			DamageCause cause, double damage, boolean knockback, boolean ignoreRate, boolean ignoreArmor,
			String source, String reason, boolean cancelled)
	{
		return NewDamageEvent(damagee, damager, proj, null, cause, damage, knockback, ignoreRate, ignoreArmor, source, reason, cancelled);
	}

	public CustomDamageEvent NewDamageEvent(LivingEntity damagee, LivingEntity damager, Projectile proj, Location knockbackOrigin,
			DamageCause cause, double damage, boolean knockback, boolean ignoreRate, boolean ignoreArmor,
			String source, String reason)
	{
		return NewDamageEvent(damagee, damager, proj, knockbackOrigin, cause, damage, knockback, ignoreRate, ignoreArmor, source,
				reason, false);
	}

	public CustomDamageEvent NewDamageEvent(LivingEntity damagee, LivingEntity damager, Projectile proj, Location knockbackOrigin,
			DamageCause cause, double damage, boolean knockback, boolean ignoreRate, boolean ignoreArmor,
			String source, String reason, boolean cancelled)
	{
		CustomDamageEvent customDamageEvent = new CustomDamageEvent(damagee, damager, proj, knockbackOrigin, cause,
				damage, knockback, ignoreRate, ignoreArmor, source, reason, cancelled);
		_plugin.getServer().getPluginManager().callEvent(customDamageEvent);
		return customDamageEvent;
	}

	@EventHandler(priority = EventPriority.LOW)
	public void CancelDamageEvent(CustomDamageEvent event)
	{
		if (event.GetDamageeEntity().getHealth() <= 0)
		{
			event.SetCancelled("0 Health");
			return;
		}
		
		if (event.GetProjectile() != null && event.GetDamageeEntity().equals(event.GetProjectile().getShooter()) && !event.getProjectileDamageSelf())
		{
			event.SetCancelled("Self Projectile Damage");
			return;
		}

		if (event.GetDamageePlayer() != null)
		{
			Player damagee = event.GetDamageePlayer();

			//Not Survival
			if (damagee.getGameMode() != GameMode.SURVIVAL && damagee.getGameMode() != GameMode.ADVENTURE)
			{
				event.SetCancelled("Damagee in Creative");
				return;
			}

			if (UtilPlayer.isSpectator(damagee))
			{
				event.SetCancelled("Damagee in Spectator");
				return;
			}

			//Limit Mob/World Damage Rate
			if (!event.IgnoreRate())
			{
				if (!_combatManager.Get(damagee.getUniqueId()).CanBeHurtBy(event.GetDamagerEntity(true)))
				{
					event.SetCancelled("World/Monster Damage Rate");
					return;
				}
			}
		}

		if (event.GetDamagerPlayer(true) != null)
		{
			Player damager = event.GetDamagerPlayer(true);

			//Not Survival
			if (damager.getGameMode() != GameMode.SURVIVAL && damager.getGameMode() != GameMode.ADVENTURE)
			{
				event.SetCancelled("Damager in Creative");
				return;
			}

			//Damage Rate
			if (!event.IgnoreRate())
				if (!_combatManager.Get(damager.getUniqueId()).CanHurt(event.GetDamageeEntity()))
				{
					event.SetCancelled("PvP Damage Rate");
					return;
				}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void handleEnchants(CustomDamageEvent event)
	{
		if (event.IsCancelled())
			return;

		//Defensive
		Player damagee = event.GetDamageePlayer();
		if (damagee != null)
		{
			if (event.GetDamage() <= 0)
			{
				return;
			}
			if (getTotalEnchantReduction(damagee.getInventory().getArmorContents(), event.GetCause()) > 0)
			{
				event.AddMult("Ench Prot", damagee.getName(), getTotalEnchantReduction(damagee.getInventory().getArmorContents(), event.GetCause()), false);
			}
		}

		//Offensive
		Player damager = event.GetDamagerPlayer(true);
		if (damager != null)
		{
			ItemStack stack = damager.getItemInHand();
			if (stack == null)
				return;

			Map<Enchantment, Integer> enchants = stack.getEnchantments();
			for (Enchantment e : enchants.keySet())
			{
				if (e.equals(Enchantment.ARROW_KNOCKBACK) || e.equals(Enchantment.KNOCKBACK))
					event.AddKnockback("Ench Knockback", 1 + (0.5 * (double)enchants.get(e)));

				else if (e.equals(Enchantment.ARROW_DAMAGE) || e.equals(Enchantment.DAMAGE_ALL))
					event.AddMod("Enchant", "Ench Damage", 0.5 * (double)enchants.get(e), false);

				else if (e.equals(Enchantment.FIRE_ASPECT))
					if (_conditionManager != null)
					{
						double reduce = 0;
						if (damagee != null)
						{
							reduce = (15 * getHighestLevel(Enchantment.PROTECTION_FIRE, damagee.getInventory().getArmorContents())) * (4 * (double)enchants.get(e));
						}
						_conditionManager.Factory().Ignite("Ench Fire", event.GetDamageeEntity(), damager,
								(4 * (double)enchants.get(e)) - reduce, false, false);
					}
			}
		}

		if (event.GetProjectile() instanceof Arrow && event.GetProjectile().getFireTicks() > 0)
		{
			if (_conditionManager != null)
				_conditionManager.Factory().Ignite("Arrow Fire", event.GetDamageeEntity(), damager, 5, false, false);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void EndDamageEvent(CustomDamageEvent event)
	{
		if (!event.IsCancelled() && event.GetDamage() > 0)
		{
			Damage(event);

			//DING ARROW
			if (event.GetProjectile() != null && event.GetProjectile() instanceof Arrow)
			{
				Player player = event.GetDamagerPlayer(true);
				if (player != null)
				{
					if (player.equals(event.GetDamageeEntity()) && !event.getProjectileDamageSelf()) return;
					
					player.playSound(player.getLocation(), Sound.ORB_PICKUP, 0.5f, 0.5f);
				}
			}
		}
	}

	/*
	 * Should only be used to debug the damage event
	 * No modification of the event should take place
	 */
	@EventHandler (priority = EventPriority.MONITOR)
	public void debugDamageEvent(CustomDamageEvent event)
	{
		DisplayDamage(event);
	}

	private void Damage(CustomDamageEvent event) 
	{
		if (event.GetDamageeEntity() == null)
			return;

		if (event.GetDamageeEntity().getHealth() <= 0)
			return;

		if (event.GetProjectile() != null && 
				event.GetDamageeEntity().equals(event.GetProjectile().getShooter()) && 
				!event.getProjectileDamageSelf())
			return;
		
		//Player Conditions
		if (event.GetDamageePlayer() != null)
		{
			//Register Damage (must happen before damage)
			_combatManager.AddAttack(event);
		}

		if (event.GetDamagerPlayer(true) != null && event.DisplayDamageToLevel())
		{
			//Display Damage to Damager
			if (event.GetCause() != DamageCause.THORNS)
				event.GetDamagerPlayer(true).setLevel((int)event.GetDamage());
		}

		try
		{	
			double bruteBonus = 0;
			if (event.IsBrute() && 
					(
							event.GetCause() == DamageCause.ENTITY_ATTACK || 
							event.GetCause() == DamageCause.PROJECTILE || 
							event.GetCause() == DamageCause.CUSTOM
							))// && event.GetDamage() > 2)
				bruteBonus = Math.min(8, event.GetDamage()*2);

			//Do Damage
			HandleDamage(event.GetDamageeEntity(), event.GetDamagerEntity(true), event.GetCause(), (float)(event.GetDamage() + bruteBonus), event.IgnoreArmor());

			//Effect
			event.GetDamageeEntity().playEffect(EntityEffect.HURT);

			//Sticky Arrow
			if (event.GetCause() == DamageCause.PROJECTILE && event.GetProjectile() != null && event.GetProjectile() instanceof Arrow && event.getShowArrows())
				((CraftLivingEntity)event.GetDamageeEntity()).getHandle().o(((CraftLivingEntity)event.GetDamageeEntity()).getHandle().bv() + 1);

			//Knockback
			if (event.IsKnockback() && (event.getKnockbackOrigin() != null || event.GetDamagerEntity(true) != null))
			{
				//Base
				double knockback = _constantKnockback;

				if (_constantKnockback == 0)
				{
					knockback = Math.log10(Math.max(event.GetDamage(), 2));
				}

				//Mults
				for (double cur : event.GetKnockback().values())
				{
					knockback *= cur;
				}

				//Origin
				Location origin = null;
				
				if (event.GetDamagerEntity(true) != null)
					origin = event.GetDamagerEntity(true).getLocation();
				if (event.getKnockbackOrigin() != null)
					origin = event.getKnockbackOrigin();

				//Vec
				Vector trajectory = UtilAlg.getTrajectory2d(origin, event.GetDamageeEntity().getLocation());
				trajectory.multiply(0.6 * knockback);
				trajectory.setY(Math.abs(trajectory.getY()));
				
				if (event.GetProjectile() != null && event.getKnockbackOrigin() == null)
				{
					trajectory = event.GetProjectile().getVelocity();
					trajectory.setY(0);
					trajectory.multiply(0.37 * knockback / trajectory.length());
					trajectory.setY(0.06);
				}

				//Apply
				double vel = 0.2 + trajectory.length() * 0.8;

				UtilAction.velocity(event.GetDamageeEntity(), trajectory, vel, 
						false, 0, Math.abs(0.2 * knockback), 0.4 + (0.04 * knockback), true);
			}
		}
		catch (IllegalAccessException e)
		{
			e.printStackTrace();
		} 
		catch (IllegalArgumentException e)
		{
			e.printStackTrace();
		}
		catch (InvocationTargetException e)
		{
			e.printStackTrace();
		}
	}

	private void DisplayDamage(CustomDamageEvent event) 
	{
		for (Player player : UtilServer.getPlayers())
		{
			if (!UtilGear.isMat(player.getItemInHand(), Material.COMMAND))
				continue;

			UtilPlayer.message(player, " ");
			UtilPlayer.message(player, "=====================================");
			UtilPlayer.message(player, F.elem("Reason ") + event.GetReason());
			UtilPlayer.message(player, F.elem("Cause ") + event.GetCause());
			UtilPlayer.message(player, F.elem("Damager ") + UtilEnt.getName(event.GetDamagerEntity(true)));
			UtilPlayer.message(player, F.elem("Damagee ") + UtilEnt.getName(event.GetDamageeEntity()));
			UtilPlayer.message(player, F.elem("Projectile ") + UtilEnt.getName(event.GetProjectile()));
			UtilPlayer.message(player, F.elem("Damage ") + event.GetDamage());
			UtilPlayer.message(player, F.elem("Damage Initial ") + event.GetDamageInitial());
			for (DamageChange cur : event.GetDamageMod())
				UtilPlayer.message(player, F.elem("Mod ") + cur.GetDamage() + " - " + cur.GetReason() + " by " + cur.GetSource());

			for (DamageChange cur : event.GetDamageMult())
				UtilPlayer.message(player, F.elem("Mult ") + cur.GetDamage() + " - " + cur.GetReason() + " by " + cur.GetSource());

			for (String cur : event.GetKnockback().keySet())
				UtilPlayer.message(player, F.elem("Knockback ") + cur + " = " + event.GetKnockback().get(cur));

			for (String cur : event.GetCancellers())
				UtilPlayer.message(player, F.elem("Cancel ") + cur);
		}
	}

	private void HandleDamage(LivingEntity damagee, LivingEntity damager, DamageCause cause, float damage, boolean ignoreArmor) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
	{
		EntityLiving entityDamagee = ((CraftLivingEntity)damagee).getHandle();
		EntityLiving entityDamager = null;

		if (damager != null)
			entityDamager= ((CraftLivingEntity)damager).getHandle();

		entityDamagee.aC = 1.5F;

		if ((float) entityDamagee.noDamageTicks > (float) entityDamagee.maxNoDamageTicks / 2.0F) 
		{
			if (damage <= entityDamagee.lastDamage)
			{
				return;
			}

			ApplyDamage(entityDamagee, damage - entityDamagee.lastDamage, ignoreArmor);
			entityDamagee.lastDamage = damage;
		}        
		else
		{
			entityDamagee.lastDamage = damage;
			//entityDamagee.aw = entityDamagee.getHealth();
			//entityDamagee.noDamageTicks = entityDamagee.maxNoDamageTicks;
			ApplyDamage(entityDamagee, damage, ignoreArmor);
			//entityDamagee.hurtTicks = entityDamagee.aW = 10;
		}

		if (entityDamager != null)
			entityDamagee.b(entityDamager);

		if (entityDamager != null)
			if (entityDamager instanceof EntityHuman)
			{
				_lastDamageByPlayerTime.setInt(entityDamagee, 100);
				entityDamagee.killer = (EntityHuman)entityDamager;
			}

		if (entityDamagee.getHealth() <= 0) 
		{
			if (entityDamager != null)
			{
				if (entityDamager instanceof EntityHuman)			entityDamagee.die(DamageSource.playerAttack((EntityHuman)entityDamager));
				else if (entityDamager instanceof EntityLiving)		entityDamagee.die(DamageSource.mobAttack((EntityLiving)entityDamager));
				else												entityDamagee.die(DamageSource.GENERIC);
			}
			else
				entityDamagee.die(DamageSource.GENERIC);
		}
	}

	@EventHandler (priority = EventPriority.MONITOR)
	public void DamageSound(CustomDamageEvent event)
	{
		if (event.IsCancelled())
			return;

		if (event.GetCause() != DamageCause.ENTITY_ATTACK && event.GetCause() != DamageCause.PROJECTILE)
			return;

		//Damagee
		LivingEntity damagee = event.GetDamageeEntity();
		if (damagee == null)    return;


		if (_disguiseManager.isDisguised(damagee))
		{
			_disguiseManager.getDisguise(damagee).playHurtSound();
			return;
		}

		//Sound
		Sound sound = Sound.HURT_FLESH;
		float vol = 1f;
		float pitch = 1f;

		//Armor Sound
		if (damagee instanceof Player)
		{
			Player player = (Player)damagee;

			double r = Math.random();

			ItemStack stack = null;

			if (r > 0.50)		stack = player.getInventory().getChestplate();
			else if (r > 0.25)	stack = player.getInventory().getLeggings();
			else if (r > 0.10)	stack = player.getInventory().getHelmet();
			else 				stack = player.getInventory().getBoots();

			if (stack != null)
			{
				if (stack.getType().toString().contains("LEATHER_"))	
				{
					sound = Sound.SHOOT_ARROW;
					pitch = 2f;
				}
				else if (stack.getType().toString().contains("CHAINMAIL_"))	
				{
					sound = Sound.ITEM_BREAK;
					pitch = 1.4f;
				}
				else if (stack.getType().toString().contains("GOLD_"))	
				{
					sound = Sound.ITEM_BREAK;
					pitch = 1.8f;
				}
				else if (stack.getType().toString().contains("IRON_"))	
				{
					sound = Sound.BLAZE_HIT;
					pitch = 0.7f;
				}
				else if (stack.getType().toString().contains("DIAMOND_"))	
				{
					sound = Sound.BLAZE_HIT;
					pitch = 0.9f;
				}	
			}
		}
		//Animal Sound
		else 
		{
			UtilEnt.PlayDamageSound(damagee);
			return;
		}

		damagee.getWorld().playSound(damagee.getLocation(), sound, vol, pitch);
	}

	private void ApplyDamage(EntityLiving entityLiving, float damage, boolean ignoreArmor) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException
	{
		if (!ignoreArmor)
		{
			int j = 25 - entityLiving.br();
			float k = damage * (float)j;

			_k.invoke(entityLiving, damage);
			damage = k / 25.0f;
		}

		/**
		if (entityLiving.hasEffect(MobEffectList.RESISTANCE)) 
		{
			int j = (entityLiving.getEffect(MobEffectList.RESISTANCE).getAmplifier() + 1) * 5;
			int k = 25 - j;
			int l = damage * k + _aS.getInt(entityLiving);

			damage = l / 25;
			_aS.setInt(entityLiving, l % 25);
		}
		 **/

		entityLiving.setHealth(entityLiving.getHealth() - damage);
	}

	private void WeaponDamage(EntityDamageEvent event, LivingEntity ent)
	{
		if (!(ent instanceof Player))
			return;

		if (event.getCause() != DamageCause.ENTITY_ATTACK)
			return;

		Player damager = (Player)ent;

		if (UseSimpleWeaponDamage)
		{
			if (event.getDamage() > 1)
				event.setDamage(event.getDamage() - 1);

			if (UtilGear.isWeapon(damager.getItemInHand()) && damager.getItemInHand().getType().name().contains("GOLD_")) 
				event.setDamage(event.getDamage() + 2);

			return;
		}

		if (damager.getItemInHand() == null || !UtilGear.isWeapon(damager.getItemInHand()))
		{
			event.setDamage(1);
			return;
		}

		Material mat = damager.getItemInHand().getType();

		int damage = 6;

		if (mat.name().contains("WOOD")) damage -= 3;
		else if (mat.name().contains("STONE")) damage -= 2;
		else if (mat.name().contains("DIAMOND")) damage += 1;
		else if (mat.name().contains("GOLD")) damage += 0;

		event.setDamage(damage);
	}

	public LivingEntity GetDamageeEntity(EntityDamageEvent event)
	{
		if (event.getEntity() instanceof LivingEntity)
			return (LivingEntity)event.getEntity();

		return null;
	}

	public Projectile GetProjectile(EntityDamageEvent event)
	{
		if (!(event instanceof EntityDamageByEntityEvent))
			return null;

		EntityDamageByEntityEvent eventEE = (EntityDamageByEntityEvent)event;

		if (eventEE.getDamager() instanceof Projectile)
			return (Projectile)eventEE.getDamager();

		return null;
	}
	
	public boolean IsEnabled()
	{
		return _enabled;
	}

	public void SetEnabled(boolean var)
	{
		_enabled = var;
	}

	/**
	 * Sets the value of {@link #_constantKnockback}.
	 **/
	public void setConstantKnockback(double constantKnockback)
	{
		_constantKnockback = constantKnockback;
	}

	/**
	 * Resets the configuration of damage manager and ensures it is enabled.
	 */
	public void resetConfiguration()
	{
		DisableDamageChanges = false;
		_constantKnockback = 0;
		SetEnabled(true);
	}

	public CombatManager GetCombatManager()
	{
		return _combatManager;
	}

	public void setConditionManager(ConditionManager cm) 
	{
		_conditionManager = cm;
	}
}
