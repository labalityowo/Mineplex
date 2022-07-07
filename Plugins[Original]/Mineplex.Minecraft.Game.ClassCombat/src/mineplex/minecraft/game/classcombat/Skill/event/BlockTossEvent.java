package mineplex.minecraft.game.classcombat.Skill.event;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class BlockTossEvent extends Event implements Cancellable
{
	private static final HandlerList handlers = new HandlerList();
	public static HandlerList getHandlerList() { return handlers; }
	public HandlerList getHandlers() { return handlers; }
	
	private boolean _cancelled;
	public boolean isCancelled() { return _cancelled; }
	public void setCancelled(boolean cancelled) { _cancelled = cancelled; }
	
	private Block _block;
	public Block getBlock() { return _block; }
	public Location getLocation() { return _block.getLocation(); }

    public BlockTossEvent(Block block)
    {
    	_block = block;
    }
}
