package nautilus.game.arcade.game.games.lobbers.events;

import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class TNTThrowEvent extends PlayerEvent
{
	private static final HandlerList _handlers = new HandlerList();

	private TNTPrimed _tnt;
		
	public TNTThrowEvent(Player thrower, TNTPrimed tnt)
	{
		super(thrower);
		
		_tnt = tnt;
	}
	
	public TNTPrimed getTNT()
	{
		return _tnt;
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
