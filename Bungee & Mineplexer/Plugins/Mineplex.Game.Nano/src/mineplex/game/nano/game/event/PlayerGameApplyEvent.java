package mineplex.game.nano.game.event;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

import mineplex.game.nano.game.components.team.GameTeam;

public class PlayerGameApplyEvent extends PlayerEvent
{

	private static final HandlerList HANDLER_LIST = new HandlerList();

	public static HandlerList getHandlerList()
	{
		return HANDLER_LIST;
	}

	private final GameTeam _team;
	private Location _respawnLocation;
	private boolean _clearPlayer;

	public PlayerGameApplyEvent(Player who, GameTeam team, Location respawnLocation, boolean clearPlayer)
	{
		super(who);

		_team = team;
		_respawnLocation = respawnLocation;
		_clearPlayer = clearPlayer;
	}

	public GameTeam getTeam()
	{
		return _team;
	}

	public void setRespawnLocation(Location respawnLocation)
	{
		_respawnLocation = respawnLocation;
	}

	public Location getRespawnLocation()
	{
		return _respawnLocation.clone();
	}

	public void setClearPlayer(boolean clearPlayer)
	{
		_clearPlayer = clearPlayer;
	}

	public boolean isClearPlayer()
	{
		return _clearPlayer;
	}

	@Override
	public HandlerList getHandlers()
	{
		return HANDLER_LIST;
	}
}
