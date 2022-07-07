package mineplex.minecraft.game.core.combat.itemstack;

import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.minecraft.game.core.combat.CombatComponent;
import mineplex.minecraft.game.core.combat.event.CombatDeathEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;

public class Stats implements Listener
{
	@EventHandler(priority = EventPriority.HIGHEST)
	public void StatsKill(CombatDeathEvent event)
	{
		if (event.GetLog().GetAttackers().isEmpty())
			return;

		CombatComponent kill = event.GetLog().GetAttackers().getFirst();

		if (!kill.IsPlayer())	return;

		Player killer = UtilPlayer.searchExact(kill.GetName());
		if (killer == null)		return;
		
		if (killer.isBlocking())
			return;

		ItemStack item = killer.getItemInHand();

		if (item == null)
			return;

		if (item.getMaxStackSize() > 1)
			return;

		int kills = 1 + ItemStackFactory.Instance.GetLoreVar(item, "Player Kills", 0);

		ItemStackFactory.Instance.SetLoreVar(item, "Player Kills", "" + kills);
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void StatsArmor(CustomDamageEvent event)
	{
		if (event.IsCancelled())
			return;
		
		if (event.GetCause() == DamageCause.SUICIDE)
			return;

		Player damagee = event.GetDamageePlayer();
		if (damagee == null)	return;

		ItemStackFactory.Instance.StatsArmorRename(damagee.getInventory().getHelmet(), (int)event.GetDamage());
		ItemStackFactory.Instance.StatsArmorRename(damagee.getInventory().getChestplate(), (int)event.GetDamage());
		ItemStackFactory.Instance.StatsArmorRename(damagee.getInventory().getLeggings(), (int)event.GetDamage());
		ItemStackFactory.Instance.StatsArmorRename(damagee.getInventory().getBoots(), (int)event.GetDamage());
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void StatsDamage(CustomDamageEvent event)
	{
		if (event.IsCancelled())
			return;

		Player damager = event.GetDamagerPlayer(true);
		
		if (damager == null)
			return;
		
		if (damager.isBlocking())
			return;

		if (event.GetCause() == DamageCause.FIRE_TICK)
			return;
		
		ItemStack item = damager.getItemInHand();

		if (item == null)
			return;

		if (item.getMaxStackSize() > 1)
			return;

		int damage = (int)event.GetDamage() + ItemStackFactory.Instance.GetLoreVar(item, "Damage Dealt", 0);

		ItemStackFactory.Instance.SetLoreVar(item, "Damage Dealt", "" + damage);
		
		if (damage >= 10000)
			item.addEnchantment(Enchantment.DURABILITY, 1);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void StatsBowHit(CustomDamageEvent event)
	{
		if (event.IsCancelled())
			return;

		Player damager = event.GetDamagerPlayer(true);
		if (damager == null)	return;

		Projectile proj = event.GetProjectile();
		if (proj == null)		return;

		ItemStack item = damager.getItemInHand();

		if (item == null)
			return;

		if (item.getType() != Material.BOW)
			return;

		int hits = 1 + ItemStackFactory.Instance.GetLoreVar(item, "Arrows Hit", 0);

		ItemStackFactory.Instance.SetLoreVar(item, "Arrows Hit", "" + hits);

		int shots = ItemStackFactory.Instance.GetLoreVar(item, "Arrows Shot", 0);

		double acc = UtilMath.trim(1, ((double)hits/(double)shots)*100);

		ItemStackFactory.Instance.SetLoreVar(item, "Accuracy", acc + "%");
	}

}
