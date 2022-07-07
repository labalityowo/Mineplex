package mineplex.minecraft.game.classcombat.Condition;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import mineplex.core.common.util.UtilServer;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.condition.ConditionEffect;
import mineplex.minecraft.game.core.condition.ConditionManager;
import mineplex.minecraft.game.classcombat.Skill.event.SkillTriggerEvent;

public class SkillConditionEffect extends ConditionEffect
{
	public SkillConditionEffect(ConditionManager manager)
	{
		super(manager);
	}

	@EventHandler(priority = EventPriority.LOW)
	public void Silence(SkillTriggerEvent event)
	{
		if (event.IsCancelled())
			return;

		if (!Manager.IsSilenced(event.GetPlayer(), event.GetSkillName()))
			return;

		//Set Damage
		event.SetCancelled(true);
	}
}
