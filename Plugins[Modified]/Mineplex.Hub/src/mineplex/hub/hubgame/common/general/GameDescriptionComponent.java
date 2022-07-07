package mineplex.hub.hubgame.common.general;

import java.util.function.Function;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import mineplex.hub.hubgame.CycledGame;
import mineplex.hub.hubgame.CycledGame.GameState;
import mineplex.hub.hubgame.HubGameManager;
import mineplex.hub.hubgame.common.HubGameComponent;
import mineplex.hub.hubgame.event.HubGameStateChangeEvent;

public class GameDescriptionComponent extends HubGameComponent<CycledGame>
{

	private final Function<Player, String> _customLines;

	public GameDescriptionComponent(CycledGame game)
	{
		this(game, null);
	}

	public GameDescriptionComponent(CycledGame game, Function<Player, String> customLine)
	{
		super(game);

		_customLines = customLine;
	}

	@EventHandler
	public void displayDescription(HubGameStateChangeEvent event)
	{
		if (event.getState() != GameState.Prepare || !event.getGame().equals(_game))
		{
			return;
		}

		CycledGame game = event.getGame();

		String gameString = _game.getManager().getGameHeader(game);

		for (Player player : game.getAllPlayers())
		{
			player.sendMessage(HubGameManager.getHeaderFooter());
			player.sendMessage(gameString);
			player.sendMessage("");

			for (String description : game.getGameType().getDescription())
			{
				player.sendMessage("  " + description);
			}

			if (_customLines != null)
			{
				String line = _customLines.apply(player);

				if (line != null)
				{
					player.sendMessage("");
					player.sendMessage("  "  + line);
				}
			}

			player.sendMessage("");
			player.sendMessage(HubGameManager.getHeaderFooter());

			player.playSound(player.getLocation(), Sound.LEVEL_UP, 1, 1.2F);
		}
	}

}
