package mineplex.core.gadget.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class PlayerConsumeMelonEvent extends PlayerEvent
{
	private static final HandlerList handlers = new HandlerList();

	public PlayerConsumeMelonEvent(Player player)
	{
		super(player);
	}

	public HandlerList getHandlers()
	{
		return handlers;
	}

	public static HandlerList getHandlerList()
	{
		return handlers;
	}
}
