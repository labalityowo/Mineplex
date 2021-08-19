package nautilus.game.arcade.stats;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import mineplex.core.common.util.UtilPlayer;
import mineplex.minecraft.game.core.combat.CombatComponent;
import mineplex.minecraft.game.core.combat.event.CombatDeathEvent;
import nautilus.game.arcade.game.Game;

public class AssistsStatTracker extends StatTracker<Game>
{
	public AssistsStatTracker(Game game)
	{
		super(game);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onCombatDeath(CombatDeathEvent event)
	{
		if (getGame().GetState() != Game.GameState.Live)
			return;

		for (CombatComponent log : event.GetLog().GetAttackers())
		{
			if (event.GetLog().GetKiller() != null && log.equals(event.GetLog().GetKiller()))
				continue;

			Player player = UtilPlayer.searchExact(log.GetName());

			if (player != null)
			{
				addStat(player, "Assists", 1, false, false);

//				if (getGame().GetKit(player) != null)
//					addStat(player, getGame().GetKit(player).getName() + " Assists", 1, false, false);
			}
		}
	}
}
