package mineplex.game.nano.game.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

import mineplex.game.nano.game.components.team.GameTeam;

public class PlayerStateChangeEvent extends PlayerEvent
{

	private static final HandlerList HANDLER_LIST = new HandlerList();

	public static HandlerList getHandlerList()
	{
		return HANDLER_LIST;
	}

	private final GameTeam _team;
	private final boolean _alive;

	public PlayerStateChangeEvent(Player who, GameTeam team, boolean alive)
	{
		super(who);

		_team = team;
		_alive = alive;
	}

	public GameTeam getTeam()
	{
		return _team;
	}

	public boolean isAlive()
	{
		return _alive;
	}

	@Override
	public HandlerList getHandlers()
	{
		return HANDLER_LIST;
	}
}
