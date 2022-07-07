package mineplex.game.clans.tutorial.tutorials.clans.objective;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import mineplex.core.common.util.C;
import mineplex.game.clans.tutorial.TutorialSession;
import mineplex.game.clans.tutorial.objective.OrderedObjective;
import mineplex.game.clans.tutorial.tutorials.clans.ClansMainTutorial;
import mineplex.game.clans.tutorial.tutorials.clans.objective.goals.HoldItemGoal;
import mineplex.game.clans.tutorial.tutorials.clans.objective.goals.fields.GoToFieldsGoal;
import mineplex.game.clans.tutorial.tutorials.clans.objective.goals.fields.MineDiamondsGoal;
import mineplex.game.clans.tutorial.tutorials.clans.objective.goals.fields.SellDiamondsGoal;

public class FieldsObjective extends OrderedObjective<ClansMainTutorial>
{
	public FieldsObjective(ClansMainTutorial clansMainTutorial, JavaPlugin javaPlugin)
	{
		super(clansMainTutorial, javaPlugin, "Fields Tutorial", "Get various resources by mining for them in the fields");

		addGoal(new HoldItemGoal(
				this,
				Material.MAP,
				"Identify Fields on Map",
				"Find the Orange Striped Area on your Map",
				"Fields are marked by " + C.cGold + "Orange Stripes" + C.mBody + ".",
				80L
		));
		addGoal(new GoToFieldsGoal(this));
		addGoal(new MineDiamondsGoal(this));
		addGoal(new SellDiamondsGoal(this));

//		setStartMessageDelay(60);
	}

	@Override
	protected void customLeave(Player player)
	{
	}

	@Override
	protected void customFinish(Player player)
	{
	}

	@Override
	protected void customStart(Player player)
	{
		super.customStart(player);

		TutorialSession session = getPlugin().getTutorialSession(player);
		session.setMapTargetLocation(getPlugin().getCenter(session.getRegion(), ClansMainTutorial.Bounds.FIELDS));
	}
}
