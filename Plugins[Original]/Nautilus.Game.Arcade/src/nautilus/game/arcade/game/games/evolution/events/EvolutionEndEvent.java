package nautilus.game.arcade.game.games.evolution.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class EvolutionEndEvent extends PlayerEvent implements Cancellable
{
	/**
	 * @author Mysticate
	 */
	
	private static HandlerList _handlers = new HandlerList();

	private boolean _cancelled = false;
	
	public EvolutionEndEvent(Player who)
	{
		super(who);
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

	@Override
	public boolean isCancelled()
	{
		return _cancelled;
	}

	@Override
	public void setCancelled(boolean flag)
	{
		_cancelled = flag;
	}
}
