package mineplex.game.clans.tutorial.tutorials.clans.objective.goals.classes;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import mineplex.core.common.util.UtilPlayer;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.tutorial.objective.ObjectiveGoal;
import mineplex.game.clans.tutorial.tutorials.clans.objective.ClassesObjective;
import mineplex.minecraft.game.classcombat.Class.ClientClass;
import mineplex.minecraft.game.classcombat.Class.IPvpClass;
import mineplex.minecraft.game.classcombat.Skill.ISkill;

public class SelectBullsChargeGoal extends ObjectiveGoal<ClassesObjective>
{
	public SelectBullsChargeGoal(ClassesObjective objective)
	{
		super(objective, "Open Class Manager", "Using the Class Manager, choose Bulls Charge for your Axe Skill");
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
	public void update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		getActivePlayers().forEach(uuid -> {
			Player player = UtilPlayer.searchExact(uuid);
			
			if (player == null || !player.isOnline())
			{
				return;
			}
			
			ClientClass client = ClansManager.getInstance().getClassManager().Get(player);
			
			if (client.GetGameClass() != null)
			{
				IPvpClass gameClass = client.GetGameClass();
				
				for (ISkill skill : gameClass.GetSkills())
				{
					if (skill.GetName().toLowerCase().contains("bulls charge"))
					{
						finish(player);
						break;
					}
				}
			}
		});
	}
}
