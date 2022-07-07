package mineplex.gemhunters.economy.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class PlayerCashOutCompleteEvent extends PlayerEvent
{

	private static final HandlerList HANDLERS = new HandlerList();

	private int _gems;

	public PlayerCashOutCompleteEvent(Player player)
	{
		super(player);
	}

	public void incrementGems(int gems)
	{
		_gems += gems;
	}

	public void setGems(int gems)
	{
		_gems = gems;
	}

	public int getGems()
	{
		return _gems;
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
