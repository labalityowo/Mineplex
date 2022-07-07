package mineplex.game.clans.tutorial.tutorials.clans.objective.goals.clan;

import mineplex.core.common.util.F;
import mineplex.game.clans.clans.event.ClanCreatedEvent;
import mineplex.game.clans.clans.gui.events.ClansButtonClickEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import mineplex.game.clans.tutorial.objective.ObjectiveGoal;
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.clans.event.ClanCreationCompleteEvent;
import mineplex.game.clans.tutorial.tutorials.clans.objective.ClanObjective;
import org.bukkit.event.EventPriority;

public class CreateClanGoal extends ObjectiveGoal<ClanObjective>
{
	public CreateClanGoal(ClanObjective objective)
	{
		super(
				objective,
				"Create a Clan",
				"Type '/c create <name>' to create a new Clan",
				F.elem("Clans") + " are groups of players that can claim land, build fortresses, " +
						"and fight epic battles. Together they will challenge other clans for " +
						"control of the land.",
				null
		);
	}

	@Override
	protected void customStart(Player player)
	{

		if (ClansManager.getInstance().getClan(player) != null)
		{
			finish(player);
		}
	}

	@Override
	protected void customFinish(Player player)
	{

	}

	@EventHandler
	public void onClanCreate(ClanCreationCompleteEvent event)
	{
		if (contains(event.getFounder()))
		{
			finish(event.getFounder());
			ClansManager.getInstance().resetLeftTimer(event.getFounder().getUniqueId());
		}
	}


	@EventHandler (priority = EventPriority.HIGHEST)
	public void onClick(ClansButtonClickEvent event) {
		if(contains(event.getPlayer()) && event.getButtonType().equals(ClansButtonClickEvent.ButtonType.Create))
			event.setCancelled(false);
	}
}
