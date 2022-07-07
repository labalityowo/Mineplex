package mineplex.game.clans.tutorial.tutorials.clans.objective;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import mineplex.game.clans.tutorial.TutorialSession;
import mineplex.game.clans.tutorial.objective.OrderedObjective;
import mineplex.game.clans.tutorial.tutorials.clans.ClansMainTutorial;
import mineplex.game.clans.tutorial.tutorials.clans.objective.goals.classes.EquipDefaultBuildGoal;
import mineplex.game.clans.tutorial.tutorials.clans.objective.goals.classes.OpenClassManagerGoal;
import mineplex.game.clans.tutorial.tutorials.clans.objective.goals.classes.UseBullsChargeGoal;

public class ClassesObjective extends OrderedObjective<ClansMainTutorial>
{
	public ClassesObjective(ClansMainTutorial clansMainTutorial, JavaPlugin javaPlugin)
	{
		super(clansMainTutorial, javaPlugin, "Classes Tutorial", "Learn to use our fully customizable classes");

		addGoal(new EquipDefaultBuildGoal(this));
		addGoal(new OpenClassManagerGoal(this));
		addGoal(new UseBullsChargeGoal(this));

		setStartMessageDelay(60);
	}

	@Override
	protected void customStart(Player player)
	{
		super.customStart(player);

		TutorialSession session = getPlugin().getTutorialSession(player);
		session.setMapTargetLocation(null);
	}

	@Override
	protected void customLeave(Player player)
	{
	}

	@Override
	protected void customFinish(Player player)
	{
	}
}
