package nautilus.game.arcade.game.games.skyfall.stats;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import mineplex.core.common.util.UtilPlayer;
import mineplex.minecraft.game.core.combat.event.CombatDeathEvent;
import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.stats.StatTracker;

/**
 * StatTracker which collects
 * stats of kills in the air.
 *
 * @author xXVevzZXx
 */
public class AeronaughtStatTracker extends StatTracker<Game>
{

	public AeronaughtStatTracker(Game game)
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
		
		if (!UtilPlayer.isGliding(player))
			return;

		addStat(player, "Aeronaught", 1, false, false);

	}

}
