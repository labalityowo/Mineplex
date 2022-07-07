package nautilus.game.arcade.stats;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import mineplex.core.common.util.UtilPlayer;
import mineplex.minecraft.game.core.combat.event.CombatDeathEvent;

import nautilus.game.arcade.game.Game;

public class HunterKillerStatTracker extends StatTracker<Game>
{
	private final Map<UUID, Integer> _huntersKilled = new HashMap<>();

	public HunterKillerStatTracker(Game game)
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

		Player player = UtilPlayer.searchExact(event.GetLog().GetPlayer().GetName());
		if (player == null)
			return;

		if (getGame().GetTeam(player).GetColor() == ChatColor.RED)
		{
			Integer huntersKilled = _huntersKilled.get(killer.getUniqueId());

			huntersKilled = (huntersKilled == null ? 0 : huntersKilled) + 1;

			_huntersKilled.put(killer.getUniqueId(), huntersKilled);

			if (huntersKilled >= 10)
				addStat(killer, "HunterKiller", 1, true, false);
		}
	}
}
