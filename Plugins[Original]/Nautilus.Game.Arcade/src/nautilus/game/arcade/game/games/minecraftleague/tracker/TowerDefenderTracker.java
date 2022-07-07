package nautilus.game.arcade.game.games.minecraftleague.tracker;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.minecraft.game.core.combat.event.CombatDeathEvent;
import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.game.games.minecraftleague.MinecraftLeague;
import nautilus.game.arcade.stats.StatTracker;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

public class TowerDefenderTracker extends StatTracker<MinecraftLeague>
{
	private final int _killCount;
	private final String _stat;
	private final int _timeLimit;

	private final ConcurrentHashMap<UUID, Integer> _kills = new ConcurrentHashMap<UUID, Integer>();
	private final ConcurrentHashMap<UUID, Long> _lastKill = new ConcurrentHashMap<UUID, Long>();

	public TowerDefenderTracker(MinecraftLeague game, int killCount, int timeLimit, String stat)
	{
		super(game);

		_killCount = killCount;
		_stat = stat;
		_timeLimit = timeLimit * 1000;
	}

	public int getKillCount()
	{
		return _killCount;
	}

	public int getTimeLimit()
	{
		return _timeLimit;
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
		Long lastKill = _lastKill.get(player.getUniqueId());

		killCount = (killCount == null ? 0 : killCount) + 1;
		lastKill = (lastKill == null ? System.currentTimeMillis() : lastKill);

		_kills.put(player.getUniqueId(), killCount);
		_lastKill.put(player.getUniqueId(), System.currentTimeMillis());

		if (killCount == getKillCount() && System.currentTimeMillis() - lastKill < getTimeLimit())
		{
			if (UtilMath.offset2d(player.getLocation(), getGame().getActiveTower(getGame().GetTeam(player)).getLocation()) <= 7)
			{
				addStat(player, getStat(), 1, true, false);
				_lastKill.remove(player.getUniqueId());
				_kills.remove(player.getUniqueId());
			}
		}
	}

	public String getStat()
	{
		return _stat;
	}
}