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

public class KillFastStatTracker extends StatTracker<Game>
{
	private final Map<UUID, Integer> _killCount = new HashMap<>();
	private final Map<UUID, Long> _lastKillTime = new HashMap<>();

	private final int _requiredKillCount;
	private final String _stat;
	private final int _timeBetweenKills;

	public KillFastStatTracker(Game game, int requiredKillCount, int timeBetweenKills, String stat)
	{
		super(game);

		_requiredKillCount = requiredKillCount;
		_stat = stat;
		_timeBetweenKills = timeBetweenKills * 1000;
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

		Player player = UtilPlayer.searchExact(event.GetLog().GetPlayer().GetName());
		if (player == null)
			return;

		Long lastTime = _lastKillTime.get(killer.getUniqueId());

		long now = System.currentTimeMillis();

		Integer killCount;
		if (lastTime == null || now - lastTime > getTimeBetweenKills())
			killCount = 0;
		else
		{
			killCount = _killCount.get(killer.getUniqueId());
			if (killCount == null)
				killCount = 0;
		}

		killCount++;

		_killCount.put(killer.getUniqueId(), killCount);
		_lastKillTime.put(killer.getUniqueId(), now);

		_killCount.remove(player.getUniqueId());
		_lastKillTime.remove(player.getUniqueId());

		if (killCount >= getRequiredKillCount())
			addStat(killer, getStat(), 1, true, false);
	}

	public int getRequiredKillCount()
	{
		return _requiredKillCount;
	}

	public int getTimeBetweenKills()
	{
		return _timeBetweenKills;
	}

	public String getStat()
	{
		return _stat;
	}
}
