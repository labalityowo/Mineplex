package mineplex.core.stats.event;


import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class PlayerStatsLoadedEvent extends PlayerEvent
{

	private static final HandlerList HANDLER_LIST = new HandlerList();

	public PlayerStatsLoadedEvent(Player player)
	{
		super(player);
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
