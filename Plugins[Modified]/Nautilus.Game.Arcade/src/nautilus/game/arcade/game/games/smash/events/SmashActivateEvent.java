package nautilus.game.arcade.game.games.smash.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class SmashActivateEvent extends PlayerEvent implements Cancellable
{

	private static final HandlerList HANDLERS = new HandlerList();

	private boolean _cancelled;

	public SmashActivateEvent(Player who)
	{
		super(who);
	}

	@Override
	public boolean isCancelled()
	{
		return _cancelled;
	}

	@Override
	public void setCancelled(boolean b)
	{
		_cancelled = b;
	}

	public HandlerList getHandlers()
	{
		return HANDLERS;
	}

	public static HandlerList getHandlerList()
	{
		return HANDLERS;
	}

}
