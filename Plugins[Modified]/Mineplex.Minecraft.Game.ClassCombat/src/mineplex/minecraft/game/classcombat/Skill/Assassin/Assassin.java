package mineplex.minecraft.game.classcombat.Skill.Assassin;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.PlayerInventory;

import mineplex.minecraft.game.classcombat.Class.IPvpClass.ClassType;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import mineplex.core.energy.event.EnergyEvent;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.core.updater.UpdateType;
import mineplex.minecraft.game.classcombat.Skill.Skill;
import mineplex.minecraft.game.classcombat.Skill.SkillFactory;

public class Assassin extends Skill
{
	public Assassin(SkillFactory skills, String name, ClassType classType, SkillType skillType, int cost, int levels) 
	{
		super(skills, name, classType, skillType, cost, levels);

		SetDesc(new String[] 
				{
				"Permanent Speed II.",
				"Fall damage reduced by 1.5.",
				"Arrows deal 37.5% less damage."
				});
	}

	@EventHandler
	public void Update(UpdateEvent event)
	{
		if (event.getType() == UpdateType.FAST)
			for (Player cur : GetUsers())
				Factory.Condition().Factory().Speed(GetName(), cur, cur, 1.9, 1, false, false, false);
		
		if (event.getType() == UpdateType.SLOWER)
			for (Player cur : GetUsers())
			{
				PlayerInventory inv = cur.getInventory();
				
				if (inv.getHelmet() != null && inv.getHelmet().getDurability() > 0)
					inv.getHelmet().setDurability((short) (inv.getHelmet().getDurability()-1));
				
				if (inv.getChestplate() != null && inv.getChestplate().getDurability() > 0)
					inv.getChestplate().setDurability((short) (inv.getChestplate().getDurability()-1));
				
				if (inv.getLeggings() != null && inv.getLeggings().getDurability() > 0)
					inv.getLeggings().setDurability((short) (inv.getLeggings().getDurability()-1));
				
				if (inv.getBoots() != null && inv.getBoots().getDurability() > 0)
					inv.getBoots().setDurability((short) (inv.getBoots().getDurability()-1));
			}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void AttackDamage(CustomDamageEvent event)
	{
		if (event.IsCancelled())
			return;

		if (event.GetCause() != DamageCause.ENTITY_ATTACK)
			return;

		Player damager = event.GetDamagerPlayer(false);
		if (damager == null)	return;

		int level = getLevel(damager);
		if (level == 0)		return;

		event.SetKnockback(false);
		event.AddMod(damager.getName(), GetName(), 0, false);
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void FallDamage(CustomDamageEvent event)
	{
		if (event.IsCancelled())
		{
			return;
		}

		Player player = event.GetDamageePlayer();

		if (player == null)
		{
			return;
		}

		int level = getLevel(player);

		if (level == 0)
		{
			return;
		}

		if (event.GetCause() == DamageCause.FALL)
		{
			event.AddMod(null, GetName(), -1.5, false);
		}
		else if (event.GetCause() == DamageCause.PROJECTILE && event.GetProjectile() instanceof Arrow)
		{
			event.AddMod(null, GetName(), -event.GetDamage() * 0.375, false);
		}
	}
	
	@EventHandler
	public void CancelEnergy(EnergyEvent event)
	{
		if (getLevel(event.GetPlayer()) > 0)
			event.setCancelled(true);
	}
	
	@Override
	public void Reset(Player player) 
	{

	}
}
