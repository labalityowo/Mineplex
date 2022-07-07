package mineplex.hub.hubgame.common.general;

import org.bukkit.event.EventHandler;

import mineplex.hub.doublejump.DoubleJumpPrepareEvent;
import mineplex.hub.hubgame.HubGame;
import mineplex.hub.hubgame.common.HubGameComponent;

public class DoubleJumpComponent extends HubGameComponent<HubGame>
{

	public DoubleJumpComponent(HubGame game)
	{
		super(game);
	}

	@EventHandler
	public void doubleJump(DoubleJumpPrepareEvent event)
	{
		if (!_game.isAlive(event.getPlayer()))
		{
			return;
		}

		event.setCancelled(true);
	}
}
