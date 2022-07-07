package mineplex.game.clans.tutorial.tutorials.clans.objective;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import mineplex.game.clans.tutorial.TutorialSession;
import mineplex.game.clans.tutorial.objective.OrderedObjective;
import mineplex.game.clans.tutorial.tutorials.clans.ClansMainTutorial;
import mineplex.game.clans.tutorial.tutorials.clans.objective.goals.finalobj.DisbandClanGoal;
import mineplex.game.clans.tutorial.tutorials.clans.objective.goals.finalobj.TpClanHomeGoal;

public class FinalObjective extends OrderedObjective<ClansMainTutorial>
{
	public FinalObjective(ClansMainTutorial clansMainTutorial, JavaPlugin javaPlugin)
	{
		super(clansMainTutorial, javaPlugin, "Clans Tutorial 2", "Finalize your knowledge of Clans");

		addGoal(new TpClanHomeGoal(this)); // IMPLEMENTED
		addGoal(new DisbandClanGoal(this)); // IMPLEMENTED

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
