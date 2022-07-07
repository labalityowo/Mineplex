package mineplex.core.powerplayclub;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PPCDataRequestEvent extends Event
{
	private static final HandlerList handlers = new HandlerList();
	
	private Player _player;
	private PowerPlayData _data;

	public PPCDataRequestEvent(Player player)
	{
		_player = player;
		_data = null;
	}

	public Player getPlayer()
	{
		return _player;
	}
	
	public PowerPlayData getData()
	{
		return _data;
	}

	public void setData(PowerPlayData data)
	{
		_data = data;;
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