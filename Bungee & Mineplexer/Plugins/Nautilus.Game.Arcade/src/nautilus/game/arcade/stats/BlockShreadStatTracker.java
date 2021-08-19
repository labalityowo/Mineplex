package nautilus.game.arcade.stats;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.game.games.turfforts.TurfForts;
import nautilus.game.arcade.game.games.turfforts.kits.KitShredder;

public class BlockShreadStatTracker extends StatTracker<Game>
{
	public BlockShreadStatTracker(Game game)
	{
		super(game);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onShredBlock(TurfForts.ShredBlockEvent event)
	{
		if (getGame().GetState() != Game.GameState.Live)
			return;

		if (event.getArrow().getShooter() instanceof Player)
		{
			Player shooter = (Player) event.getArrow().getShooter();

			if (getGame().GetKit(shooter) instanceof KitShredder)
				addStat(shooter, "TheShreddinator", 1, false, false);
		}
	}
}
