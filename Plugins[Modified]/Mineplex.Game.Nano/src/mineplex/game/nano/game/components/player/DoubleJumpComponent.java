package mineplex.game.nano.game.components.player;

import org.bukkit.Effect;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.nano.game.Game;
import mineplex.game.nano.game.Game.GameState;
import mineplex.game.nano.game.GameComponent;
import mineplex.game.nano.game.event.GameStateChangeEvent;

public class DoubleJumpComponent extends GameComponent<Game>
{

	private double _magnitude = 1.2;
	private boolean _directional;

	public DoubleJumpComponent(Game game)
	{
		super(game, GameState.Live, GameState.End);
	}

	@Override
	public void disable()
	{

	}

	public DoubleJumpComponent setMagnitude(double magnitude)
	{
		_magnitude = magnitude;
		return this;
	}

	public DoubleJumpComponent setDirectional(boolean directional)
	{
		_directional = directional;
		return this;
	}

	@EventHandler
	public void live(GameStateChangeEvent event)
	{
		if (event.getState() != GameState.Live)
		{
			return;
		}

		getGame().getManager().runSyncLater(() ->
		{
			if (!getGame().isLive())
			{
				return;
			}

			Player[] players = getGame().getAllPlayers().toArray(new Player[0]);

			for (Player player : players)
			{
				player.playEffect(player.getLocation(), Effect.BLAZE_SHOOT, 8);
			}

			UtilTextMiddle.display(C.cYellow + "Double Jump", "Double Tap Jump to Leap!", 0, 30, 20, players);
		}, 5);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void updateJump(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK || !getGame().isLive())
		{
			return;
		}

		for (Player player : getGame().getAlivePlayers())
		{
			if (player.getAllowFlight())
			{
				continue;
			}

			if (UtilEnt.isGrounded(player))
			{
				player.setAllowFlight(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void toggleFlight(PlayerToggleFlightEvent event)
	{
		Player player = event.getPlayer();

		if (!event.isFlying() || !getGame().isAlive(player))
		{
			return;
		}

		if (!getGame().isLive())
		{
			event.setCancelled(true);
			player.setAllowFlight(false);
			return;
		}

		Vector velocity = player.getLocation().getDirection();

		if (_directional)
		{
			velocity.setY(velocity.getY() + 0.2);
		}
		else
		{
			velocity.setY(0.4);
		}

		velocity.multiply(_magnitude);

		UtilAction.velocity(player, velocity);
		event.setCancelled(true);
		player.setAllowFlight(false);
		player.playEffect(player.getLocation(), Effect.BLAZE_SHOOT, 8);
	}
}
