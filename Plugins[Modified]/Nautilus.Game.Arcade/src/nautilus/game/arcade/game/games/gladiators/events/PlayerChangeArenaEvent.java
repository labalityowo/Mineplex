package nautilus.game.arcade.game.games.gladiators.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import nautilus.game.arcade.game.games.gladiators.Arena;

/**
 * Created by William (WilliamTiger).
 * 08/12/15
 */
public class PlayerChangeArenaEvent extends Event
{
	private static final HandlerList _handlers = new HandlerList();

	private Player player;
	private Arena to;
	private Arena from;

	public PlayerChangeArenaEvent(Player player, Arena to, Arena from)
	{
		this.player = player;
		this.to = to;
		this.from = from;
	}

	public Player getPlayer()
	{
		return player;
	}

	public Arena getTo()
	{
		return to;
	}

	public Arena getFrom()
	{
		return from;
	}

	public static HandlerList getHandlerList()
	{
		return _handlers;
	}

	@Override
	public HandlerList getHandlers()
	{
		return getHandlerList();
	}
}
