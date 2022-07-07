package mineplex.game.clans.tutorial.tutorials.clans.objective;

import mineplex.game.clans.tutorial.tutorials.clans.objective.goals.clan.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import mineplex.game.clans.tutorial.objective.OrderedObjective;
import mineplex.game.clans.tutorial.tutorials.clans.ClansMainTutorial;
import mineplex.game.clans.tutorial.tutorials.clans.objective.goals.attackenemy.BlowUpWallGoal;
import mineplex.game.clans.tutorial.tutorials.clans.objective.goals.attackenemy.StealEnemyPotatoesGoal;

public class ClanObjective extends OrderedObjective<ClansMainTutorial>
{
	public ClanObjective(ClansMainTutorial clansMainTutorial, JavaPlugin javaPlugin)
	{
		super(clansMainTutorial, javaPlugin, "Clans Tutorial", "Create clan with /c create <name>");

		addGoal(new LeaveSpawnGoal(this));
		addGoal(new CreateClanGoal(this));
		addGoal(new ClanManagementGoal(this));
		addGoal(new ClaimLandGoal(this));
		addGoal(new BuildHouseGoal(this));
		addGoal(new SetHomeGoal(this));

		// Wait 1 second because the player is logging in/loading
		setStartMessageDelay(20);
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
