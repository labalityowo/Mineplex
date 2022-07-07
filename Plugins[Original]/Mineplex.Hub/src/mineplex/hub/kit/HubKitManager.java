package mineplex.hub.kit;

import org.bukkit.Location;

import mineplex.core.MiniPlugin;
import mineplex.core.ReflectivelyCreateMiniPlugin;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.game.MineplexGameManager;
import mineplex.core.world.MineplexWorld;
import mineplex.hub.HubManager;

@ReflectivelyCreateMiniPlugin
public class HubKitManager extends MiniPlugin
{

	private HubKitManager()
	{
		super("Hub Kit");

		MineplexGameManager gameManager = require(MineplexGameManager.class);
		MineplexWorld worldData = require(HubManager.class).getWorldData();
		HubManager manager = require(HubManager.class);

		gameManager.getKits().forEach(kit ->
		{
			Location location = worldData.getSpongeLocation("KIT " + kit.getId());

			if (location != null)
			{
				UtilAlg.lookAtNearest(location, manager.getLookAt());
				kit.createNPC(location);
			}
		});
	}
}
