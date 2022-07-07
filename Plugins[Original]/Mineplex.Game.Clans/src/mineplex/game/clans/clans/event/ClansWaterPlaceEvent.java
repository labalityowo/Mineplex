package mineplex.game.clans.clans.event;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ClansWaterPlaceEvent extends Event
{
	private static final HandlerList handlers = new HandlerList();
	
	private Player _player;
	private Block _block;
	
	private boolean _cancelled;
	
	public ClansWaterPlaceEvent(Player player, Block block)
	{
		_player = player;
		_block = block;
	}
	
	public Player getPlayer()
	{
		return _player;
	}
	
	public Block getBlock()
	{
		return _block;
	}

	public void setCancelled(boolean cancelled)
	{
		_cancelled = cancelled;
	}
	
	public boolean isCancelled()
	{
		return _cancelled;
	}
	
	public HandlerList getHandlers()
	{
		return handlers;
	}
	
	public static HandlerList getHandlerList()
	{
		return handlers;
	}
	
}