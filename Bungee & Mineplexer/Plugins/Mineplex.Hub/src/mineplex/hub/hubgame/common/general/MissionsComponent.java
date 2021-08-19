package mineplex.hub.hubgame.common.general;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import mineplex.core.mission.MissionManager;
import mineplex.core.mission.MissionTrackerType;
import mineplex.hub.hubgame.CycledGame;
import mineplex.hub.hubgame.CycledGame.GameState;
import mineplex.hub.hubgame.common.HubGameComponent;
import mineplex.hub.hubgame.event.HubGameStateChangeEvent;

public class MissionsComponent extends HubGameComponent<CycledGame>
{

	public MissionsComponent(CycledGame game)
	{
		super(game);
	}

	@EventHandler
	public void gamesPlayed(HubGameStateChangeEvent event)
	{
		if (event.getState() != GameState.End || !event.getGame().equals(_game))
		{
			return;
		}

		MissionManager manager = _game.getManager().getHubManager().getMissionManager();

		for (Player player : _game.getAllPlayers())
		{
			manager.incrementProgress(player, 1, MissionTrackerType.LOBBY_GAMES_PLAYED, null, _game.getGameType().toString());
		}
	}
}
