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
import mineplex.minecraft.game.core.combat.CombatComponent;
import mineplex.minecraft.game.core.combat.CombatDamage;
import mineplex.minecraft.game.core.combat.event.CombatDeathEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import mineplex.minecraft.game.core.damage.DamageChange;
import nautilus.game.arcade.game.Game;

public class KillReasonStatTracker extends StatTracker<Game>
{
	private final String _reason;
	private final String _statName;
	private final boolean _canBeDamagedByKilledPlayer;
	private final Map<UUID, Set<UUID>> _damaged = new HashMap<>();

	public KillReasonStatTracker(Game game, String reason, String statName, boolean canBeDamagedByKilledPlayer)
	{
		super(game);

		_reason = reason;
		_statName = statName;
		_canBeDamagedByKilledPlayer = canBeDamagedByKilledPlayer;
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onCustomDamage(CustomDamageEvent event)
	{
		if (canBeDamagedByKilledPlayer())
			return;

		if (getGame().GetState() != Game.GameState.Live)
			return;

		Player damager = event.GetDamagerPlayer(false);
		if (damager == null)
			return;

		Player damagee = event.GetDamageePlayer();
		if (damagee == null)
			return;

		Set<UUID> set = _damaged.get(damagee.getUniqueId());
		if (set == null)
		{
			set = new HashSet<>();
			_damaged.put(damagee.getUniqueId(), set);
		}
		set.add(damager.getUniqueId());
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

		if (!canBeDamagedByKilledPlayer())
		{
			Set<UUID> set = _damaged.remove(killer.getUniqueId());
			if (set != null && set.contains(player.getUniqueId()))
				return;
		}

		if (event.GetLog().GetLastDamager() != null && event.GetLog().GetLastDamager().GetReason() != null && event.GetLog().GetLastDamager().GetReason().contains(getReason()))
			addStat(killer, getStatName(), 1, false, false);
		else
		{
			for (CombatComponent component : event.GetLog().GetAttackers())
			{
				for (CombatDamage damage : component.GetDamage())
				{
					if (damage.getDamageMod() != null)
					{
						for (DamageChange mod : damage.getDamageMod())
						{
							if (mod.GetReason() != null && mod.GetReason().contains(getReason()))
							{
								addStat(killer, getStatName(), 1, false, false);

								return;
							}
						}
					}
				}
			}
		}
	}

	public String getStatName()
	{
		return _statName;
	}

	public String getReason()
	{
		return _reason;
	}

	public boolean canBeDamagedByKilledPlayer()
	{
		return _canBeDamagedByKilledPlayer;
	}
}
