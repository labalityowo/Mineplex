package mineplex.minecraft.game.classcombat.Skill.Global;

import mineplex.minecraft.game.classcombat.Class.IPvpClass.ClassType;
import mineplex.minecraft.game.classcombat.Skill.Skill;
import mineplex.minecraft.game.classcombat.Skill.SkillFactory;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityShootBowEvent;

/**
 * The BowDeficiencySkill provides a reduction in overall arrow velocity and damage to owners.
 * @author MrTwiggy
 *
 */
public class BowDeficiencySkill extends Skill
{

	public BowDeficiencySkill(SkillFactory skills, String name, ClassType classType, SkillType skillType, int cost, int maxLevel) 
	{
		super(skills, name, classType, skillType, cost, maxLevel);
	}

	/**
	 * Reduce outgoing arrow velocity by bow deficiency owners.
	 * @param event
	 */
	@EventHandler
	public void BowShoot(EntityShootBowEvent event)
	{
		if (getLevel(event.getEntity()) == 0)
			return;
		
		event.getProjectile().setVelocity(event.getProjectile().getVelocity().multiply(0.75));
	}
	
	/**
	 * Reduce damage output of arrows shot by bow deficiency owners.
	 * @param event
	 */
	@EventHandler
	public void onArrowDamage(CustomDamageEvent event)
	{
		// Check to see if arrow was shot by owner of this Skill.
		if (event.GetProjectile() == null || getLevel(event.GetDamagerEntity(true)) == 0)
			return;
		
		event.AddMod("BowDeficiencySkill", -2.0d);
	}

	@Override
	public void Reset(Player player)
	{
		
	}
}
