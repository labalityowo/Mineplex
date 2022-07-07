package nautilus.game.arcade.game.games.sneakyassassins.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class PlayerMasterAssassinEvent extends PlayerEvent implements Cancellable
{
	private static final HandlerList _handlers = new HandlerList();

	public static HandlerList getHandlerList()
	{
		return _handlers;
	}

	@Override
	public HandlerList getHandlers()
	{
		return getHandlerList();
	}

	private boolean _cancelled = false;

	@Override
	public boolean isCancelled()
	{
		return _cancelled;
	}

	@Override
	public void setCancelled(boolean cancelled)
	{
		_cancelled = cancelled;
	}

	public PlayerMasterAssassinEvent(Player who)
	{
		super(who);
	}
}
