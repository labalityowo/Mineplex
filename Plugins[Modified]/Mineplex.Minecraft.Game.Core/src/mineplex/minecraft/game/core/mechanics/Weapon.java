package mineplex.minecraft.game.core.mechanics;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;

import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import mineplex.core.MiniPlugin;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilGear;
import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.energy.Energy;

public class Weapon extends MiniPlugin
{
	private Energy _energy;
	
	public Weapon(JavaPlugin plugin, Energy energy) 
	{
		super("Weapon", plugin);
		_energy = energy;
	}

	@EventHandler
	public void ShootBow(EntityShootBowEvent event)
	{
		if (event.getEntity().getLocation().getBlock().isLiquid())
		{
			UtilPlayer.message(event.getEntity(), F.main("Skill", "You cannot use " + F.item("Bow") + " in water."));
			event.setCancelled(true);
			return;
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void ArrowFix(CustomDamageEvent event) 
	{
		Projectile proj = event.GetProjectile();
		if (proj == null)	return;

		if (!(proj instanceof Arrow))
			return;

		event.AddMod("Del", "Arrow Fix", -event.GetDamageInitial(), false);
		event.AddMod("Add", "Arrow Fix", proj.getVelocity().length() * 3, false);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void ArrowDelete(CustomDamageEvent event) 
	{
		if (event.GetCause() != DamageCause.PROJECTILE)
			return;

		Projectile proj = event.GetProjectile();
		if (proj == null) 	return;

		proj.remove();
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void AttackExhaust(CustomDamageEvent event) 
	{
		if (event.GetCause() != DamageCause.ENTITY_ATTACK)
			return;
		
		//No Energy
		Player damager = event.GetDamagerPlayer(false);
		if (damager == null)	return;

		if (damager.hasPotionEffect(PotionEffectType.FAST_DIGGING))
			return;

		if (_energy.Use(damager, "Attack", 1, false, false))
			return;

		//50%
		event.AddMod(damager.getName(), "Exhaustion", -event.GetDamageInitial() + 1, false);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void WeaponDurability(CustomDamageEvent event) 
	{
		if (event.IsCancelled())
			return;

		if (event.GetCause() != DamageCause.ENTITY_ATTACK)
			return;
		
		Player damager = event.GetDamagerPlayer(false);
		if (damager == null)	return;

		if (GoldPower(damager))
			return;

		ItemStack item = damager.getItemInHand();

		if (item == null)
			return;

		if (item.getType().getMaxDurability() == 0)
			return;

		if (UtilGear.isArmor(item))
			return;

		// Don't mess with unbreakable items
		if (item.getItemMeta() != null && item.getItemMeta().spigot().isUnbreakable())
			return;

		item.setDurability((short) (item.getDurability() + 1));

		if (item.getDurability() >= item.getType().getMaxDurability())
		{
			UtilPlayer.message(damager, F.main("Weapon", "Your " + F.item(item.getItemMeta().getDisplayName()) + " has broken."));
			damager.setItemInHand(null);
			UtilInv.Update(damager);
			damager.getWorld().playSound(damager.getLocation(), Sound.ANVIL_LAND, 1f, 0.8f);
		}
	}

	private boolean GoldPower(Player damager) 
	{
		try
		{
			if (!UtilGear.isGold(damager.getItemInHand()))
				return false;

			if (!damager.getInventory().contains(Material.GOLD_NUGGET))
				return false;

			UtilInv.remove(damager, Material.GOLD_NUGGET, (byte)0, 1);
			return true;
		}
		catch (Exception e)
		{
			return false;
		}
	}
}
