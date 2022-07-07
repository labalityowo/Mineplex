package mineplex.game.clans.tutorial.tutorials.clans.objective.goals.clan;

import mineplex.game.clans.clans.gui.events.ClansButtonClickEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import mineplex.game.clans.tutorial.objective.ObjectiveGoal;
import mineplex.game.clans.clans.ClanInfo;
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.clans.event.ClansCommandExecutedEvent;
import mineplex.game.clans.tutorial.tutorials.clans.objective.ClanObjective;
import org.bukkit.event.EventPriority;

public class ClanDetailsGoal extends ObjectiveGoal<ClanObjective>
{
	public ClanDetailsGoal(ClanObjective objective)
	{
		super(objective, "View Clan Details", "View Clan Details with /c");
	}

	@Override
	protected void customStart(Player player)
	{
	}

	@Override
	protected void customFinish(Player player)
	{
	}

	@Override
	public String getDescription(Player player)
	{
		ClanInfo clan = ClansManager.getInstance().getClan(player);
		if (clan != null)
		{
			return "View Clan Details with /c " + clan.getName();
		}
		else
		{
			return "View Clan Details";
		}
	}

	@EventHandler
	public void onClanInfo(ClansCommandExecutedEvent event)
	{
		if (contains(event.getPlayer()))
		{
			if (event.getCommand().equalsIgnoreCase("info"))
			{
				finish(event.getPlayer());
			}
		}
	}


	@EventHandler (priority = EventPriority.HIGHEST)
	public void onClick(ClansButtonClickEvent event) {
		if(contains(event.getPlayer()) && (event.getButtonType().equals(ClansButtonClickEvent.ButtonType.Who)))
			event.setCancelled(false);
	}
}
