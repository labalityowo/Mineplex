package mineplex.game.clans.tutorial.tutorials.clans.objective.goals.shops;

import java.util.UUID;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAlg;
import mineplex.game.clans.tutorial.objective.ObjectiveGoal;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.clans.tutorial.tutorials.clans.ClansMainTutorial;
import mineplex.game.clans.tutorial.tutorials.clans.objective.ShopsObjective;

public class GoToShopsGoal extends ObjectiveGoal<ShopsObjective>
{
	public GoToShopsGoal(ShopsObjective objective)
	{
		super(
				objective,
				"Go to the Shops",
				"Walk to the Shops",
				"The shops are the place where you can buy and sell all sorts of items! "
				+ "The Shops are a " + F.elem("Safe Zone") + ", meaning meaning that players cannot hurt each other.",
				DyeColor.LIGHT_BLUE
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
			if(UtilPlayer.searchExact(uuid) == null) continue;
			getObjective().getPlugin().performGateCheck(UtilPlayer.searchExact(uuid), DyeColor.BROWN);
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
			if (getObjective().getPlugin().isIn(player, ClansMainTutorial.Bounds.SHOPS))
			{
				finish(player);
			}
		}
	}
}
