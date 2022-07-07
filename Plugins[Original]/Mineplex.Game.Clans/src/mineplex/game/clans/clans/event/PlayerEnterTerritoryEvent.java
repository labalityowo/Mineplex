package mineplex.game.clans.clans.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerEnterTerritoryEvent extends Event
{
	private static final HandlerList handlers = new HandlerList();
	
	private String _newTerritory;
	private String _lastTerritory;
	private Player _player;
	
	private boolean _sendMessage;
	
	private boolean _safe;
	
	public PlayerEnterTerritoryEvent(Player player, String lastTerritory, String newTerritory, boolean safe, boolean sendMessage)
	{
		_player = player;
		_lastTerritory = lastTerritory;
		_newTerritory = newTerritory;
		_safe = safe;
		_sendMessage = sendMessage;
	}
	
	public Player getPlayer()
	{
		return _player;
	}
	
	public boolean willSendMessage()
	{
		return _sendMessage;
	}
	
	public void setSendMessage(boolean flag)
	{
		_sendMessage = flag;
	}
	
	public String getLastTerritory()
	{
		return _lastTerritory;
	}
	
	public String getNewTerritory()
	{
		return _newTerritory;
	}
	
	public HandlerList getHandlers()
	{
		return handlers;
	}
	
	public static HandlerList getHandlerList()
	{
		return handlers;
	}

	public boolean isSafe()
	{
		return _safe;
	}
}