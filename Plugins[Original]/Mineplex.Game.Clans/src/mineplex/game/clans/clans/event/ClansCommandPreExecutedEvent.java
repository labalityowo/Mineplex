package mineplex.game.clans.clans.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ClansCommandPreExecutedEvent extends Event
{
	private static final HandlerList handlers = new HandlerList();

	private Player _player;
	private String[] _args;

	private boolean _cancelled;

	public ClansCommandPreExecutedEvent(Player player, String... args)
	{
		_player = player;
		_args = args;
	}

	public ClansCommandPreExecutedEvent(Player player, Object... args)
	{
		_player = player;

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