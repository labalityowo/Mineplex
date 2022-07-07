package mineplex.game.clans.tutorial.tutorials.clans.objective.goals.finalobj;

import mineplex.game.clans.clans.event.ClanDisbandedEvent;
import mineplex.game.clans.clans.gui.events.ClansButtonClickEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import mineplex.game.clans.clans.event.ClansCommandPreExecutedEvent;
import mineplex.game.clans.tutorial.objective.Objective;
import mineplex.game.clans.tutorial.objective.ObjectiveGoal;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.clans.event.ClansCommandExecutedEvent;
import mineplex.game.clans.tutorial.tutorials.clans.objective.FinalObjective;

public class DisbandClanGoal extends ObjectiveGoal<FinalObjective>
{
	public DisbandClanGoal(FinalObjective objective)
	{
		super(
				objective,
				"Disband Clan",
				"Type '/c' and Disband your Clan",
				"Now that the tutorial is almost finished, let’s delete your Clan. Disbanding a " +
						"Clan will delete it, and unclaim all of your land.",
				null
		);
	}

	@Override
	protected void customStart(Player player)
	{
	}

	@Override
	protected void customFinish(Player player)
	{
		ClansManager.getInstance().resetLeftTimer(player.getUniqueId());
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void teleport(ClanDisbandedEvent event)
	{
		if (!contains(event.getDisbander()))
		{
			return;
		}
		
		event.setCancelled(true);

		UtilPlayer.message(event.getDisbander(), F.main("Clans", "You have disbanded your Tutorial Clan."));
		ClansManager.getInstance().getClanDataAccess().delete(ClansManager.getInstance().getClan(event.getDisbander()), null);
		ClansManager.getInstance().resetLeftTimer(event.getDisbander().getUniqueId());
		finish(event.getDisbander());
	}


	@EventHandler (priority = EventPriority.HIGHEST)
	public void onClick(ClansButtonClickEvent event) {
		if(contains(event.getPlayer()) && event.getButtonType().equals(ClansButtonClickEvent.ButtonType.Disband))
			event.setCancelled(false);
	}
}
