package nautilus.game.arcade.stats;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

public class MineStrikeLastAliveKillStatTracker extends StatTracker<Minestrike>
{
	private final Map<UUID, Integer> _killCount = new HashMap<>();

	public MineStrikeLastAliveKillStatTracker(Minestrike game)
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

		Player player = UtilPlayer.searchExact(event.GetLog().GetKiller().GetName());
		if (player == null)
			return;

		Integer killCount = _killCount.get(player.getUniqueId());
		_killCount.put(player.getUniqueId(), (killCount == null ? 0 : killCount) + 1);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onRoundOver(GunModule.RoundOverEvent event)
	{
		for (GameTeam team : getGame().GetTeamList())
		{
			List<Player> players = team.GetPlayers(true);

			if (players.size() == 1)
			{
				Player player = players.get(0);
				Integer killCount = _killCount.get(player.getUniqueId());

				if (killCount != null && killCount >= 3)
					addStat(player, "ClutchOrKick", 1, true, false);
			}
		}

		_killCount.clear();
	}
}
