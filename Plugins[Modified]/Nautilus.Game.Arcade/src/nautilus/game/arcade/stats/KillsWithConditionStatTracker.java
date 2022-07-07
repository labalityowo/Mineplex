package nautilus.game.arcade.stats;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import mineplex.core.common.util.UtilPlayer;
import mineplex.minecraft.game.core.combat.event.CombatDeathEvent;
import mineplex.minecraft.game.core.condition.Condition;
import mineplex.minecraft.game.core.condition.events.ConditionApplyEvent;
import nautilus.game.arcade.game.Game;

public class KillsWithConditionStatTracker extends StatTracker<Game>
{
	private final String _statName;
	private final Condition.ConditionType _conditionType;
	private final String _conditionReason;
	private final int _necessaryKillCount;
	private final Map<UUID, Integer> _kills = new HashMap<>();

	public KillsWithConditionStatTracker(Game game, String statName, Condition.ConditionType conditionType, String conditionReason, int necessaryKillCount)
	{
		super(game);

		_statName = statName;
		_conditionType = conditionType;
		_conditionReason = conditionReason;
		_necessaryKillCount = necessaryKillCount;
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
	public void onConditionApply(ConditionApplyEvent event)
	{
		if (event.GetCondition().GetEnt() instanceof Player)
		{
			if (event.GetCondition().GetReason() != null && event.GetCondition().GetReason().contains(getConditionReason()))
				_kills.remove(event.GetCondition().GetEnt().getUniqueId());
		}
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

		Condition condition = getGame().Manager.GetCondition().GetActiveCondition(player, getConditionType());
		if (condition != null && condition.GetType() == getConditionType() && condition.GetReason() != null && condition.GetReason().contains(getConditionReason()))
		{
			Integer kills = _kills.get(player.getUniqueId());
			kills = (kills == null ? 0 : kills) + 1;
			_kills.put(player.getUniqueId(), kills);

			if (kills == getNecessaryKillCount())
				addStat(player, getStatName(), 1, true, false);
		}
		else
			_kills.remove(player.getUniqueId());
	}

	public Condition.ConditionType getConditionType()
	{
		return _conditionType;
	}

	public String getConditionReason()
	{
		return _conditionReason;
	}
}
