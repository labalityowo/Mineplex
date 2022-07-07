package mineplex.game.clans.tutorial.tutorials.clans.objective.goals.attackenemy;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import mineplex.core.common.util.C;
import mineplex.game.clans.clans.map.events.PlayerGetMapEvent;
import mineplex.game.clans.clans.siege.weapon.Cannon;
import mineplex.game.clans.tutorial.objective.ObjectiveGoal;
import mineplex.game.clans.tutorial.tutorials.clans.objective.AttackEnemyObjective;

public class GetMapGoal extends ObjectiveGoal<AttackEnemyObjective>
{
	public GetMapGoal(AttackEnemyObjective objective)
	{
		super(
				objective,
				"Get a Map",
				"Type '/map' to get a Map",
				"You can get a Map any time you need one. The map will show you who " +
						"owns the land around the map. Your clan is " + C.cAqua + "blue" +
						C.mBody + ", your allies are " + C.cGreen + "green" + C.mBody + ", " +
						"and your enemies are " + C.cRed + "red" + C.mBody + ".",
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
	public void onGetMap(PlayerGetMapEvent event)
	{
		if (getObjective().getPlugin().isInTutorial(event.getPlayer()))
		{
			event.setCancelled(true);
			getObjective().getPlugin().getMapManager().setMap(event.getPlayer());

			if (contains(event.getPlayer()))
			{
				finish(event.getPlayer());
			}
		}
	}
}
