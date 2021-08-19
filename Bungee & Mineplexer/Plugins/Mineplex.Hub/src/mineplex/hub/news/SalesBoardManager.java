package mineplex.hub.news;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import mineplex.core.MiniPlugin;
import mineplex.core.ReflectivelyCreateMiniPlugin;
import mineplex.core.account.CoreClientManager;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.imagemap.ImageMapManager;

@ReflectivelyCreateMiniPlugin
public class SalesBoardManager extends MiniPlugin
{

	private final CoreClientManager _clientManager;
	private final ImageMapManager _mapManager;

	//private final PlayerMapBoard _board;

	private SalesBoardManager()
	{
		super("Sales Board");

		_clientManager = require(CoreClientManager.class);
		_mapManager = require(ImageMapManager.class);

//		Location location = require(WorldDataModule.class).getCustomLocation("SALES BOARD").get(0).add(0, 0, 1);
//		_board = _mapManager.createPlayerBoard(location, BlockFace.NORTH, 9, 5, false,"BLESSED.png");
	}

	@EventHandler
	public void playerJoin(PlayerJoinEvent event)
	{
		Player player = event.getPlayer();
		PermissionGroup group = _clientManager.Get(player).getPrimaryGroup();

		runSyncLater(() ->
		{
			if (!player.isOnline())
			{
				return;
			}

			//_board.goTo(player, 0);
		}, 40);
	}
}
