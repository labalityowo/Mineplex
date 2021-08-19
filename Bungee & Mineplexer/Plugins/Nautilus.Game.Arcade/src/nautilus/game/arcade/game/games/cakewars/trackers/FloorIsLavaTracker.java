package nautilus.game.arcade.game.games.cakewars.trackers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilTime;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.Game.GameState;
import nautilus.game.arcade.game.games.cakewars.CakeWars;
import nautilus.game.arcade.stats.StatTracker;

public class FloorIsLavaTracker extends StatTracker<CakeWars>
{

	private static final long GRACE_TIME = TimeUnit.SECONDS.toMillis(30);

	private final Set<Player> _successful;

	public FloorIsLavaTracker(CakeWars game)
	{
		super(game);

		_successful = new HashSet<>();
	}

	@EventHandler
	public void gameStateChange(GameStateChangeEvent event)
	{
		if (event.GetState() == GameState.Live)
		{
			_successful.addAll(getGame().GetPlayers(true));
		}
		else if (event.GetState() == GameState.End)
		{
			List<Player> winners = getGame().getWinners();

			_successful.removeIf(player -> !winners.contains(player));
			_successful.forEach(player -> addStat(player, "FloorIsLava", 1, true, false));
		}
	}

	@EventHandler
	public void updateFloor(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SLOW || !getGame().IsLive() || !UtilTime.elapsed(getGame().GetStateTime(), GRACE_TIME))
		{
			return;
		}

		_successful.removeIf(player ->
		{
			if (!player.isOnline())
			{
				return true;
			}

			Location location = player.getLocation();
			Block block = location.getBlock();
			Set<Block> blocks = getGame().getCakePlayerModule().getPlacedBlocks();
			boolean surrounding = false;

			for (Block nearby : UtilBlock.getSurrounding(block, true))
			{
				if (blocks.contains(nearby))
				{
					surrounding = true;
					break;
				}
			}

			return !blocks.contains(block) && !surrounding && UtilEnt.onBlock(player) && !getGame().getCakeShopModule().isNearShop(location);
		});
	}
}
