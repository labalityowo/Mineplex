package mineplex.core.event;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Created by Shaun on 10/24/2014.
 */
public class CustomTagEvent extends Event
{
	private static final HandlerList handlers = new HandlerList();

	private Player _player;
	private int _entityId;
	private String _customName;

	public CustomTagEvent(Player player, int entityId, String customName)
	{
		_player = player;
		_entityId = entityId;
		_customName = customName;
	}

	public Player getPlayer()
	{
		return _player;
	}

	public String getCustomName()
	{
		return _customName;
	}

	public void setCustomName(String customName)
	{
		_customName = customName;
	}

	public HandlerList getHandlers()
	{
		return handlers;
	}

	public static HandlerList getHandlerList()
	{
		return handlers;
	}

	public int getEntityId()
	{
		return _entityId;
	}
}
