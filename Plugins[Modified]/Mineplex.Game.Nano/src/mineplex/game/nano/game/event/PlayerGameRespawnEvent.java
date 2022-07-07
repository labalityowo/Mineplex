package mineplex.game.nano.game.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

import mineplex.game.nano.game.components.team.GameTeam;

public class PlayerGameRespawnEvent extends PlayerEvent
{

	private static final HandlerList HANDLER_LIST = new HandlerList();

	public static HandlerList getHandlerList()
	{
		return HANDLER_LIST;
	}

	private final GameTeam _team;

	public PlayerGameRespawnEvent(Player who, GameTeam team)
	{
		super(who);

		_team = team;
	}

	public GameTeam getTeam()
	{
		return _team;
	}

	@Override
	public HandlerList getHandlers()
	{
		return HANDLER_LIST;
	}
}
