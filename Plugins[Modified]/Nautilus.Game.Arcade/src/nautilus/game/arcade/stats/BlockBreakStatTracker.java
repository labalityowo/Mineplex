package nautilus.game.arcade.stats;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;

import nautilus.game.arcade.game.Game;

/**
 * Created by TeddehDev on 15/01/2016.
 */
public class BlockBreakStatTracker extends StatTracker<Game>
{
	private boolean _blockDamage;

	/**
	 * @param blockDamage
	 *   -  true = triggers block damage event
	 *   -  false = triggers block break event
	 */
	public BlockBreakStatTracker(Game game, boolean blockDamage)
	{
		super(game);

		_blockDamage = blockDamage;
	}

	@EventHandler
	public void blockBreak(BlockBreakEvent event)
	{
		if(!getGame().IsLive())
			return;

		if(event.isCancelled())
			return;

		if(_blockDamage)
			return;

		Player player = event.getPlayer();
		if(player == null)
			return;

		addStat(player, "BlocksBroken", 1, false, false);
	}

	@EventHandler
	public void blockBreak(BlockDamageEvent event)
	{
		if(!getGame().IsLive())
			return;

		if(event.isCancelled())
			return;

		if(!_blockDamage)
			return;

		Player player = event.getPlayer();
		if(player == null)
			return;

		addStat(player, "BlocksBroken", 1, false, false);
	}

	public void addStat(Player player)
	{
		addStat(player, "BlocksBroken", 1, false, false);
	}
}
