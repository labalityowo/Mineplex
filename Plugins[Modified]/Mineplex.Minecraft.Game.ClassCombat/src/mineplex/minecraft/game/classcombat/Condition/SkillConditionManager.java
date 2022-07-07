package mineplex.minecraft.game.classcombat.Condition;

import org.bukkit.plugin.java.JavaPlugin;

import mineplex.minecraft.game.core.condition.ConditionEffect;
import mineplex.minecraft.game.core.condition.ConditionManager;

public class SkillConditionManager extends ConditionManager
{
	public SkillConditionManager(JavaPlugin plugin)
	{
		super(plugin);
	}
	
	public ConditionEffect Effect()
	{
		if (Effect == null)
			Effect = new SkillConditionEffect(this);		

		return Effect;
	}
}
