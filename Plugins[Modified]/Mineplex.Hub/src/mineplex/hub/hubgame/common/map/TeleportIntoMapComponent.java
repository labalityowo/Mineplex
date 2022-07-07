package mineplex.hub.hubgame.common.map;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import mineplex.core.common.util.UtilAlg;
import mineplex.hub.hubgame.CycledGame.GameState;
import mineplex.hub.hubgame.HubGame;
import mineplex.hub.hubgame.common.HubGameComponent;
import mineplex.hub.hubgame.event.HubGameStateChangeEvent;

public class TeleportIntoMapComponent extends HubGameComponent<HubGame>
{

	private final List<Location> _spawns;

	public TeleportIntoMapComponent(HubGame game, List<Location> spawns)
	{
		super(game);

		_spawns = spawns;
	}

	@EventHandler
	public void prepare(HubGameStateChangeEvent event)
	{
		if (event.getState() != GameState.Prepare || !event.getGame().equals(_game))
		{
			return;
		}

		Location average = UtilAlg.getAverageLocation(_spawns);
		int i = 0;

		for (Player player : _game.getAlivePlayers())
		{
			Location location = _spawns.get(i++);

			if (location.getYaw() == 0)
			{
				location.setYaw(UtilAlg.GetYaw(UtilAlg.getTrajectory(location, average)));
			}

			player.teleport(location);
		}
	}
}
