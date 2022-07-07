package nautilus.game.arcade.game.modules.combattracker;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import mineplex.core.common.util.UtilPlayer;
import mineplex.minecraft.game.core.combat.CombatComponent;
import mineplex.minecraft.game.core.combat.event.CombatDeathEvent;

import nautilus.game.arcade.game.modules.Module;

public class CombatTrackerModule extends Module
{

	private final Map<UUID, CombatData> _combatData;

	public CombatTrackerModule()
	{
		_combatData = new HashMap<>();
	}

	public CombatData getCombatData(Player player)
	{
		return _combatData.computeIfAbsent(player.getUniqueId(), k -> new CombatData());
	}

	@Override
	public void cleanup()
	{
		_combatData.clear();
	}

	@EventHandler
	public void combatDeath(CombatDeathEvent event)
	{
		if (!(event.GetEvent().getEntity() instanceof Player))
		{
			return;
		}

		Player killed = (Player) event.GetEvent().getEntity();

		if (event.GetLog().GetKiller() != null)
		{
			Player killer = UtilPlayer.searchExact(event.GetLog().GetKiller().GetName());

			if (killer != null && !killer.equals(killed))
			{
				getCombatData(killer).incrementKills();
			}
		}

		for (CombatComponent log : event.GetLog().GetAttackers())
		{
			if (event.GetLog().GetKiller() != null && log.equals(event.GetLog().GetKiller()))
			{
				continue;
			}

			Player assist = UtilPlayer.searchExact(log.GetName());

			if (assist != null)
			{
				getCombatData(assist).incrementAssists();
			}
		}
	}
}
