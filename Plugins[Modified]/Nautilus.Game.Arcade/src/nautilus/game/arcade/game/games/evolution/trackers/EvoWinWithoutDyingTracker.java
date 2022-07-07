package nautilus.game.arcade.game.games.evolution.trackers;

import java.util.ArrayList;
import java.util.List;

import mineplex.core.common.util.UtilPlayer;
import mineplex.minecraft.game.core.combat.event.CombatDeathEvent;
import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.Game.GameState;
import nautilus.game.arcade.game.games.evolution.Evolution;
import nautilus.game.arcade.stats.StatTracker;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

public class EvoWinWithoutDyingTracker extends StatTracker<Evolution>
{
	private List<String> _out = new ArrayList<String>();

	public EvoWinWithoutDyingTracker(Evolution evo)
	{
		super(evo);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onCombatDeath(CombatDeathEvent event)
	{
		if (!getGame().IsLive())
			return;

		if (event.GetLog().GetPlayer() == null)
			return;

		if (!event.GetLog().GetPlayer().IsPlayer())
			return;

		Player player = UtilPlayer.searchExact(event.GetLog().GetPlayer().GetName());
		if (player == null || !player.isOnline())
			return;
		
		_out.add(player.getUniqueId().toString());
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onGameStateChange(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.End)
			return;
		
		List<Player> winners = getGame().getWinners();
		if (winners == null)
			return;

		if (winners.size() < 1)
			return;

		Player winner = winners.get(0);

		if (_out.contains(winner.getUniqueId().toString()))
			return;

		addStat(winner, "NoDeaths", 1, false, false);
	}
}
