package nautilus.game.arcade.game.games.speedbuilders.events;

import nautilus.game.arcade.game.games.speedbuilders.SpeedBuilders;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PerfectBuildEvent extends Event
{
	private static final HandlerList handlers = new HandlerList();

	private Player _player;
	private SpeedBuilders _game;
	private long _timeElapsed; // Build time elapsed in ms

	public PerfectBuildEvent(Player player, long timeElapsed, SpeedBuilders game)
	{
		_player = player;
		_timeElapsed = timeElapsed;
		_game = game;
	}

	public Player getPlayer()
	{
		return _player;
	}

	public long getTimeElapsed()
	{
		return _timeElapsed;
	}

	public SpeedBuilders getGame()
	{
		return _game;
	}

	public static HandlerList getHandlerList()
	{
		return handlers;
	}

	@Override
	public HandlerList getHandlers()
	{
		return handlers;
	}

}
