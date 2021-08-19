package mineplex.hub.hubgame.common.general;

import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import mineplex.hub.hubgame.CycledGame;
import mineplex.hub.hubgame.CycledGame.GameState;
import mineplex.hub.hubgame.common.HubGameComponent;
import mineplex.hub.hubgame.event.HubGamePlayerDeathEvent;
import mineplex.hub.hubgame.event.HubGameStateChangeEvent;

public class PlayerGameModeComponent extends HubGameComponent<CycledGame>
{

	private final GameMode _gameMode;

	public PlayerGameModeComponent(CycledGame game, GameMode gameMode)
	{
		super(game);

		_gameMode = gameMode;
	}

	@EventHandler
	public void prepare(HubGameStateChangeEvent event)
	{
		if (event.getState() != GameState.Prepare)
		{
			return;
		}

		_game.getAlivePlayers().forEach(player -> player.setGameMode(_gameMode));
	}

	@EventHandler(priority = EventPriority.LOW)
	public void playerDeath(HubGamePlayerDeathEvent event)
	{
		event.getPlayer().setGameMode(GameMode.ADVENTURE);
	}
}
