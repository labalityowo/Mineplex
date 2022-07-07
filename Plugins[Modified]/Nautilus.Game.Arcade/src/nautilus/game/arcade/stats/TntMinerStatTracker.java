package nautilus.game.arcade.stats;

import nautilus.game.arcade.game.*;
import nautilus.game.arcade.kit.perks.*;
import org.bukkit.event.*;

public class TntMinerStatTracker extends StatTracker<Game>
{
	public TntMinerStatTracker(Game game)
	{
		super(game);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onEntityExplode(PerkBomber.BomberExplodeDiamondBlock event)
	{
		if (getGame().GetState() != Game.GameState.Live)
			return;

		addStat(event.getPlayer(), "FortuneBomber", 1, false, false);
	}
}
