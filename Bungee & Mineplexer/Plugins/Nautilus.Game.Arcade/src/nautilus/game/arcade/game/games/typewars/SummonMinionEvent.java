package nautilus.game.arcade.game.games.typewars;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class SummonMinionEvent extends Event
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
	private Minion _minion;

	public SummonMinionEvent(Player player, Minion minion)
	{
		_player = player;
		_minion = minion;
	}

	public Player getPlayer()
	{
		return _player;
	}
	
	public Minion getMinion()
	{
		return _minion;
	}
}
