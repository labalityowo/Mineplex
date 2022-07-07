package nautilus.game.arcade.game.games.spleef;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockEvent;

public class SpleefDestroyBlockEvent extends BlockEvent
{
	private static final HandlerList handlers = new HandlerList();

	public static HandlerList getHandlerList()
	{
		return handlers;
	}

	@Override
	public HandlerList getHandlers()
	{
		return getHandlerList();
	}

	private final Player _player;

	public SpleefDestroyBlockEvent(Block theBlock, Player player)
	{
		super(theBlock);

		_player = player;
	}

	public Player getPlayer()
	{
		return _player;
	}
}
