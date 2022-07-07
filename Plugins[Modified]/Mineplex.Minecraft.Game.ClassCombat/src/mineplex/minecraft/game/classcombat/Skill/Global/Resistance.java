package mineplex.minecraft.game.classcombat.Skill.Global;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import mineplex.minecraft.game.core.condition.Condition.ConditionType;
import mineplex.minecraft.game.core.condition.events.ConditionApplyEvent;
import mineplex.minecraft.game.classcombat.Class.IPvpClass.ClassType;
import mineplex.minecraft.game.classcombat.Skill.Skill;
import mineplex.minecraft.game.classcombat.Skill.SkillFactory;

public class Resistance extends Skill
{
	public Resistance(SkillFactory skills, String name, ClassType classType, SkillType skillType, int cost, int levels) 
	{
		super(skills, name, classType, skillType, cost, levels);

		SetDesc(new String[] 
				{
				"Your body and mind is exceptionally resistant.",
				"Durations on you are #20#15 % shorter for;",
				"Slow, Fire, Shock, Confusion, Poison,",
				"Blindness and Jump Prevention"
				});
	}

	@EventHandler
	public void Resist(ConditionApplyEvent event)
	{
		//Dont Resist Self Condition
		if (event.GetCondition().GetReason().equalsIgnoreCase("Hold Position"))
		{
			return;
		}

		ConditionType type = event.GetCondition().GetType();

		switch (type)
		{
			case BURNING:
			case SLOW:
			case SHOCK:
			case CONFUSION:
			case POISON:
			case BLINDNESS:
				int level = getLevel(event.GetCondition().GetEnt());

				if (level == 0)
				{
					return;
				}

				double reduction = -0.20f - (0.15f * level);
				event.GetCondition().ModifyTicks((int) (event.GetCondition().GetTicksTotal() * reduction));
				break;
		}
	}

	@Override
	public void Reset(Player player) 
	{

	}
}
