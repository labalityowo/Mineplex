package nautilus.game.arcade.game.games.lobbers.events;

import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class TNTPreExplodeEvent extends PlayerEvent implements Cancellable
{
	private static final HandlerList _handlers = new HandlerList();

	private boolean _cancelled = false;
	
	private TNTPrimed _tnt;
		
	public TNTPreExplodeEvent(Player thrower, TNTPrimed tnt)
	{
		super(thrower);
		
		_tnt = tnt;
	}
	
	public TNTPrimed getTNT()
	{
		return _tnt;
	}
	
	@Override
	public void setCancelled(boolean flag)
	{
		_cancelled = flag;
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
	
	@Override
	public HandlerList getHandlers()
	{
		return getHandlerList();
	}

}
