package nautilus.game.arcade.stats;

import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.game.games.wither.events.HumanReviveEvent;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

public class WitherAssaultReviveTracker extends StatTracker<Game>
{
	public WitherAssaultReviveTracker(Game game)
	{
		super(game);
	}
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onCombatDeath(HumanReviveEvent event)
	{
		if (getGame().GetState() != Game.GameState.Live)
			return;

		addStat(event.getPlayer(), "WitherHeal", 1, false, false);
	}

}
