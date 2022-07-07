package mineplex.minecraft.game.classcombat.Skill.Brute;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityShootBowEvent;

import mineplex.core.energy.event.EnergyEvent;
import mineplex.minecraft.game.classcombat.Class.IPvpClass.ClassType;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import mineplex.minecraft.game.classcombat.Skill.Skill;
import mineplex.minecraft.game.classcombat.Skill.SkillFactory;
import mineplex.minecraft.game.classcombat.Skill.Global.BowDeficiencySkill;

public class Brute extends BowDeficiencySkill
{
	public Brute(SkillFactory skills, String name, ClassType classType, SkillType skillType, int cost, int levels) 
	{
		super(skills, name, classType, skillType, cost, levels);

		SetDesc(new String[] 
				{
					"You take 8 more damage from enemy attacks",
					"to counter the strength of Diamond Armor.",
					"",
					"25% reduction in Arrow Velocity."
				});
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void Damage(CustomDamageEvent event)
	{
		if (event.IsCancelled())
			return;

		Player damagee = event.GetDamageePlayer();
		if (damagee == null)	return;

		int level = getLevel(damagee);
		if (level == 0)		return;

		//Damage
		event.AddMod(damagee.getName(), GetName(), 0, false);
		event.SetBrute();
	}
	
	@EventHandler
	public void CancelEnergy(EnergyEvent event)
	{
		if (getLevel(event.GetPlayer()) > 0)
			event.setCancelled(true);
	}
}
