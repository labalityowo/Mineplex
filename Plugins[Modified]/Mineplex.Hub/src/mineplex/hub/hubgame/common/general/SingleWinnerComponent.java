package mineplex.hub.hubgame.common.general;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilText;
import mineplex.hub.hubgame.CycledGame;
import mineplex.hub.hubgame.CycledGame.GameState;
import mineplex.hub.hubgame.HubGameManager;
import mineplex.hub.hubgame.common.HubGameComponent;
import mineplex.hub.hubgame.event.HubGameStateChangeEvent;

public class SingleWinnerComponent extends HubGameComponent<CycledGame>
{

	public SingleWinnerComponent(CycledGame game)
	{
		super(game);
	}

	@EventHandler
	public void displayWinner(HubGameStateChangeEvent event)
	{
		if (event.getState() != GameState.End || !event.getGame().equals(_game))
		{
			return;
		}

		CycledGame game = event.getGame();
		List<Player> places = game.getPlaces();

		if (places.isEmpty())
		{
			return;
		}

		Player winner = places.get(0);
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

			player.sendMessage(UtilText.centerChat(C.cWhiteB + "Winner  " + (winner.equals(player) ? C.cGreen : C.cRed) + winner.getName(), LineFormat.CHAT));

			player.sendMessage("");
			player.sendMessage(HubGameManager.getHeaderFooter());
		}
	}
}
