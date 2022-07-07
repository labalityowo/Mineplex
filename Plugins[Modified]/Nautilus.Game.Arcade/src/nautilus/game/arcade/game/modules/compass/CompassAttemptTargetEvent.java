package nautilus.game.arcade.game.modules.compass;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class CompassAttemptTargetEvent extends PlayerEvent implements Cancellable
{
	private static HandlerList _handlers = new HandlerList();
	private boolean _cancelled = false;

	private Entity _target;

	CompassAttemptTargetEvent(Player player, Entity target)
	{
		super(player);

		_target = target;
	}

	public Entity getTarget()
	{
		return _target;
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
	public void setCancelled(boolean cancelled)
	{
		_cancelled = cancelled;
	}
}
