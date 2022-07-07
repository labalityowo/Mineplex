package nautilus.game.arcade.stats;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPlaceEvent;

import nautilus.game.arcade.game.Game;

/**
 * Created by TeddehDev on 15/01/2016.
 */
public class BlockPlaceStatTracker extends StatTracker<Game>
{
	private Material[] _ignore;

	public BlockPlaceStatTracker(Game game, Material[] ignore)
	{
		super(game);

		_ignore = ignore;
	}

	@EventHandler
	public void blockPlace(BlockPlaceEvent event)
	{
		if(!getGame().IsLive())
			return;

		if(event.isCancelled())
			return;

		Player player = event.getPlayer();
		if(player == null)
			return;

		if(_ignore.length == 0)
			addStat(event.getPlayer(), "BlocksPlaced", 1, false, false);

		for(Material material : _ignore)
		{
			if(event.getBlock().getType() == material)
				continue;

			addStat(event.getPlayer(), "BlocksPlaced", 1, false, false);
		}
	}
}
