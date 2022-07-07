package mineplex.game.clans.tutorial.tutorials.clans.objective.goals.energy;

import mineplex.game.clans.clans.event.ClansCommandPreExecutedEvent;
import mineplex.game.clans.clans.gui.events.ClansButtonClickEvent;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.tutorial.objective.ObjectiveGoal;
import mineplex.game.clans.tutorial.tutorials.clans.objective.EnergyObjective;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

public class ExplainEnergyGoal extends ObjectiveGoal<EnergyObjective>
{
	public ExplainEnergyGoal(EnergyObjective objective)
	{
		super(
				objective,
				"Check your Clans Energy",
				"Type '/c' to check your Clans Energy",
				"Owning land isn’t free! You will need to buy Energy from the Shops to retain " +
						"ownership of it. If your Clan Energy ever reaches 0, you will lose your " +
						"land claims!",
				null
		);
	}

	@Override
	protected void customStart(Player player)
	{
		player.playSound(player.getLocation(), Sound.NOTE_PLING, 1.0f, 1.0f);
	}

	@Override
	protected void customFinish(Player player)
	{
	}

	@EventHandler
	public void onCommand(ClansCommandPreExecutedEvent event)
	{
		if(contains(event.getPlayer()) && event.getArguments().length == 0)
		{
			finish(event.getPlayer());
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onClick(ClansButtonClickEvent event) {
		if(contains(event.getPlayer()) && event.getButtonType().equals(ClansButtonClickEvent.ButtonType.Energy))
			event.setCancelled(false);
	}
}
