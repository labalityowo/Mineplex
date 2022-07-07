package mineplex.gemhunters.economy.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class PlayerEarnGemsEvent extends PlayerEvent implements Cancellable
{

	private static final HandlerList HANDLERS = new HandlerList();

	private int _gems;
	private String _reason;
	private boolean _cancel;
	
	public PlayerEarnGemsEvent(Player player, int gems, String reason)
	{
		super(player);
		
		_gems = gems;
		_reason = reason;
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
	
	public void setReason(String reason)
	{
		_reason = reason;
	}
	
	public String getReason()
	{
		return _reason;
	}

	public HandlerList getHandlers()
	{
		return HANDLERS;
	}

	public static HandlerList getHandlerList()
	{
		return HANDLERS;
	}

	@Override
	public boolean isCancelled()
	{
		return _cancel;
	}

	@Override
	public void setCancelled(boolean cancel)
	{
		_cancel = cancel;
	}

}
