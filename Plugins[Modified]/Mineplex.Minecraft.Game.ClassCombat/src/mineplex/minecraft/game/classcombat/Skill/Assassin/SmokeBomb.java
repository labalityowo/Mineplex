package mineplex.minecraft.game.classcombat.Skill.Assassin;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftInventoryCrafting;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilGear;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.classcombat.Class.IPvpClass.ClassType;
import mineplex.minecraft.game.classcombat.Skill.Skill;
import mineplex.minecraft.game.classcombat.Skill.SkillFactory;
import mineplex.minecraft.game.classcombat.Skill.event.SkillTriggerEvent;
import mineplex.minecraft.game.core.condition.Condition;
import mineplex.minecraft.game.core.condition.Condition.ConditionType;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

public class SmokeBomb extends Skill
{
	public SmokeBomb(SkillFactory skills, String name, ClassType classType, SkillType skillType, int cost, int levels) 
	{
		super(skills, name, classType, skillType, cost, levels);

		SetDesc(new String[] 
				{
				"Drop Axe/Sword to Use.",
				"",
				"Create a smokey explosion, giving",
				"Blindness to players within #2#1 Blocks",
				"for #1#1 seconds.",
				"",
				"You go invisible for #1#2 seconds."
				});
	}

	@Override
	public String GetRechargeString()
	{
		return "Recharge: #45#-5 Seconds";
	}

	@EventHandler
	public void Use(PlayerDropItemEvent event)
	{
		Player player = event.getPlayer();
		
		if (!(player.getOpenInventory().getTopInventory() instanceof CraftInventoryCrafting))
		{
			return;
		}
		
		int level = getLevel(player);
		if (level == 0)		return;

		if (!UtilGear.isWeapon(event.getItemDrop().getItemStack()))
			return;

		event.setCancelled(true);

		//Check Allowed
		SkillTriggerEvent trigger = new SkillTriggerEvent(player, GetName(), GetClassType());
		Bukkit.getServer().getPluginManager().callEvent(trigger);

		if (trigger.IsCancelled())
			return;
		
		if (isInWater(player))
		{
			UtilPlayer.message(player, F.main("Skill", "You cannot use " + F.skill(GetName()) + " in water."));
			return;
		}

		if (!Recharge.Instance.use(player, GetName(), GetName(level), 45000 - (level * 5000), true, false))
			return;

		//Action
		Factory.Condition().Factory().untrueCloak(GetName(), player, player, 1 + level * 2, false);

		//Effect
		UtilParticle.PlayParticleToAll(ParticleType.HUGE_EXPLOSION, player.getLocation(), 0, 0, 0, 0, 1, ViewDist.LONG);

		for (Player other : UtilPlayer.getNearby(player.getLocation(), 2 + level))
		{
			if (other.equals(player) || !Factory.Relation().canHurt(player, other))
			{
				continue;
			}

			Factory.Condition().Factory().Blind(GetName(), other, player, 1 + level, 0, false, false, false);
		}

		for (int i=0 ; i<3 ; i++)
			player.getWorld().playSound(player.getLocation(), Sound.FIZZ, 2f, 0.5f);

		//Inform
		UtilPlayer.message(player, F.main(GetClassType().name(), "You used " + F.skill(GetName(level)) + "."));
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void EndDamagee(CustomDamageEvent event)
	{
		if (event.IsCancelled())
		{
			return;
		}

		Player damagee = event.GetDamageePlayer();
		if (damagee == null)	return;

		if (getLevel(damagee) == 0)
			return;
		
		if (event.GetDamage() <= 0)
			return;

		//End
		Factory.Condition().EndCondition(damagee, null, GetName());
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void EndDamager(CustomDamageEvent event)
	{
		if (event.IsCancelled())
			return;

		Player damager = event.GetDamagerPlayer(true);
		if (damager == null)	return;

		if (getLevel(damager) == 0)
			return;

		//End
		Factory.Condition().EndCondition(damager, null, GetName());
	}

	@EventHandler
	public void EndInteract(PlayerInteractEvent event)
	{
		if (getLevel(event.getPlayer()) == 0)
			return;

		Factory.Condition().EndCondition(event.getPlayer(), null, GetName());
	}
	
	@EventHandler
	public void closeInv(InventoryCloseEvent event)
	{
		if (getLevel(event.getPlayer()) == 0)
		{
			return;
		}
		
		event.getPlayer().getInventory().addItem(event.getPlayer().getItemOnCursor());
		event.getPlayer().setItemOnCursor(null);
	}
	
	@EventHandler
	public void Smoke(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;

		for (Player cur : GetUsers())
		{
			Condition cond = Factory.Condition().GetActiveCondition(cur, ConditionType.UNTRUE_CLOAK);
			if (cond == null)		continue;

			if (!cond.GetReason().equals(GetName()))
				continue;

			//Smoke
			cur.getWorld().playEffect(cur.getLocation(), Effect.SMOKE, 4);
		}
	}

	@Override
	public void Reset(Player player) 
	{
		//Remove Condition
		Factory.Condition().EndCondition(player, null, GetName());
	}
}