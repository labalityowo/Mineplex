package mineplex.minecraft.game.classcombat.Skill.Global;

import org.bukkit.entity.Player;

import mineplex.minecraft.game.classcombat.Class.IPvpClass.ClassType;
import mineplex.minecraft.game.classcombat.Skill.Skill;
import mineplex.minecraft.game.classcombat.Skill.SkillFactory;

public class Fitness extends Skill
{
	public Fitness(SkillFactory skills, String name, ClassType classType, SkillType skillType, int cost, int levels) 
	{
		super(skills, name, classType, skillType, cost, levels);

		SetDesc(new String[] 
				{
				"Maximum Energy is increased by #0#27 ( #0#15 %)."
				});
	}
	
	@Override
	public void OnPlayerAdd(Player player)
	{
		Factory.Energy().AddEnergyMaxMod(player, GetName(), 18 * getLevel(player));
	}
	
	@Override
	public void Reset(Player player) 
	{
		Factory.Energy().RemoveEnergyMaxMod(player, GetName());
	}
}
