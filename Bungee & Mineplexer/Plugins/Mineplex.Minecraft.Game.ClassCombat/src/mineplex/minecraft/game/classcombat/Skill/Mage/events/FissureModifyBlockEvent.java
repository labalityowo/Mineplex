package mineplex.minecraft.game.classcombat.Skill.Mage.events;

import org.bukkit.block.Block;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class FissureModifyBlockEvent extends Event implements Cancellable
{
	private static final HandlerList HANDLER_LIST = new HandlerList();

	private final Block _targetBlock;

	private boolean _cancelled;

	public FissureModifyBlockEvent(Block targetBlock)
	{
		this._targetBlock = targetBlock;
	}

	public boolean isCancelled()
	{
		return this._cancelled;
	}

	public void setCancelled(boolean cancelled)
	{
		this._cancelled = cancelled;
	}

	public Block getTargetBlock()
	{
		return this._targetBlock;
	}

	@Override
	public HandlerList getHandlers()
	{
		return HANDLER_LIST;
	}

	public static HandlerList getHandlerList()
	{
		return HANDLER_LIST;
	}
}
