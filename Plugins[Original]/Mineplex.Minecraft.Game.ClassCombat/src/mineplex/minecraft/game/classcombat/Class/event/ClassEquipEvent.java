package mineplex.minecraft.game.classcombat.Class.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import mineplex.minecraft.game.classcombat.Class.ClientClass;
import mineplex.minecraft.game.classcombat.Class.repository.token.CustomBuildToken;

public class ClassEquipEvent extends Event
{
	private static final HandlerList handlers = new HandlerList();
	
	private ClientClass _client;
	private Player _user;
	private CustomBuildToken _build;
	
	private boolean _cancelled;
	
	public ClassEquipEvent(ClientClass client, CustomBuildToken build, Player user)
	{
		_client = client;
		_build = build;
		_user = user;
	}
	
	public ClientClass getPlayer()
	{
		return _client;
	}
	
	public CustomBuildToken getBuild()
	{
		return _build;
	}
	
	public Player getUser()
	{
		return _user;
	}
	
	public void setCancelled(boolean cancelled)
	{
		_cancelled = cancelled;
	}
	
	public boolean isCancelled()
	{
		return _cancelled;
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