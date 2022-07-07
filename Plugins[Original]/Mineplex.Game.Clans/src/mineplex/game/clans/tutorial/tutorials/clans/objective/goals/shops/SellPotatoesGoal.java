package mineplex.game.clans.tutorial.tutorials.clans.objective.goals.shops;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilPlayer;
import mineplex.game.clans.clans.event.ClansPlayerSellItemEvent;
import mineplex.game.clans.tutorial.TutorialSession;
import mineplex.game.clans.tutorial.objective.ObjectiveGoal;
import mineplex.game.clans.tutorial.tutorials.clans.ClansMainTutorial;
import mineplex.game.clans.tutorial.tutorials.clans.objective.ShopsObjective;
import org.bukkit.event.EventPriority;

public class SellPotatoesGoal extends ObjectiveGoal<ShopsObjective>
{
	public SellPotatoesGoal(ShopsObjective objective)
	{
		super(
				objective,
				"Sell Potatoes",
				"Sell your Potatoes to the " + F.elem("Organic Produce Shop"),
				"Farming is a great way to make money in Clans. Build a farm in your land, " +
				"harvest the crops, and sell it to the shops for profit!",
				DyeColor.PINK
		);
	}

	@Override
	protected void customStart(Player player)
	{
		TutorialSession session = getObjective().getPlugin().getTutorialSession(player);
		session.setMapTargetLocation(getObjective().getPlugin().getPoint(session.getRegion(), ClansMainTutorial.Point.FARMING_SHOP));
	}

	@Override
	protected void customFinish(Player player)
	{
		// Shops Fences Closed
		getObjective().getPlugin().spawnFences(getObjective().getPlugin().getRegion(player), DyeColor.BROWN);

		// Remove all potatoes from inventory
		UtilInv.removeAll(player, Material.POTATO_ITEM, (byte) 0);
	}

	@EventHandler (priority = EventPriority.HIGH)
	public void onSell(ClansPlayerSellItemEvent event)
	{
		if (contains(event.getPlayer())) {
			if (event.getItem().getType() == Material.POTATO_ITEM) {
				event.setCancelled(false);
				finish(event.getPlayer());
			}
		}
	}
}
