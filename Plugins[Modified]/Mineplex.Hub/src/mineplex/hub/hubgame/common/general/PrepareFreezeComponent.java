package mineplex.hub.hubgame.common.general;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

import mineplex.hub.hubgame.CycledGame;
import mineplex.hub.hubgame.CycledGame.GameState;
import mineplex.hub.hubgame.common.HubGameComponent;

public class PrepareFreezeComponent extends HubGameComponent<CycledGame>
{

	public PrepareFreezeComponent(CycledGame game)
	{
		super(game);
	}

	@EventHandler
	public void playerMove(PlayerMoveEvent event)
	{
		if (_game.getGameState() != GameState.Prepare || !_game.isAlive(event.getPlayer()))
		{
			return;
		}

		Location from = event.getFrom();
		Location to = event.getTo();

		if (from.getX() == to.getX() && from.getZ() == to.getZ())
		{
			return;
		}

		event.setTo(from);
	}
}
