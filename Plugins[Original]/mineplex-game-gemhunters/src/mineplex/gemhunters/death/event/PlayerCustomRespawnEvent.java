package mineplex.gemhunters.death.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class PlayerCustomRespawnEvent extends PlayerEvent 
{

	private static final HandlerList HANDLERS = new HandlerList();
	
	public PlayerCustomRespawnEvent(Player who)
	{
		super(who);
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
