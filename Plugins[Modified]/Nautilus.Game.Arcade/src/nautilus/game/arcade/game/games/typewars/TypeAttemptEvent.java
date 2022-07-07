package nautilus.game.arcade.game.games.typewars;

import java.util.ArrayList;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class TypeAttemptEvent extends Event
{
	private static final HandlerList handlers = new HandlerList();

	public static HandlerList getHandlerList()
	{
		return handlers;
	}

	@Override
	public HandlerList getHandlers()
	{
		return getHandlerList();
	}

	private Player _player;
	private String _attempt;
	private boolean _success;

	public TypeAttemptEvent(Player player, String attempt, boolean sucess)
	{
		_player = player;
		_attempt = attempt;
		_success = sucess;
	}

	public Player getPlayer()
	{
		return _player;
	}
	
	public String getAttempt()
	{
		return _attempt;
	}
	
	public boolean isSuccessful()
	{
		return _success;
	}
}
