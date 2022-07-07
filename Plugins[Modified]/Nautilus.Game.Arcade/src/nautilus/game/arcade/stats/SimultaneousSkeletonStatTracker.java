package nautilus.game.arcade.stats;

import nautilus.game.arcade.game.*;
import nautilus.game.arcade.kit.perks.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;

import java.util.*;

public class SimultaneousSkeletonStatTracker extends StatTracker<Game>
{
	private final int _requiredCount;

	public SimultaneousSkeletonStatTracker(Game game, int requiredCount)
	{
		super(game);

		_requiredCount = requiredCount;
	}

	@EventHandler
	public void onMinionSpawn(PerkSkeletons.MinionSpawnEvent event)
	{
		if (getGame().GetState() != Game.GameState.Live)
			return;

		List<Skeleton> skeletons = event.getPerkSkeletons().getSkeletons(event.getPlayer());

		if (skeletons != null)
		{
			int aliveCount = 0;

			for (Skeleton skeleton : skeletons)
			{
				if (!skeleton.isDead())
					aliveCount++;
			}

			if (aliveCount >= getRequiredCount())
				addStat(event.getPlayer(), "Skeletons", 1, true, false);
		}
	}

	public int getRequiredCount()
	{
		return _requiredCount;
	}
}
