package nautilus.game.arcade.stats;

import mineplex.minecraft.game.classcombat.Skill.Brute.*;
import nautilus.game.arcade.game.*;
import org.bukkit.event.*;

public class SeismicSlamStatTracker extends StatTracker<Game>
{
	public SeismicSlamStatTracker(Game game)
	{
		super(game);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onLongshotHit(SeismicSlam.SeismicSlamEvent event)
	{
		if (getGame().GetState() != Game.GameState.Live)
			return;

		if (event.getTargets().size() >= 5)
			addStat(event.getPlayer(), "Earthquake", 1, true, false);
	}
}
