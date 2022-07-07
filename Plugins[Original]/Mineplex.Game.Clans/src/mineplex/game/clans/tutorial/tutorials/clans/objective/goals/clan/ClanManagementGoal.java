package mineplex.game.clans.tutorial.tutorials.clans.objective.goals.clan;

import mineplex.core.common.util.F;
import mineplex.game.clans.clans.ClanInfo;
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.clans.event.ClansCommandExecutedEvent;
import mineplex.game.clans.clans.event.ClansCommandPreExecutedEvent;
import mineplex.game.clans.clans.gui.events.ClansButtonClickEvent;
import mineplex.game.clans.tutorial.objective.ObjectiveGoal;
import mineplex.game.clans.tutorial.tutorials.clans.objective.ClanObjective;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

/**
 * Created by Adam on 29/03/2016.
 */
public class ClanManagementGoal extends ObjectiveGoal<ClanObjective>
{
    public ClanManagementGoal(ClanObjective objective)
    {
        super(
                objective,
                "Open the Clan Menu",
                "Type '/c' to open the Clan Menu",
                "Clan Menu lets you do lots of Clans actions, and view information about your Clan. Take a moment to look at it all!",
                null
        );
    }

    @Override
    protected void customStart(Player player)
    {
        player.sendMessage(F.main("Clans", "You can use the command /c to manage your clan."));
    }

    @Override
    protected void customFinish(Player player)
    {
    }

    @EventHandler
    public void onClanInfo(ClansCommandPreExecutedEvent event)
    {
        if (contains(event.getPlayer()))
        {
            if (event.getArguments() == null || event.getArguments().length == 0)
            {
                finish(event.getPlayer());
            }
        }
    }


    @EventHandler (priority = EventPriority.HIGHEST)
    public void onClick(ClansButtonClickEvent event) {
        if(contains(event.getPlayer()) && event.getButtonType().equals(ClansButtonClickEvent.ButtonType.Energy))
            event.setCancelled(false);
    }
}
