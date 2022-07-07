package mineplex.gemhunters.loot.event;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

import mineplex.gemhunters.loot.ChestProperties;

public class PlayerChestOpenEvent extends PlayerEvent implements Cancellable
{

	private static final HandlerList HANDLERS = new HandlerList();
	
	private boolean _cancel;
	private final Block _block;
	private final ChestProperties _properties;
	
	public PlayerChestOpenEvent(Player who, Block block, ChestProperties properties)
	{
		super(who);
		
		_block = block;
		_properties = properties;
	}
	
	public Block getChest()
	{
		return _block;
	}
	
	public ChestProperties getProperties()
	{
		return _properties;
	}

	public HandlerList getHandlers()
	{
		return HANDLERS;
	}

	public static HandlerList getHandlerList()
	{
		return HANDLERS;
	}

	@Override
	public boolean isCancelled()
	{
		return _cancel;
	}

	@Override
	public void setCancelled(boolean cancel)
	{
		_cancel = cancel;
	}

}
