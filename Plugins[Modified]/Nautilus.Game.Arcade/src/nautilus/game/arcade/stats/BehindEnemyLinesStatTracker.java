package nautilus.game.arcade.stats;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

import nautilus.game.arcade.game.games.turfforts.TurfForts;
import nautilus.game.arcade.game.games.turfforts.TurfForts.InfiltrateData;

public class BehindEnemyLinesStatTracker extends StatTracker<TurfForts>
{

	public BehindEnemyLinesStatTracker(TurfForts game)
	{
		super(game);
	}

	@EventHandler
	public void onUpdate(UpdateEvent event)
	{
		if (!getGame().IsLive() || event.getType() != UpdateType.SEC)
		{
			return;
		}

		for (Player player : getGame().GetPlayers(true))
		{
			InfiltrateData data = getGame().getInfiltrateData(player);

			if (data != null && data.Seconds > 15)
			{
				addStat(player, "BehindEnemyLines", 1, true, false);
			}
		}
	}
}
