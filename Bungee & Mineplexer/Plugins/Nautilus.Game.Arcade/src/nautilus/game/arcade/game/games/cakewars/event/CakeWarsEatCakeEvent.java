package nautilus.game.arcade.game.games.cakewars.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.games.cakewars.team.CakeTeam;

public class CakeWarsEatCakeEvent extends PlayerEvent
{

	private static final HandlerList HANDLER_LIST = new HandlerList();

	private final CakeTeam _cakeTeam;

	public CakeWarsEatCakeEvent(Player who, CakeTeam cakeTeam)
	{
		super(who);

		_cakeTeam = cakeTeam;
	}

	public CakeTeam getCakeTeam()
	{
		return _cakeTeam;
	}

	public GameTeam getGameTeam()
	{
		return _cakeTeam.getGameTeam();
	}

	public static HandlerList getHandlerList()
	{
		return HANDLER_LIST;
	}

	@Override
	public HandlerList getHandlers()
	{
		return getHandlerList();
	}

}
