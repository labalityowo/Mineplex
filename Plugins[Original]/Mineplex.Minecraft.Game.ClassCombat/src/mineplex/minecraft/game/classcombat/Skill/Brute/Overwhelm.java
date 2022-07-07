package mineplex.minecraft.game.classcombat.Skill.Brute;

import mineplex.minecraft.game.classcombat.Class.IPvpClass.ClassType;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import mineplex.minecraft.game.classcombat.Skill.Skill;
import mineplex.minecraft.game.classcombat.Skill.SkillFactory;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class Overwhelm extends Skill
{
	public Overwhelm(SkillFactory skills, String name, ClassType classType, SkillType skillType, int cost, int levels) 
	{
		super(skills, name, classType, skillType, cost, levels);

		SetDesc(new String[] 
				{
				"For every 1 health you have more",
				"than your target, you deal 0.25",
				"bonus damage",
				"",				
				"Maximum of #0.5#0.5 bonus damage."
				});
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void Damage(CustomDamageEvent event)
	{
		if (event.IsCancelled())
			return;
		
		if (event.GetCause() != DamageCause.ENTITY_ATTACK)
			return;

		LivingEntity damagee = event.GetDamageeEntity();
		if (damagee == null)	return;

		//Damager
		Player damager = event.GetDamagerPlayer(false);
		if (damager == null)	return;

		double diff = (damager.getHealth() - damagee.getHealth()) * 0.25;
		
		if (diff <= 0)
			return;
		
		//Level
		int level = getLevel(damager);
		if (level == 0)			return;
		
		diff = Math.min(diff, 0.5 + 0.5 * level);

		//Damage
		event.AddMod(damager.getName(), GetName(), diff, true);
	}

	@Override
	public void Reset(Player player) 
	{
		
	}
}