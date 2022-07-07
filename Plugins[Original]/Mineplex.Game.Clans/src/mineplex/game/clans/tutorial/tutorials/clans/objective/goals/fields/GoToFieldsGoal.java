package mineplex.game.clans.tutorial.tutorials.clans.objective.goals.fields;

import java.util.UUID;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import mineplex.core.common.util.UtilAlg;
import mineplex.game.clans.tutorial.objective.ObjectiveGoal;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.clans.tutorial.tutorials.clans.ClansMainTutorial;
import mineplex.game.clans.tutorial.tutorials.clans.objective.FieldsObjective;

public class GoToFieldsGoal extends ObjectiveGoal<FieldsObjective>
{
	public GoToFieldsGoal(FieldsObjective objective)
	{
		super(
				objective,
				"Go to the Fields",
				"Go to the Fields",
				"The Fields are a very dangerous place where players come to fight and harvest " +
						"resources!",
				DyeColor.YELLOW
		);
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
	public void openGates(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTER)
		{
			return;
		}

		for (UUID uuid : getActivePlayers())
		{
			if(UtilPlayer.searchExact(uuid) == null) return;

			getObjective().getPlugin().performGateCheck(UtilPlayer.searchExact(uuid), DyeColor.RED);
		}
	}

	@EventHandler
	public void checkRegion(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;

		for (UUID uuid : getActivePlayers())
		{
			Player player = UtilPlayer.searchExact(uuid);
			if(player == null || !player.isOnline()) continue;
			if (getObjective().getPlugin().isIn(player, ClansMainTutorial.Bounds.FIELDS))
			{
				finish(player);
			}
		}
	}
}
