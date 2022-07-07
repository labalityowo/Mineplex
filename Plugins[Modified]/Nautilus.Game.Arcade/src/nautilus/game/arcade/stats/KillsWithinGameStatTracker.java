package nautilus.game.arcade.stats;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import mineplex.core.common.util.UtilPlayer;
import mineplex.minecraft.game.core.combat.event.CombatDeathEvent;
import nautilus.game.arcade.game.Game;

public class KillsWithinGameStatTracker extends StatTracker<Game>
{
	private final int _necessaryKillCount;
	private final String _statName;
	private final Map<UUID, Integer> _kills = new HashMap<>();

	public KillsWithinGameStatTracker(Game game, int necessaryKillCount, String statName)
	{
		super(game);

		_necessaryKillCount = necessaryKillCount;
		_statName = statName;
	}

	public String getStatName()
	{
		return _statName;
	}

	public int getNecessaryKillCount()
	{
		return _necessaryKillCount;
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

		Integer killCount = _kills.get(player.getUniqueId());
		killCount = (killCount == null ? 0 : killCount) + 1;
		_kills.put(player.getUniqueId(), killCount);

		if (killCount == getNecessaryKillCount())
			addStat(player, getStatName(), 1, true, false);
	}
}
