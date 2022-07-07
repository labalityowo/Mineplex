package nautilus.game.arcade.stats;

import mineplex.minecraft.game.core.combat.CombatComponent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import mineplex.core.common.util.UtilPlayer;
import mineplex.minecraft.game.core.combat.event.CombatDeathEvent;
import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.game.GameTeam;

public class TeamDeathsStatTracker extends StatTracker<Game>
{
	public TeamDeathsStatTracker(Game game)
	{
		super(game);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onCombatDeath(CombatDeathEvent event)
	{
		CombatComponent component = event.GetLog().GetPlayer();

		if (!getGame().IsLive() || getGame().GetTeamList().size() < 2 || component == null || !component.IsPlayer())
		{
			return;
		}

		Player player = UtilPlayer.searchExact(component.GetName());

		if (player != null)
		{
			GameTeam team = getGame().GetTeam(player);

			if (team != null && team.GetName() != null)
			{
				addStat(player, team.GetName() + " Deaths", 1, false, false);
			}
		}
	}
}
