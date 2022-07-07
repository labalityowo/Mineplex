package mineplex.hub.gimmicks.staffbuild;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import mineplex.core.MiniPlugin;
import mineplex.core.ReflectivelyCreateMiniPlugin;
import mineplex.core.account.CoreClientManager;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilItem;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.hub.HubManager;
import mineplex.hub.player.CreativeManager;
import mineplex.hub.player.HubPlayerManager;

@ReflectivelyCreateMiniPlugin
public class StaffBuild extends MiniPlugin
{

	public enum Perm implements Permission
	{
		BUILD,
		BUILD_HISTORY,
		CLEAR_BUILD
	}

	private static final int MAX_Y_DIFFERENCE = 50;

	private final CoreClientManager _clientManager;
	private final CreativeManager _creativeManager;
	private final HubPlayerManager _playerManager;
	private final List<Location> _buildLocations;
	private final Map<Block, String> _buildHistory;

	private StaffBuild()
	{
		super("Staff Build");

		_clientManager = require(CoreClientManager.class);
		_creativeManager = require(CreativeManager.class);
		_playerManager = require(HubPlayerManager.class);
		_buildLocations = require(HubManager.class).getWorldData().getSpongeLocations(String.valueOf(Material.RED_SANDSTONE.getId()));
		_buildLocations.forEach(location -> location.getBlock().setType(Material.AIR));
		_buildHistory = new HashMap<>();

		generatePermissions();
	}

	private void generatePermissions()
	{
		PermissionGroup.BUILDER.setPermission(Perm.BUILD, true, true);
		PermissionGroup.ADMIN.setPermission(Perm.BUILD_HISTORY, true, true);
		PermissionGroup.ADMIN.setPermission(Perm.CLEAR_BUILD, true, true);
	}

	@Override
	public void addCommands()
	{
		addCommand(new BuildHistoryCommand(this));
		addCommand(new ClearBuildCommand(this));
	}

	@EventHandler
	public void updateCreative(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
		{
			return;
		}

		for (Player player : UtilServer.getPlayersCollection())
		{
			boolean allowed = isAllowed(player, player.getLocation());
			boolean creative = player.getGameMode() == GameMode.CREATIVE;

			if (allowed && !creative)
			{
				player.setGameMode(GameMode.CREATIVE);
				sendMessage(player, true);
				player.getInventory().clear();
			}
			else if (!allowed && creative)
			{
				runSyncLater(() ->
				{
					if (isAllowed(player, player.getLocation()))
					{
						return;
					}

					player.setGameMode(GameMode.ADVENTURE);
					sendMessage(player, false);
					UtilPlayer.clearPotionEffects(player);
					_playerManager.giveHotbar(player);
				}, 19);
			}
		}
	}

	private void sendMessage(Player player, boolean enabled)
	{
		player.sendMessage(F.main(_moduleName, "Build mode " + F.ed(enabled) + "."));
	}

	@EventHandler
	public void blockBreak(BlockBreakEvent event)
	{
		Player player = event.getPlayer();

		if (!_creativeManager.isInCreative(player))
		{
			if (isAllowed(player, event.getBlock().getLocation()))
			{
				_buildHistory.remove(event.getBlock());
			}
			else
			{
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void blockPlace(BlockPlaceEvent event)
	{
		Player player = event.getPlayer();

		if (!_creativeManager.isInCreative(player))
		{
			if (isAllowed(player, event.getBlock().getLocation()))
			{
				_buildHistory.put(event.getBlock(), player.getName());
			}
			else
			{
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void playerDropItem(PlayerDropItemEvent event)
	{
		Player player = event.getPlayer();

		if (_creativeManager.isInCreative(player))
		{
			return;
		}

		event.setCancelled(true);
	}

	@EventHandler
	public void hangingPlace(HangingPlaceEvent event)
	{
		Player player = event.getPlayer();

		if (_creativeManager.isInCreative(player))
		{
			return;
		}

		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void blockPlace(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();

		if (_creativeManager.isInCreative(player))
		{
			return;
		}

		ItemStack itemStack = player.getItemInHand();

		if (UtilItem.isVehicle(itemStack) || UtilItem.isDoor(itemStack) || itemStack.getType() == Material.ARMOR_STAND)
		{
			event.setCancelled(true);
		}
	}

	private boolean isAllowed(Player player, Location location)
	{
		if (_creativeManager.isInCreative(player))
		{
			return true;
		}
		else if (!_clientManager.Get(player).hasPermission(Perm.BUILD))
		{
			return false;
		}

		for (Location buildLocation : _buildLocations)
		{
			int delta = location.getBlockY() - buildLocation.getBlockY();

			if (location.getBlockX() == buildLocation.getBlockX() && location.getBlockZ() == buildLocation.getBlockZ() && delta >= 0 && delta <= MAX_Y_DIFFERENCE)
			{
				return true;
			}
		}

		return false;
	}

	public Map<Block, String> getBuildHistory()
	{
		return _buildHistory;
	}
}
