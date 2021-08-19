package nautilus.game.arcade.game.modules.rejoin;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

import nautilus.game.arcade.game.modules.rejoin.RejoinModule.PlayerGameInfo;

public class PlayerRejoinGameEvent extends PlayerEvent implements Cancellable
{

	private static final HandlerList HANDLER_LIST = new HandlerList();

	public static HandlerList getHandlerList()
	{
		return HANDLER_LIST;
	}

	private final PlayerGameInfo _playerGameInfo;

	private boolean _cancelled;

	PlayerRejoinGameEvent(Player who, PlayerGameInfo playerGameInfo)
	{
		super(who);

		_playerGameInfo = playerGameInfo;
	}

	public PlayerGameInfo getPlayerGameInfo()
	{
		return _playerGameInfo;
	}

	@Override
	public void setCancelled(boolean cancelled)
	{
		_cancelled = cancelled;
	}

	@Override
	public boolean isCancelled()
	{
		return _cancelled;
	}

	@Override
	public HandlerList getHandlers()
	{
		return HANDLER_LIST;
	}
}
