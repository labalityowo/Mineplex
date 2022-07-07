package nautilus.game.arcade.stats;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import mineplex.core.common.util.UtilPlayer;
import mineplex.minecraft.game.core.combat.event.CombatDeathEvent;
import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.games.minestrike.GunModule;
import nautilus.game.arcade.game.games.minestrike.Minestrike;

public class KillAllOpposingMineStrikeRoundStatTracker extends StatTracker<Minestrike>
{
	private final Map<UUID, Set<UUID>> _kills = new HashMap<>();

	public KillAllOpposingMineStrikeRoundStatTracker(Minestrike game)
	{
		super(game);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onCombatDeath(CombatDeathEvent event)
	{
		if (getGame().GetState() != Game.GameState.Live)
			return;

		if (event.GetLog().GetKiller() == null)
			return;

		if (!event.GetLog().GetKiller().IsPlayer())
			return;

		Player killer = UtilPlayer.searchExact(event.GetLog().GetKiller().GetName());
		if (killer == null)
			return;

		if (event.GetLog().GetPlayer() == null)
			return;

		if (!event.GetLog().GetPlayer().IsPlayer())
			return;

		Player killed = UtilPlayer.searchExact(event.GetLog().GetPlayer().GetName());
		if (killed == null)
			return;

		Set<UUID> kills = _kills.get(killer.getUniqueId());
		if (kills == null)
		{
			kills = new HashSet<>();
			_kills.put(killer.getUniqueId(), kills);
		}

		kills.add(killed.getUniqueId());
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onRoundOver(GunModule.RoundOverEvent event)
	{
		for (GameTeam team : getGame().GetTeamList())
		{
			for (Player player : team.GetPlayers(false))
			{
				Set<UUID> kills = _kills.get(player.getUniqueId());
				if (kills == null)
					continue;

				for (GameTeam otherTeam : getGame().GetTeamList())
				{
					if (otherTeam == team)
						continue;

					boolean killedAll = true;

					for (Player otherPlayer : otherTeam.GetPlayers(true))
					{
						if (!kills.contains(otherPlayer.getUniqueId()))
						{
							killedAll = false;

							break;
						}
					}

					if (killedAll)
						addStat(player, "Ace", 1, true, false);
				}
			}
		}

		_kills.clear();
	}
}
