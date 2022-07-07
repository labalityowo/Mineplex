package mineplex.core.newnpc.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

import mineplex.core.newnpc.NPC;

public class NPCInteractEvent extends PlayerEvent implements Cancellable
{

	private static final HandlerList _handlers = new HandlerList();

	private final NPC _npc;
	private final boolean _leftClick;

	private boolean _cancel;

	public NPCInteractEvent(Player who, NPC npc, boolean leftClick)
	{
		super(who);

		_npc = npc;
		_leftClick = leftClick;
	}

	public NPC getNpc()
	{
		return _npc;
	}

	public boolean isLeftClick()
	{
		return _leftClick;
	}

	public boolean isRightClick()
	{
		return !isLeftClick();
	}

	public void setCancelled(boolean cancel)
	{
		_cancel = cancel;
	}

	public boolean isCancelled()
	{
		return _cancel;
	}

	public static HandlerList getHandlerList()
	{
		return _handlers;
	}

	@Override
	public HandlerList getHandlers()
	{
		return getHandlerList();
	}

}
