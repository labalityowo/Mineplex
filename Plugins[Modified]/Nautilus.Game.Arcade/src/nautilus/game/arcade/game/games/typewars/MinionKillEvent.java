package nautilus.game.arcade.game.games.typewars;

import nautilus.game.arcade.game.games.typewars.TypeWars.KillType;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class MinionKillEvent extends Event implements Cancellable
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
	private KillType _type;
	
	private boolean _canceled;

	public MinionKillEvent(Player player, Minion minion, KillType type)
	{
		_player = player;
		_minion = minion;
		_type = type;
		_canceled = false;
	}

	public Player getPlayer()
	{
		return _player;
	}
	
	public Minion getMinion()
	{
		return _minion;
	}
	
	public KillType getType()
	{
		return _type;
	}

	@Override
	public void setCancelled(boolean cancel)
	{
		_canceled = cancel;
	}
	
	public boolean isCancelled()
	{
		return _canceled;
	}

}
