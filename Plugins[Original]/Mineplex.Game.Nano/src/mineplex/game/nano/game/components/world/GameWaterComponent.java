package mineplex.game.nano.game.components.world;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import mineplex.core.common.util.UtilEnt;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.nano.game.Game;
import mineplex.game.nano.game.Game.GameState;
import mineplex.game.nano.game.GameComponent;
import mineplex.game.nano.game.event.GameStateChangeEvent;

public class GameWaterComponent extends GameComponent<Game>
{

	private boolean _override;

	public GameWaterComponent(Game game)
	{
		super(game, GameState.Live);
	}

	@Override
	public void disable()
	{

	}

	public GameWaterComponent override()
	{
		_override = true;
		return this;
	}

	@EventHandler
	public void live(GameStateChangeEvent event)
	{
		if (event.getState() != GameState.Live)
		{
			return;
		}

		if (getGame().getMineplexWorld().getSpongeLocation("WATER_DAMAGE") != null)
		{
			_override = true;
		}
	}

	@EventHandler
	public void update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST || !_override)
		{
			return;
		}

		for (Player player : getGame().getAlivePlayers())
		{
			if (UtilEnt.isInWater(player))
			{
				getGame().getManager().getDamageManager().NewDamageEvent(player, null, null, DamageCause.DROWNING, 2, false, false, false, getGame().getGameType().getName(), "Water Damage");
			}
		}
	}
}
