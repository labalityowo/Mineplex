package mineplex.hub.hubgame.common.general;

import java.util.concurrent.TimeUnit;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilTime;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.hub.hubgame.CycledGame;
import mineplex.hub.hubgame.common.HubGameComponent;

public class GameTimeoutComponent extends HubGameComponent<CycledGame>
{

	private static final long SECONDS_30 = TimeUnit.SECONDS.toMillis(30);

	private final long _timeout;
	private boolean _announced30Seconds;

	public GameTimeoutComponent(CycledGame game, long timeout)
	{
		super(game);

		_timeout = timeout;
	}

	@EventHandler
	public void updateTimeout(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC || !_game.isLive())
		{
			return;
		}

		if (!_announced30Seconds && UtilTime.elapsed(_game.getLastStart(), _timeout - SECONDS_30))
		{
			_game.announce("The game will end in " + F.time("30 Seconds") + ".");
			_announced30Seconds = true;
		}
		else if (UtilTime.elapsed(_game.getLastStart(), _timeout))
		{
			_game.announce("The game took too long and has ended");

			for (Player player : _game.getAlivePlayers())
			{
				_game.onPlayerDeath(player, true);
			}

			_game.getAlivePlayers().clear();
			_announced30Seconds = false;
		}
	}
}
