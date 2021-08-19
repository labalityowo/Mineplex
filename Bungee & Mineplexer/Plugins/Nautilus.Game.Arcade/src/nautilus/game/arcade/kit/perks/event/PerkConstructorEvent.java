package nautilus.game.arcade.kit.perks.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class PerkConstructorEvent extends PlayerEvent implements Cancellable
{
	private static HandlerList _handlers = new HandlerList();
	
	private boolean _cancelled = false;
	
	public PerkConstructorEvent(Player who)
	{
		super(who);
	}
	
	@Override
	public void setCancelled(boolean cancelled)
	{
		_cancelled = cancelled;
	}
	
	@Override
	public boolean isCancelled()
	{
		return _cancelled;
	}
	
	public static HandlerList getHandlerList()
	{
		return _handlers;
	}
	
	public HandlerList getHandlers()
	{
		return getHandlerList();
	}
}
