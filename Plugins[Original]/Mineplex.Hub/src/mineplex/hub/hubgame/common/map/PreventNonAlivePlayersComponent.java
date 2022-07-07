package mineplex.hub.hubgame.common.map;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilServer;
import mineplex.core.gadget.event.GadgetBlockEvent;
import mineplex.core.gadget.event.GadgetCollideEntityEvent;
import mineplex.core.gadget.event.GadgetSelectLocationEvent;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.hub.hubgame.HubGame;
import mineplex.hub.hubgame.common.HubGameComponent;

public class PreventNonAlivePlayersComponent extends HubGameComponent<HubGame>
{

	private final Location _cornerA;
	private final Location _cornerB;

	public PreventNonAlivePlayersComponent(HubGame game, Location cornerA, Location cornerB)
	{
		super(game);

		_cornerA = cornerA;
		_cornerB = cornerB;
	}

	@EventHandler
	public void update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
		{
			return;
		}

		for (Player player : UtilServer.getPlayersCollection())
		{
			if (!_game.isAlive(player) && UtilAlg.inBoundingBox(player.getLocation(), _cornerA, _cornerB))
			{
				player.sendMessage(F.main(_game.getManager().getName(), "You are not allowed to enter the game area."));
				player.teleport(_game.getSpawn());
			}
		}
	}

	@EventHandler
	public void gadgetLocation(GadgetSelectLocationEvent event)
	{
		if (UtilAlg.inBoundingBox(event.getLocation(), _cornerA, _cornerB))
		{
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void gadgetBlock(GadgetBlockEvent event)
	{
		event.getBlocks().removeIf(block -> UtilAlg.inBoundingBox(block.getLocation(), _cornerA, _cornerB));
	}

	@EventHandler
	public void gadgetCollide(GadgetCollideEntityEvent event)
	{
		if (event.getEntity() instanceof Player && _game.isAlive((Player) event.getEntity()))
		{
			event.setCancelled(true);
		}
	}
}
