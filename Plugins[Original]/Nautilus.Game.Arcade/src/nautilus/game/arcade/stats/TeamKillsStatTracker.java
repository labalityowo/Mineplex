package nautilus.game.arcade.stats;

import mineplex.minecraft.game.core.combat.CombatComponent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import mineplex.core.common.util.UtilPlayer;
import mineplex.minecraft.game.core.combat.event.CombatDeathEvent;
import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.game.GameTeam;

public class TeamKillsStatTracker extends StatTracker<Game>
{

	public TeamKillsStatTracker(Game game)
	{
		super(game);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onCombatDeath(CombatDeathEvent event)
	{
		CombatComponent killer = event.GetLog().GetKiller();

		if (!getGame().IsLive() || getGame().GetTeamList().size() < 2 || killer == null || !killer.IsPlayer())
		{
			return;
		}

		Player player = UtilPlayer.searchExact(killer.GetName());

		if (player != null)
		{
			GameTeam team = getGame().GetTeam(player);

			if (team != null && team.GetName() != null)
			{
				addStat(player, team.GetName() + " Kills", 1, false, false);
			}
		}

		for (CombatComponent attacker : event.GetLog().GetAttackers())
		{
			if (!attacker.IsPlayer() || killer.equals(attacker))
			{
				continue;
			}

			Player assist = UtilPlayer.searchExact(attacker.GetName());

			if (assist == null)
			{
				continue;
			}

			GameTeam team = getGame().GetTeam(assist);

			if (team != null && team.GetName() != null)
			{
				addStat(assist, team.GetName() + " Assists", 1, false, false);
			}
		}
	}
}
