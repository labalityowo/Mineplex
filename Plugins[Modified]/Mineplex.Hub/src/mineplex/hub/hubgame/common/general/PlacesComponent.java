package mineplex.hub.hubgame.common.general;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import mineplex.core.common.util.C;
import mineplex.hub.hubgame.CycledGame;
import mineplex.hub.hubgame.CycledGame.GameState;
import mineplex.hub.hubgame.HubGameManager;
import mineplex.hub.hubgame.common.HubGameComponent;
import mineplex.hub.hubgame.event.HubGameStateChangeEvent;

public class PlacesComponent extends HubGameComponent<CycledGame>
{

	public PlacesComponent(CycledGame game)
	{
		super(game);
	}

	@EventHandler
	public void displayPlaces(HubGameStateChangeEvent event)
	{
		if (event.getState() != GameState.End || !event.getGame().equals(_game))
		{
			return;
		}

		CycledGame game = event.getGame();
		List<Player> places = game.getPlaces();
		String gameString = _game.getManager().getGameHeader(game);

		for (Player player : places)
		{
			if (!player.isOnline())
			{
				continue;
			}

			player.sendMessage(HubGameManager.getHeaderFooter());
			player.sendMessage(gameString);
			player.sendMessage("");
			if (!places.isEmpty())
			{
				player.sendMessage(C.cRedB + "1st Place " + C.cWhite + places.get(0).getName());
			}
			if (places.size() > 1)
			{
				player.sendMessage(C.cGoldB + "2nd Place " + C.cWhite + places.get(1).getName());
			}
			if (places.size() > 2)
			{
				player.sendMessage(C.cYellowB + "3rd Place " + C.cWhite + places.get(2).getName());
			}
			player.sendMessage("");
			player.sendMessage(HubGameManager.getHeaderFooter());
		}
	}
}
