package mineplex.game.clans.tutorial.tutorials.clans.objective;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import mineplex.game.clans.tutorial.TutorialSession;
import mineplex.game.clans.tutorial.objective.OrderedObjective;
import mineplex.game.clans.tutorial.tutorials.clans.ClansMainTutorial;
import mineplex.game.clans.tutorial.tutorials.clans.objective.goals.energy.BuyEnergyGoal;
import mineplex.game.clans.tutorial.tutorials.clans.objective.goals.energy.ExplainEnergyGoal;

public class EnergyObjective extends OrderedObjective<ClansMainTutorial>
{
	public EnergyObjective(ClansMainTutorial clansMainTutorial, JavaPlugin javaPlugin)
	{
		super(clansMainTutorial, javaPlugin, "Energy Tutorial", "A Clan requires Energy to maintain all of it's territory.");

		addGoal(new ExplainEnergyGoal(this));
		addGoal(new BuyEnergyGoal(this));

		setStartMessageDelay(60);
	}

	@Override
	protected void customStart(Player player)
	{
		super.customStart(player);

		TutorialSession session = getPlugin().getTutorialSession(player);
		session.setMapTargetLocation(getPlugin().getPoint(session.getRegion(), ClansMainTutorial.Point.ENERGY_SHOP));
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
