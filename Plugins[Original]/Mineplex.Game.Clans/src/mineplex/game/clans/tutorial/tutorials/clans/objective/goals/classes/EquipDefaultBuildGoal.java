package mineplex.game.clans.tutorial.tutorials.clans.objective.goals.classes;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import mineplex.game.clans.tutorial.objective.ObjectiveGoal;
import mineplex.game.clans.tutorial.tutorials.clans.objective.ClassesObjective;
import mineplex.minecraft.game.classcombat.Class.event.ClassEquipEvent;

public class EquipDefaultBuildGoal extends ObjectiveGoal<ClassesObjective>
{
	public EquipDefaultBuildGoal(ClassesObjective objective)
	{
		super(
				objective,
				"Equip Armor",
				"Put on your Iron Armor",
				"When you wear a full set of armor, it will equip a class! The Iron set makes you " +
						"into a Knight. Each class has different skills and is strong in its own way.",
				null
		);

//		setStartMessageDelay(120);
	}

	@Override
	protected void customStart(Player player)
	{

	}

	@Override
	protected void customFinish(Player player)
	{

	}

	@EventHandler
	public void classEquip(ClassEquipEvent event)
	{
		if (contains(event.getUser()))
		{
			finish(event.getUser());
		}
	}
}
