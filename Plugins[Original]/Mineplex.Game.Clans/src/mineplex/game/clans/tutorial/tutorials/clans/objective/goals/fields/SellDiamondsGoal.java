package mineplex.game.clans.tutorial.tutorials.clans.objective.goals.fields;

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
import mineplex.game.clans.tutorial.tutorials.clans.objective.FieldsObjective;
import org.bukkit.event.EventPriority;

public class SellDiamondsGoal extends ObjectiveGoal<FieldsObjective>
{
	public SellDiamondsGoal(FieldsObjective objective)
	{
		super(
				objective,
				"Sell Diamonds",
				"Sell your Diamonds to the Mining Shop",
				"Go back to the Shops and sell your precious diamonds!",
				DyeColor.SILVER
		);
	}

	@Override
	protected void customStart(Player player)
	{
		TutorialSession session = getObjective().getPlugin().getTutorialSession(player);
		session.setMapTargetLocation(getObjective().getPlugin().getPoint(session.getRegion(), ClansMainTutorial.Point.MINING_SHOP));
	}

	@Override
	protected void customFinish(Player player)
	{
		// Close Middle Gate
		getObjective().getPlugin().destroyFences(getObjective().getPlugin().getRegion(player), DyeColor.RED);
		
		// Close Fields Gate
		getObjective().getPlugin().destroyFences(getObjective().getPlugin().getRegion(player), DyeColor.BLACK);
	}

	@EventHandler (priority = EventPriority.HIGH)
	public void onSell(ClansPlayerSellItemEvent event)
	{
		if (contains(event.getPlayer()))
		{
			if (event.getItem().getType() == Material.DIAMOND)
			{
				event.setCancelled(false);
				UtilInv.removeAll(event.getPlayer(), Material.DIAMOND, (byte) 0);
				finish(event.getPlayer());
			}
		}
	}
}
