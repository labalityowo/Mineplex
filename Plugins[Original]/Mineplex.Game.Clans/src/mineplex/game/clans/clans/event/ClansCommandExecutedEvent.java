package mineplex.game.clans.clans.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ClansCommandExecutedEvent extends Event
{
	private static final HandlerList handlers = new HandlerList();
	
	private Player _player;
	private String _command;
	private String[] _args;
	
	private boolean _cancelled;
	
	public ClansCommandExecutedEvent(Player player, String command, String... args)
	{
		_player = player;
		_command = command;
		_args = args;
	}
	
	public ClansCommandExecutedEvent(Player player, String command, Object... args)
	{
		_player = player;
		_command = command;
		
		String[] strArgs = new String[args != null ? args.length : 0];
		
		int index = 0;
		for (Object obj : args)
		{
			strArgs[index] = obj.toString();
			
			index++;
		}
		
		_args = strArgs;
	}
	
	public Player getPlayer()
	{
		return _player;
	}
	
	public String getCommand()
	{
		return _command;
	}
	
	public boolean isCancelled()
	{
		return _cancelled;
	}
	
	public String[] getArguments()
	{
		return _args;
	}
	
	public HandlerList getHandlers()
	{
		return handlers;
	}
	
	public static HandlerList getHandlerList()
	{
		return handlers;
	}
	
	public void setCancelled(boolean cancelled)
	{
		_cancelled = cancelled;
	}
}