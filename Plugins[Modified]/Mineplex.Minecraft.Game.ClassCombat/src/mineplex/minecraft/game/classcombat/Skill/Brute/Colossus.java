package mineplex.minecraft.game.classcombat.Skill.Brute;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import mineplex.minecraft.game.classcombat.Class.IPvpClass.ClassType;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import mineplex.minecraft.game.classcombat.Skill.Skill;
import mineplex.minecraft.game.classcombat.Skill.SkillFactory;

public class Colossus extends Skill
{
	public Colossus(SkillFactory skills, String name, ClassType classType, SkillType skillType, int cost, int levels) 
	{
		super(skills, name, classType, skillType, cost, levels);

		SetDesc(new String[] 
				{
				"You are so huge that you take",
				"33% less knockback from attacks",
				"and while sneaking you take no",
				"knockback."
				});
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void Damage(CustomDamageEvent event)
	{
		if (event.IsCancelled())
		{
			return;
		}

		Player damagee = event.GetDamageePlayer(), damager = event.GetDamagerPlayer(true);

		if (damagee == null || damager == null)
		{
			return;
		}

		int damageeeLevel = getLevel(damagee), damagerLevel = getLevel(damager);
		DamageCause cause = event.GetCause();

		if (damageeeLevel > 0 && (cause == DamageCause.ENTITY_ATTACK || cause == DamageCause.PROJECTILE))
		{
			if (damagee.isSneaking())
			{
				event.SetKnockback(false);
			}
			else
			{
				event.AddKnockback(GetName(), 0.66);
			}
		}

		if (damagerLevel > 0)
		{
			event.AddMod(GetName(), -0.5);
		}
	}

	@Override
	public void Reset(Player player)
	{

	}
}
